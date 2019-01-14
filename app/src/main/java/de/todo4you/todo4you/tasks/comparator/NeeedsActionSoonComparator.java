package de.todo4you.todo4you.tasks.comparator;

import java.time.LocalDate;
import java.util.Comparator;

import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.util.StandardDates;

/**
 * Sorts anything that is can or should be worked on to the front. This means, everything that
 * is due, soon due, or can be started now or soon. Anything that is not due soon is sorted
 * to the end. Please note that this comparator treats due and start dates equal.
 */
public class NeeedsActionSoonComparator implements Comparator<Todo> {
    @Override
    public int compare(Todo o1, Todo o2) {
        LocalDate actionDate1 = o1.getDueDate();
        LocalDate sd1 = o1.getStartDate();
        if (actionDate1 == null || StandardDates.compare(sd1, actionDate1) < 0) {
            // If there is no due date or the start date is before it, then pick it
            actionDate1 = o1.getStartDate();
        }
        LocalDate actionDate2 = o2.getDueDate();
        LocalDate sd2 = o2.getStartDate();
        if (actionDate2 == null || StandardDates.compare(sd2, actionDate2) < 0) {
            // If there is no due date or the start date is before it, then pick it
            actionDate2 = o2.getStartDate();
        }

        if (actionDate1 == null && actionDate2 == null) {
            return 0; // Fast-path: Both TO DO entries have no dates set
        }

        boolean dueSoon1 = StandardDates.isDueSoon(actionDate1);
        boolean dueSoon2 = StandardDates.isDueSoon(actionDate2);
        if (dueSoon1 ^ dueSoon2) {
            // Only one is due soon
            return dueSoon1 ? -1 : 1;
        }

        // None is due soon
        return 0;
    }
}
