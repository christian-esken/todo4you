package de.todo4you.todo4you.tasks;

import java.util.ArrayList;
import java.util.List;

import de.todo4you.todo4you.model.Idea;

/**
 * Holds the result from a store fetch
 */
public class StoreResult {
    private final List<Idea> ideas;
    private final StoreStatus status;

    /**
     * Creates a new StoreResult. The To Do references are copied to a new list.
     * @param ideas
     * @param storeState
     */
    public StoreResult (List<Idea> ideas, StoreState storeState) {
        this(ideas, new StoreStatus(storeState, null, null));
    }

    /**
     * Creates a new StoreResult. The To Do references are copied to a new list.
     * @param ideas
     * @param status
     * @param userErrorMessage
     * @param exception
     */
    public StoreResult (List<Idea> ideas, StoreState status, String userErrorMessage, Exception exception) {
        this(ideas, new StoreStatus(status, userErrorMessage, exception));
    }

    public StoreResult(List<Idea> ideas, StoreStatus status) {
        this.ideas = new ArrayList<>(ideas);
        this.status = status;
    }

    public List<Idea> getTodos() {
        return ideas;
    }

    public StoreStatus getStatus() {
        return status;
    }
}

