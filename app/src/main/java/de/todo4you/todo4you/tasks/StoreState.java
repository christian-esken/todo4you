package de.todo4you.todo4you.tasks;

enum StoreState {
    EMPTY, // only before the first load
    LOADING, // while (first) load
    LOADED, // loaded
    ERROR  // error loading
}
