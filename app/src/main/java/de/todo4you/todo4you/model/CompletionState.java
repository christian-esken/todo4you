package de.todo4you.todo4you.model;

/**
 * The idea status. A new idea,
 */
public enum CompletionState {
    UNDEFINED,
    // A new idea
    NEW,
    ///  The idea is worked on
    IN_PROGRESS,
    // Completed
    COMPLETED,
    // Cancelled
    CANCELLED;

    public static CompletionState valueOfOrDefault(String state, CompletionState defaultValue) {
        if (state == null) {
            return defaultValue;
        }
        try {
            return CompletionState.valueOf(state);
        } catch (Exception exc) {
            // If it is null, or an unknown status, we return the default value
            return defaultValue;
        }
    }
}
