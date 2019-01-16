package de.todo4you.todo4you.model;

public enum TodoState {
    // Invalid
    UNINITIALIZED,
    // Loaded from calendar. Not modified
    UNMODIFIED,
    // Loaded from calendar. Modified
    MODIFIED,
    // Created in App. Needs sync to Calendar
    FRESHLY_CREATED,
}
