package de.todo4you.todo4you.tasks;

import android.support.annotation.GuardedBy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.todo4you.todo4you.model.Idea;
import de.todo4you.todo4you.storage.StorageStatistics;
import de.todo4you.todo4you.util.StoreUpdateNotifier;

/**
 * The TaskStore holds all tasks and is responsible to sync between all three targets:
 * The in-memory representation in this TaskStore, the device DB, the cloud storage.
 */
public class TaskStore extends Thread {

    public static final long FIFTEEN_MINUTES_IN_MS = 1000 * 15 * 60;
    public static final long MINIMUM_WAIT_IN_MS = 10000; // Wait at least 10 seconds
    @GuardedBy("TaskStore.class")
    private static TaskStore instance;
    List<StoreUpdateNotifier> listeners = new CopyOnWriteArrayList<>();

    // Migrate the following 2 back to StoreResult ?!?
    List<Idea> ideas = new ArrayList<>();
    StoreStatus status = StoreStatus.loading;

    Idea highlightIdea; // TODO move this field somewhere else.

    private volatile boolean running = true;
    private volatile boolean upstreamSync = false;
    private volatile boolean downstreamSync = false;


    public static synchronized TaskStore instance() {
        if (instance == null) {
            instance = new TaskStore();
            instance.start();
        }
        return instance;
    }

    public StoreResult getAll() {
        return new StoreResult(ideas, status);
    }

    public Idea getByUid(String ruid) {
        if (ruid == null) {
            return null;
        }
        for (Idea idea : ideas) {
            if (ruid.equals(idea.getUid())) {
                return idea;
            }
        }
        return null;
    }

    public TaskStoreStatistics statistics() {
        int iCount = ideas.size();
        StorageStatistics memory = new StorageStatistics("App", true, iCount, iCount);
        int cCount = (int)ideas.stream().filter(i -> !i.needsCloudSync()).count();
        StorageStatistics cloud = new StorageStatistics("Cloud", true, iCount, cCount);
        int dCount = (int)ideas.stream().filter(i -> !i.needsDeviceSync()).count();
        StorageStatistics device = new StorageStatistics("AppDB", false, 0, dCount);

        return new TaskStoreStatistics(memory, cloud, device);
    }

    public Idea getHighlightIdea() {
        return highlightIdea;
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


    public Idea findBySummary(Object taskObject) {
        if (!(taskObject instanceof String)) {
            return null;
        }
        String taskDescription = (String)taskObject;
        for (Idea idea : ideas) {
            if (idea.getSummary().isEmpty()) {
                continue; // no summary
            }
            // findBySummary() is hacky. Two entries could have the same summary.
            // We need full tasks in the taskListView (or at least a key/reference)
            // Additionally the taskDescription has a status prepended, so only the start
            // matches.
            if (taskDescription.startsWith(idea.getSummary())) {
                return idea;
            }
        }
        return null;
    }

    @Override
    public void run() {
        long nextDownstreamSync = 0;
        while (running) {
            try {
                TaskDAO taskDao = TaskDAO.instance();
                // -1- upstream-sync NEW ideas and MODIFIED ideas
                Iterator<Idea> iterator = ideas.iterator();
                while (iterator.hasNext()) {
                    Idea idea = iterator.next();
                    if (idea.needsDeviceSync()) {
                        // TODO implement writing to SQLite here
                    }
                    if (idea.needsCloudSync()) {
                        idea.updateVTodoFromModel();
                        if (taskDao.insertOrUpdate(idea)) {
                            idea.setCloudSynced();
                        }
                    }
                }

                // TODO Trigger sleep here if we did not reach the time for doing the downstream sync.
                long now = System.currentTimeMillis();
                if (downstreamSync || now >= nextDownstreamSync) {
                    // downstreamSync can be triggered by a reload gesture
                    downstreamSync = false;
                    nextDownstreamSync = now + FIFTEEN_MINUTES_IN_MS;
                    // -2- Load everything new
                    // Note: This should be time triggered
                    StoreResult todosLoaded = taskDao.loadAll();
                    status = todosLoaded.getStatus();
                    merge(todosLoaded); // merge in everything you got (so far)
                    informListeners();
                }
            }
            catch(Exception exc){
                // Unexpected exception, e.g. NPE. No data recieved, but remember the error
                status = new StoreStatus(StoreState.ERROR, "Error: " + exc.getMessage(), exc);
            }

            // Interruption policy: continue, and let the loop determine the "running" flag
            if (upstreamSync || this.isInterrupted()) {
                upstreamSync = false;
                continue;
            }
            try {
                long remainingMillis = Math.max(MINIMUM_WAIT_IN_MS, nextDownstreamSync - System.currentTimeMillis());

                Thread.sleep(remainingMillis); // update all 15 minutes
            } catch (InterruptedException e) {
                // Interruption policy: continue, and let the loop determine the "running" flag
                continue;
            }
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
     * @param remote
     * @return
     */
    private void merge(StoreResult remote) {
        List<Idea> merged = new ArrayList<>(remote.getTodos().size());
        List<Idea> locked = new ArrayList<>();

        // -1- Pick all remote entries, unless a local entry is dirty (not yet synced)
        for (Idea rtodo : remote.getTodos()) {
            String ruid = rtodo.getUid();
            Idea ltodo = getByUid(ruid);
            /*    ltodo  null  locked   add
             *              t       *   merged
             *              f       f   merged
             *              f       t   locked
             */
            boolean isLocked = ltodo != null && ltodo.isLocked();
            if (isLocked) {
                locked.add(ltodo); // add local
            } else {
                // TOOD Also check if local modification time is newer than remote. Then keep the local.
                merged.add(rtodo); // add remote
            }
        }

        // -2- Pick all local dirty entries (they are modified or newly created)
        merged.addAll(locked);

        status = remote.getStatus();
        ideas = merged;
    }

    private void informListeners() {
        StoreResult storeResult = new StoreResult(ideas, status);
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
    public void taskModifed(Idea task) {
        Idea ideaFromStore = getByUid(task.getUid());
        if (ideaFromStore != null) {
            // Inform the listeners if we know the UID. Even if it is a different
            // To do instance, as identity is defined by UID.
            informListeners();
            triggerUpstreamSync();
        }
    }

    public void addNewTask(Idea task) {
        ideas.add(task);
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

    // TODO Likely move this method somewhere else
    public void setHighlightIdea(Idea idea) {
        if (idea != highlightIdea) {
            // modified
            for (StoreUpdateNotifier listener : listeners) {
                highlightIdea = idea;
                synchronized (listener) {
                    // Use the method parameter, as the field could concurrently change
                    // We do not yet do specific thread-safety here, e.g. DCL or AtomicReference.
                    listener.updateHighlight(idea);
                }
            }
        }
    }
}
