package de.todo4you.todo4you.tasks;

import java.util.Collections;
import java.util.List;

import de.todo4you.todo4you.model.Todo;

public class StoreResult {
    private final List<Todo> todos;
    private final StoreState status;
    private final String userErrorMessaage;
    private final Exception exception;

    public StoreResult (List<Todo> todos, StoreState storeState) {
        this(todos, storeState, null, null);
    }

    public StoreResult (List<Todo> todos, StoreState status, String userErrorMessaage, Exception exception) {
        this.todos = todos;
        this.status = status;
        this.userErrorMessaage = userErrorMessaage;
        this.exception = exception;
    }

    public static StoreResult loading() {
        return new StoreResult(Collections.emptyList(), StoreState.LOADING);
    }


    public List<Todo> getTodos() {
        return todos;
    }

    public StoreState getStatus() {
        return status;
    }

    public String getUserErrorMessaage() {
        return userErrorMessaage;
    }

    public Exception getException() {
        return exception;
    }

    public void addUnsyncedTodo(Todo unsyncedTask) {
        todos.add(unsyncedTask);
    }

    public void addUnsyncedTodos(List<Todo> unsyncedTasks) {
        todos.addAll(unsyncedTasks);
    }
}

