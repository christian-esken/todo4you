package de.todo4you.todo4you.model;

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.Function;

import de.todo4you.todo4you.storage.DataOrigin;
import de.todo4you.todo4you.util.StandardDates;
import de.todo4you.todo4you.util.UidFactory;

/**
 * The internal model for an Idea. It holds all information like title, description, status and
 * associated dates.
 * TODO The model works with dates only, discarding the time component. The time component is
 * usually preserved, but gets lost when this Todo is persisted (to the calendar) after
 * modification from the user.
 */
public class Idea {
    //public static final Logger logger = LoggerFactory.getLogger(Todo.class);
    private static final String PROP_X_TODO4YOU_STARS = "x-todo4you-stars";
    private static final String PROP_X_TODO4YOU_FAVORITE = "x-todo4you-favorite";

    volatile VToDo vtodo; // Hardcoded to ical4j at the moment

    private final String uid; // An UID should never change => final!
    volatile String summary;
    volatile String description;
    volatile CompletionState completionState = CompletionState.NEW;
    volatile int stars = 0;
    volatile boolean favorite = false;
    volatile SyncState syncState = null; // MUST be initialized in all constructors!
    LocalDate startDate = null;
    LocalDate dueDate = null;
    LocalDate completionDate = null;
    volatile boolean dirty = false;

    /**
     * Creates an instance from a backing VTodo. The VTodo is converted into the internal model.
     *
     * @param vtodo The backing VTodo
     */
    public Idea(VToDo vtodo, DataOrigin dataOrigin) {
        this.vtodo = vtodo;
        this.completionState = fromLibToModel(vtodo.getStatus());

        PropertyList properties = vtodo.getProperties();
        stars = fromIntPropertyToModel(properties.getProperty(PROP_X_TODO4YOU_STARS), 0, 5, 0);
        favorite = fromBooleanPropertyToModel(properties.getProperty(PROP_X_TODO4YOU_FAVORITE), false);
        summary = fromStringPropertyToModel(vtodo.getSummary(), null);
        description = fromStringPropertyToModel(vtodo.getDescription(), null);
        Uid uidFromVtodo = vtodo.getUid();
        if (uidFromVtodo == null) {
            uid = UidFactory.instance().produceNewUid();
        } else {
            uid = uidFromVtodo.getValue();
        }

        DtStart start = vtodo.getStartDate();
        this.startDate = startDate == null ? null : StandardDates.dateToLocalDate(start.getDate());
        Due due = vtodo.getDue();
        this.dueDate = due == null ? null : StandardDates.dateToLocalDate(due.getDate());
        Completed completed = vtodo.getDateCompleted();
        this.completionDate = completed == null ? null : StandardDates.dateToLocalDate(completed.getDate());
        syncState = SyncState.buildFrom(dataOrigin);
    }

    /**
     * Creates a new instance without backing VTodo. The latter will be created on an upstream sync.
     */
    public Idea(String summary) {
        this.summary = summary;
        vtodo = null; // invalid
        this.completionState = CompletionState.NEW;
        uid = UidFactory.instance().produceNewUid();

        stars = 0;
        favorite = false;
        dirty = true;
        syncState = SyncState.buildNew();
    }

    /**
     * Creates a new instance without backing VTodo. The latter will be created on an upstream sync.
     */
    public Idea (String uid, String summary, String description, String completionState, boolean needsCloudSync) {
        this.uid = uid;
        this.description = description;
        this.summary = summary;
        this.completionState = CompletionState.valueOfOrDefault(completionState, CompletionState.UNDEFINED);
        vtodo = null; // invalid

        stars = 0;
        favorite = false;
        dirty = needsCloudSync;
        syncState = SyncState.buildFrom(DataOrigin.DeviceStore);
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


    private String fromStringPropertyToModel(Property property, String defaultValue) {
        if (property == null) {
            return defaultValue;
        }

        String value = property.getValue();
        return value == null ? defaultValue : value;
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
                return CompletionState.IN_PROGRESS;
            case "NEEDS-ACTION":
                return CompletionState.NEW;
            case "CANCELLED":
                return CompletionState.CANCELLED;
            // Hint: Status is an optional field, thus
            default:
                return CompletionState.UNDEFINED;
        }
    }

    public String getSummary() {
        return summary == null ? "" : summary;
    }


    public String getDescription() {
        return description == null ? "" : description;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    /**
     * Returns the date when the user should take attention. This is the earlier date of
     * the due date and start date. In the first case the user could work on it, on the latter
     * the user must work on it. Both require attention, and that explains the method name.
     *
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

    /**
     * Updates the backing VTodo, creating it if it does not exist yet. Returns if the backing
     * VTodo was modified by this method. Normally this is the case when {@link #dirty} is true,
     * but in case of reverts (A-B-A-change) this method can return false. The {@link #dirty} flag
     * will also be cleared in that case.
     *
     * @return If the backing VTodo was modified by this method.
     */
    public boolean updateVTodoFromModel() {
        boolean reallyModified = false;

        if (vtodo == null) {
            vtodo = new VToDo();
        }
        try {
            reallyModified |= compareAndSetStringProperty(vtodo, vtodo.getUid(), uid, cons -> new Uid(cons));
            reallyModified |= compareAndSetStringProperty(vtodo, vtodo.getSummary(), summary, cons -> new Summary(cons));
            reallyModified |= compareAndSetStringProperty(vtodo, vtodo.getDescription(), description, cons -> new Description(cons));

            reallyModified |= compareAndSetDateProperty(vtodo, vtodo.getDue(), dueDate, cons -> new Due(cons));
            reallyModified |= compareAndSetDateProperty(vtodo, vtodo.getStartDate(), startDate, cons -> new DtStart(cons));

            //Completed requires a DateTime, so we cannot use the  compareAndSetDateProperty()
            //reallyModified |= compareAndSetDateProperty(vtodo, vtodo.getDateCompleted(), completionDate, (cons) -> new Completed(cons));


            // TODO Continue with the other props
            /*
            volatile CompletionState completionState = CompletionState.NEEDS_ACTION;
            volatile int stars = 0;
            volatile boolean favorite = false;
            volatile TodoState todoState = TodoState.UNINITIALIZED;
            LocalDate completionDate = null;
*/

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        dirty = reallyModified;
        syncState.insyncReset(!dirty);
        return reallyModified;
    }

    private boolean compareAndSetDateProperty(VToDo vtodo, DateProperty prop, LocalDate model, Function<net.fortuna.ical4j.model.Date, ? extends DateProperty> fn) throws ParseException, IOException, URISyntaxException {
        if (prop == null) {
            if (model == null) {
                return false; // Both not set => no change
            }
            // if model is set, but property does not exist yet => add it
            net.fortuna.ical4j.model.Date vdate = new net.fortuna.ical4j.model.Date(modelToDate(model));
            DateProperty newProperty = fn.apply(vdate);
            vtodo.getProperties().add(newProperty);
            return true;
        }


        String propValue = prop.getValue();
        if (propValue == null) {
            if (model == null) {
                return false; // both null. no change
            } else {
                net.fortuna.ical4j.model.Date vdate = new net.fortuna.ical4j.model.Date(modelToDate(model));
                prop.setDate(vdate);
                return true;
            }
        }

        // propValue != null here
        if (propValue.equals(model)) {
            return false; // both have the same value
        } else {
            // modified
            if (model == null) {
                vtodo.getProperties().remove(prop);
                return true; // model is null => remove property
            } else {
                net.fortuna.ical4j.model.Date vdate = new net.fortuna.ical4j.model.Date(modelToDate(model));
                prop.setDate(vdate);
                return true; // from x to y => changed
            }
        }
    }

    private Date modelToDate(LocalDate model) {
        Instant instant = model.atStartOfDay().toInstant(ZoneOffset.UTC);
        return new Date(instant.toEpochMilli());
    }

    private boolean compareAndSetStringProperty(VToDo vtodo, Property prop, String model, Function<String, ? extends Property> fn) throws ParseException, IOException, URISyntaxException {
        if (prop == null) {
            if (model == null) {
                return false; // Both not set => no change
            }
            // if model is set, but property does not exist yet => add it
            Property newProperty = fn.apply(model);
            vtodo.getProperties().add(newProperty);
            return true;
        }

        String propValue = prop.getValue();
        if (propValue == null) {
            if (model == null) {
                return false; // both null. no change
            } else {
                prop.setValue(model);
                return true;
            }
        }

        // propValue != null here
        if (propValue.equals(model)) {
            return false; // both have the same value
        } else {
            // modified
            if (model == null) {
                vtodo.getProperties().remove(prop);
                return true; // model is null => remove property
            } else {
                prop.setValue(model);
                return true; // from x to y => changed
            }
        }
    }

    @Override
    public String toString() {
        return getSummary() + ", dirty=" + dirty;
    }

    public String getUid() {
        return uid;
    }

    public void setDueDate(LocalDate now) {
        if (now == null && dueDate != null) {
            dueDate = null;
            this.dirty = true;
        }
        if (!now.equals(dueDate)) {
            dueDate = now;
            this.dirty = true;
        }
    }

    // Should not be public.
    public VToDo getInternalVtodo() {
        return vtodo;
    }

    public boolean isLocked() {
        // TOOD Implement actual entry locking. From a design perspective, the TaskStore must
        // hold all locked uid's. To lock: modified and new tasks.
        return dirty;
    }

    public boolean needsDeviceSync() {
        return !syncState.inSyncWithDeviceStore();
    }

    /**
     * TODO This method is probably superfluous, as {@link #updateVTodoFromModel()} already
     * checks for consistency with the backing VToDO.
     */
    public boolean needsCloudSync() {
        return !syncState.inSyncWithCloudStore();
    }

    public void setCloudSynced() {
        syncState.setInSyncWithCloudStore();
    }

    public void setDeviceSynced() {
        syncState.setInSyncWithDeviceStore();
    }
}
