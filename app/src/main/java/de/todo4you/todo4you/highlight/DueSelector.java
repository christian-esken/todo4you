package de.todo4you.todo4you.highlight;

import java.time.LocalDate;
import java.util.List;

import de.todo4you.todo4you.model.Todo;

public class DueSelector implements HighlightSelector {
    @Override
    public Todo select(List<Todo> todos) {
        Todo best = null;
        LocalDate earliestDue = null;
        for (Todo todo : todos) {
            LocalDate due = todo.getDueDate();
            if (due == null) {
                continue;
            }
            if (best == null || due.isBefore(earliestDue)) {
                best = todo;
                earliestDue = due;
            }
        }

        return best;
    }
}
