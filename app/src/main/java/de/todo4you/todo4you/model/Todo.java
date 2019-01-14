package de.todo4you.todo4you.model;

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Status;

import java.time.LocalDate;

import de.todo4you.todo4you.util.StandardDates;

public class Todo {
    //public static final Logger logger = LoggerFactory.getLogger(Todo.class);
    private static final String PROP_X_TODO4YOU_STARS = "x-todo4you-stars";
    private static final String PROP_X_TODO4YOU_FAVORITE = "x-todo4you-favorite";

    final VToDo vtodo;
    volatile CompletionState completionState = CompletionState.NEEDS_ACTION;
    volatile int stars = 0;
    volatile boolean favorite = false;
    volatile boolean dirty = false;

    public Todo(VToDo vtodo) {
        this.vtodo = vtodo;
        this.completionState = fromLibToModel(vtodo.getStatus());

        PropertyList properties = vtodo.getProperties();
        stars = fromIntPropertyToModel(properties.getProperty(PROP_X_TODO4YOU_STARS), 0, 5, 0);
        favorite = fromBooleanPropertyToModel(properties.getProperty(PROP_X_TODO4YOU_FAVORITE), false);
    }

    private boolean fromBooleanPropertyToModel(Property property, boolean defaultValue) {
        if (property == null) {
            return defaultValue;
        }
        try {
            String val = property.getValue();
            boolean value = Boolean.parseBoolean(val);
            return value;
        } catch (Exception exc) {
          //  logger.warn("Invalid boolean property {}: {}. Using default value {}", property.getName(), property.getValue(), defaultValue, exc);
            return defaultValue;
        }
    }

    private int fromIntPropertyToModel(Property property, int min, int max, int defaultValue) {
        if (property == null) {
            return defaultValue;
        }
        try {
            String val = property.getValue();
            int value = Integer.parseInt(val);
            if (value < min || value > max) {
                throw new IllegalArgumentException("value " + val + "is out of range. min=" + min + ", max=max");
            }
            return value;
        } catch (Exception exc) {
          //  logger.warn("Invalid int property {}: {}. Using default value {}", property.getName(), property.getValue(), defaultValue, exc);
            return defaultValue;
        }
    }

    private CompletionState fromLibToModel(Status status) {
        // Hint: Status is an optional field, thus we set the internal state to
        // CompletionState.UNDEFINED if it cannot be determined by the given status
        if (status == null) {
            return CompletionState.UNDEFINED;
        }
        final String statusValue = vtodo.getStatus().getValue();
        switch (statusValue) {
            case "COMPLETED":
                return CompletionState.COMPLETED;
            case "IN-PROCESS":
                return CompletionState.IN_PROCESS;
            case "NEEDS-ACTION":
                return CompletionState.NEEDS_ACTION;
            case "CANCELLED":
                return CompletionState.CANCELLED;
            // Hint: Status is an optional field, thus
            default:
                return CompletionState.UNDEFINED;
        }
    }

    public String getSummary() {
        return vtodo.getSummary().getValue();
    }

    public CompletionState getCompletionState() {
        return completionState;
    }

    public void setCompletionState(CompletionState completionState) {
        if (completionState != this.completionState) {
            this.completionState = completionState;
            this.dirty = true;
        }
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        if (stars != this.stars) {
            this.stars = stars;
            this.dirty = true;
        }
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        if (favorite != this.favorite) {
            this.favorite = favorite;
            this.dirty = true;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public VToDo vtodo() {
        return vtodo;
    }

    public LocalDate getDueDate() {
        Due due = vtodo.getDue();
        if (due == null) {
            return null;
        }
        return StandardDates.dateToLocalDate(due.getDate());
    }

    public LocalDate getStartDate() {
        DtStart startDate = vtodo.getStartDate();
        if (startDate == null) {
            return null;
        }
        return StandardDates.dateToLocalDate(startDate.getDate());
    }

    /**
     * Returns the date when the user should take attention. This is the earlier date of
     * the due date and start date. In the first case the user could work on it, on the latter
     * the user must work on it. Both require attention, and that explains the method name.
     * @return the attention date
     */
    public LocalDate getAttentionDate() {
        LocalDate dd = getDueDate();
        LocalDate sd = getStartDate();
        if (dd == null && sd == null) {
            return null; // no attention required
        }
        if (dd != null) {
            return dd;
        }
        if (sd != null) {
            return sd;
        }

        // both dates set => return the earlier one.
        return dd.isAfter(sd) ? sd : dd;
    }


    @Override
    public String toString() {
        return getSummary() + ", dirty=" + dirty;
    }
}
