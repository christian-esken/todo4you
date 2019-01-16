package de.todo4you.todo4you.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.util.StoreUpdateNotifier;

public class TaskStore extends Thread {

    private static TaskStore instance;
    volatile StoreResult storeResult = StoreResult.loading();
    List<StoreUpdateNotifier> listeners = new CopyOnWriteArrayList<>();
    List<Todo> unsyncedTasks = new ArrayList<>();

    private volatile boolean running = true;

    public StoreResult getAll() {
        StoreResult mergedResult = new StoreResult(storeResult.getTodos(), storeResult.getStatus(), storeResult.getUserErrorMessaage(), storeResult.getException());
        mergedResult.addUnsyncedTodos(unsyncedTasks);
        return mergedResult;
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
    void registerListener(StoreUpdateNotifier obj) {
        listeners.add(obj);
    }
    void unregisterListener(StoreUpdateNotifier obj) {
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
                StoreResult todosLoaded = TaskDAO.instance().loadAll();
                todosLoaded.addUnsyncedTodos(unsyncedTasks);

                // possiby merge TO DO's
                if (todosLoaded.getStatus() == StoreState.LOADED) {
                    // everything loaded well => replace complete result
                    storeResult = todosLoaded;

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
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                continue;
            }

            //adapter.notifyDataSetChanged();
        }
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

    public void addNewTask(Todo task) {
        unsyncedTasks.add(task); // For next refresh
        storeResult.addUnsyncedTodo(task); // For now
        informListeners();
    }
}
