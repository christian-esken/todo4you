package de.todo4you.todo4you.highlight;

import java.time.LocalDate;
import java.util.List;

import de.todo4you.todo4you.model.Idea;

public class DueSelector implements HighlightSelector {
    @Override
    public Idea select(List<Idea> ideas) {
        Idea best = null;
        LocalDate earliestDue = null;
        for (Idea idea : ideas) {
            LocalDate due = idea.getDueDate();
            if (due == null) {
                continue;
            }
            if (best == null || due.isBefore(earliestDue)) {
                best = idea;
                earliestDue = due;
            }
        }

        return best;
    }
}
