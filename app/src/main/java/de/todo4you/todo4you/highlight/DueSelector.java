package de.todo4you.todo4you.highlight;

import net.fortuna.ical4j.model.property.Due;

import java.util.List;

import de.todo4you.todo4you.model.Todo;

public class DueSelector implements HighlightSelector {
    @Override
    public Todo select(List<Todo> todos) {
        Todo best = null;
        long earliestDue = 0;
        for (Todo todo : todos) {
            Due due = todo.vtodo().getDue();
            if (due == null) {
                continue;
            }
            long dueTime = due.getDate().getTime();
            if (best == null || dueTime < earliestDue) {
                best = todo;
                earliestDue = dueTime;
            }
        }

        return best;
    }
}
