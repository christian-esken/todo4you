package de.todo4you.todo4you.tasks;

import java.util.ArrayList;
import java.util.List;

import de.todo4you.todo4you.model.Todo;

/**
 * Holds the result from a store fetch
 */
public class StoreResult {
    private final List<Todo> ideas;
    private final StoreStatus status;

    /**
     * Creates a new StoreResult. The To Do references are copied to a new list.
     * @param ideas
     * @param storeState
     */
    public StoreResult (List<Todo> ideas, StoreState storeState) {
        this(ideas, new StoreStatus(storeState, null, null));
    }

    /**
     * Creates a new StoreResult. The To Do references are copied to a new list.
     * @param todos
     * @param status
     * @param userErrorMessage
     * @param exception
     */
    public StoreResult (List<Todo> todos, StoreState status, String userErrorMessage, Exception exception) {
        this(todos, new StoreStatus(status, userErrorMessage, exception));
    }

    public StoreResult(List<Todo> ideas, StoreStatus status) {
        this.ideas = new ArrayList<>(ideas);
        this.status = status;
    }

    public List<Todo> getTodos() {
        return ideas;
    }

    public StoreStatus getStatus() {
        return status;
    }
}

