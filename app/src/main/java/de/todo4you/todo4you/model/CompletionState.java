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
    CANCELLED
}
