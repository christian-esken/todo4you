package de.todo4you.todo4you.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.util.StoreUpdateNotifier;

public class TaskStore extends Thread {

    private static TaskStore instance;
    volatile StoreResult storeResult = StoreResult.loading();
    List<StoreUpdateNotifier> listeners = new CopyOnWriteArrayList<>();
    List<Todo> unsyncedTasks = new ArrayList<>();
    Todo highlightTodo;

    private volatile boolean running = true;
    private volatile boolean upstreamSync = false;


    public StoreResult getAll() {
        StoreResult mergedResult = new StoreResult(storeResult.getTodos(), storeResult.getStatus(), storeResult.getUserErrorMessaage(), storeResult.getException());
        mergedResult.addUnsyncedTodos(unsyncedTasks);
        return mergedResult;
    }

    public Todo getByUid(String uid) {
        if (uid == null) {
            return null;
        }
        Todo todo = storeResult.getByUid(uid);
        if (todo != null) {
            return todo;
        }

        for (Todo todo1 : unsyncedTasks) {
            if (uid.equals(todo1.getUid())) {
                return todo1;
            }
        }
        return null;
    }

    public Todo getHighlightTodo() {
        return highlightTodo;
    }

    public static final synchronized TaskStore instance() {
        if (instance == null) {
            instance = new TaskStore();
            instance.start();
        }
        return instance;
    }

    TaskStore()
    {
        setName("TaskStoreUpdater");
    }
    public void registerListener(StoreUpdateNotifier obj) {
        listeners.add(obj);
    }
    public void unregisterListener(StoreUpdateNotifier obj) {
        listeners.remove(obj);
    }


    public Todo findBySummary(Object taskObject) {
        if (!(taskObject instanceof String)) {
            return null;
        }
        String taskDescription = (String)taskObject;
        for (Todo todo : storeResult.getTodos()) {
            if (todo.getSummary().isEmpty()) {
                continue; // no summary
            }
            // findBySummary() is hacky. Two entries could have the same summary.
            // We need full tasks in the taskListView (or at least a key/reference)
            // Additionally the taskDescription has a status prepended, so only the start
            // matches.
            if (taskDescription.startsWith(todo.getSummary())) {
                return todo;
            }
        }

        for (Todo todo : unsyncedTasks) {
            if (todo.getSummary().isEmpty()) {
                continue; // no summary
            }
            if (taskDescription.startsWith(todo.getSummary())) {
                return todo;
            }
        }

        return null;
    }

    @Override
    public void run() {
        while (running) {
            try {
                TaskDAO taskDao = TaskDAO.instance();
                // -1- upstream-sync NEW tasks
                Iterator<Todo> iterator = unsyncedTasks.iterator();
                while (iterator.hasNext()) {
                    Todo unsyncedTask = iterator.next();
                    unsyncedTask.updateVTodoFromModel();
                    if (taskDao.add(unsyncedTask)) {
                        unsyncedTask.setDirty(false);
                        iterator.remove();
                    }
                }

                // -2- upstream-sync MODIFIED tasks
                for (Todo todo : storeResult.getTodos()) {
                    if (todo.isDirty()) {
                        if (todo.updateVTodoFromModel()) {
                            // really modified (not a A-B-A reverting change)
                            if (taskDao.update(todo)) {
                                // successfully synced back
                                todo.setDirty(false);
                            }
                        }
                    }
                }

                if (upstreamSync) {
                    upstreamSync = false;
                    // On a specific upstream-only-sync, we do not reload data.
                    // Informing the listeners is also not done, as all changes
                    // are already locally known.
                    continue;
                }

                // -3- Load everything new
                StoreResult todosLoaded = taskDao.loadAll();
                todosLoaded.addUnsyncedTodos(unsyncedTasks);

                // -4- Set new storeResult, merging local and remote stores
                if (todosLoaded.getStatus() == StoreState.LOADED) {
                    // everything loaded well => merge in changes
                    storeResult = merge(storeResult, todosLoaded);
                    informListeners();

                } else {
                    // ERROR => keep the old todos. Might be just a network disconnect
                    StoreResult mergedResult = new StoreResult(storeResult.getTodos(), todosLoaded.getStatus(), todosLoaded.getUserErrorMessaage(), todosLoaded.getException());
                    storeResult = mergedResult;
                }
            } catch (Exception exc) {
                // Unexpected exception, e.g. NPE. Still keep the old todos.
                StoreResult mergedResult = new StoreResult(storeResult.getTodos(), StoreState.ERROR, "Error: " + exc.getMessage(), exc);
                storeResult = mergedResult;
            }

            // Interruption policy: continue, and let the loop detrmine the "running" flag
            if (this.isInterrupted()) {
                continue;
            }
            try {
                Thread.sleep(1000 * 15 * 60); // update all 15 minutes
            } catch (InterruptedException e) {
                continue;
            }

            //adapter.notifyDataSetChanged();
        }
    }

    /**
     * Merges the local (in memory) with the remote (fetched) StoreResult and returns a
     * new StoreResult. In normal cases, the new StoreResult contains all entries from remote.
     * Locally modified and created entries are taken from local instead of remote.
     * <br>
     *     Implementation hint: The merge takes precautions to not overwrite locally modified
     *     entries. This can easily happen, when merge() is called while the user edits a task.
     *
     * @param local
     * @param remote
     * @return
     */
    private StoreResult merge(StoreResult local, StoreResult remote) {
        List<Todo> merged = new ArrayList<>(remote.getTodos().size());
        List<Todo> locked = new ArrayList<>();

        // -1- Pick all remote entries, unless a local entry is dirty (not yet synced)
        for (Todo rtodo : remote.getTodos()) {
            String ruid = rtodo.getUid();
            Todo ltodo = local.getByUid(ruid);
            /*    ltodo  null  locked   add
             *              t       *   merged
             *              f       f   merged
             *              f       t   locked
             */
            boolean isLocked = ltodo != null && ltodo.isLocked();
            if (isLocked) {
                locked.add(ltodo);
            } else {
                // TOOD Also check if local modification time is newer than remote. Then keep the local.
                merged.add(rtodo);
            }
        }

        // -2- Pick all local dirty entries (they are modified or newly created)
        merged.addAll(locked);

        StoreResult mergedResult = new StoreResult(merged, remote.getStatus(), remote.getUserErrorMessaage(), remote.getException());
        return mergedResult;
    }

    private void informListeners() {
        for (StoreUpdateNotifier listener : listeners) {
            synchronized (listener) {
                // Quite trivially implemented update process. If used not properly, listeners
                // may lose notifications.
                listener.update(storeResult);
            }
        }
    }

    public void shutdownNow() {
        this.running = false;
        this.interrupt();
    }

    /**
     * Informs this TaskStore that an existing task was modified. This method must be
     * called whenever any data field in the To Do was modified. This method ensures
     * that all registered Listeners are informed and that the modified task will be
     * upstream-synced on the next occasion. If the given task does not exist in this
     * TaskStore, this method returns without any action.
     * @param task The modified task. It must be known in this TaskStore.
     */
    public void taskModifed(Todo task) {
        Todo todoFromStore = getByUid(task.getUid());
        if (todoFromStore != null) {
            // Inform the listeners if we know the UID. Even if it is a different
            // To do instance, as identity is defined by UID.
            informListeners();
            triggerUpstreamSync();
        }
    }

    public void addNewTask(Todo task) {
        unsyncedTasks.add(task); // For next refresh
        storeResult.addUnsyncedTodo(task); // For now
        informListeners();
        triggerUpstreamSync();
    }

    public void refresh() {
        // Just interrupt. Usually the thread is currently in Thread.sleep(...).
        this.interrupt();
    }

    private void triggerUpstreamSync() {
        upstreamSync = true;
        this.interrupt();
    }

    public void setHighlightTodo(Todo todo) {
        if (todo != highlightTodo) {
            // modified
            for (StoreUpdateNotifier listener : listeners) {
                highlightTodo = todo;
                synchronized (listener) {
                    // Use the method parameter, as the field could concurrently change
                    // We do not yet do specific thread-safety here, e.g. DCL or AtomicReference.
                    listener.updateHighlight(todo);
                }
            }
        }
    }
}
