package de.todo4you.todo4you.tasks.comparator;

import java.util.Comparator;

import de.todo4you.todo4you.model.Idea;

public class StandardTodoComparator implements Comparator<Idea> {
    NeeedsActionSoonComparator neeedsActionSoonComparator = new NeeedsActionSoonComparator();
    DateBasedTodoComparator dateBasedTodoComparator = new DateBasedTodoComparator();
    @Override
    public int compare(Idea o1, Idea o2) {
        int compare = neeedsActionSoonComparator.compare(o1, o2);
        if (compare != 0)
            return compare;

        return dateBasedTodoComparator.compare(o1, o2);
    }
}
