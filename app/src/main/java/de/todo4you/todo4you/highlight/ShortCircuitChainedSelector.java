package de.todo4you.todo4you.highlight;

import java.util.Arrays;
import java.util.List;

import de.todo4you.todo4you.model.Todo;

public class ShortCircuitChainedSelector implements HighlightSelector {
    final HighlightSelector[] selectors;

    public ShortCircuitChainedSelector(HighlightSelector... selectors) {
        this.selectors = Arrays.copyOf(selectors, selectors.length);
    }


    @Override
    public Todo select(List<Todo> todos) {
        if (todos.isEmpty()) {
            return null;
        }

        for (HighlightSelector selector : selectors) {
            Todo selected = selector.select(todos);
            if (selected != null) {
                return selected; // short-circuit => exit on first match
            }
        }

        return todos.get(0);
    }
}
