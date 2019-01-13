package de.todo4you.todo4you.highlight;

import java.util.List;

import de.todo4you.todo4you.model.Todo;

public interface HighlightSelector {
    Todo select(List<Todo> todos);
}
