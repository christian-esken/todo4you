package de.todo4you.todo4you.tasks.comparator;

import java.util.Comparator;

import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.util.StandardDates;

/**
 * Sort entries with due date to the front, after it the entries with start date.
 */
class DateBasedTodoComparator implements Comparator<Todo> {
    @Override
    public int compare(Todo o1, Todo o2) {
        int compare = StandardDates.compare(o1.getDueDate(), o2.getDueDate());
        if (compare != 0) {
            return compare;
        }

        compare = StandardDates.compare(o1.getStartDate(), o2.getStartDate());
        if (compare != 0) {
            return compare;
        }
        return 0;
    }
}
