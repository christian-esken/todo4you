package de.todo4you.todo4you.util;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static de.todo4you.todo4you.util.StandardDates.Name.*;
import static java.time.temporal.ChronoUnit.DAYS;

public class StandardDates {
    static ZoneId userZone = ZoneId.systemDefault();
    static long DUE_SOON_DAYS = 7;

    /**
     * Compares dates null-safe. A date that is null is considered to be very far in the future
     * and thus after the other date. The reason for this are user expectations: Lets presume that a
     * user has not set a due date or start date on the first Todo, but on the second he has. Then he
     * likely wants to sort see the second todo before the first one.
     *
     * @param ld1 date 1
     * @param ld2 date 2
     * @return -1 if ld1 is before ld2, +1 if it is after ld2. See method description for null handling.
     */
    public static int compare(LocalDate ld1, LocalDate ld2) {
        if (ld1 == null && ld2 == null) {
            return 0;
        }
        if (ld1 == null && ld2 != null) {
            return 1;
        }
        if (ld1 != null && ld2 == null) {
            return -1;
        }
        return ld1.compareTo(ld2);
    }

    public enum Name {
        OVERDUE(true), YESTERDAY(true), TODAY(true), TOMORROW, NEXT_WEEKEND, NEXT_WEEK, THIS_MONTH, NEXT_MONTH, FAR_FUTURE, UNSCHEDULED;

        private final boolean isDue;

        Name() {
            this.isDue = false;
        }

        Name(boolean isDue) {
            this.isDue = isDue;
        }

        public boolean isDue() {
            return isDue;
        }
    }

    /**
     * Null safe is due soon check
     * @param ld
     * @return
     */
    public static boolean isDueSoon(LocalDate ld) {
        if (ld == null)
            return false;
        return dayDiff(ld) <= DUE_SOON_DAYS;
    }

    public static LocalDate dateToLocalDate(Date date) {
        return instantToLocalDate(date.toInstant());
    }

    public static LocalDate epochToLocalDate(long dueEpoch) {
        return instantToLocalDate(Instant.ofEpochMilli(dueEpoch));
    }

    /**
     * Converts an Instant to the LocalDate using the {@link #userZone} time zone.
     * @param instant Instant
     * @return LocalDate
     */
     static LocalDate instantToLocalDate(Instant instant) {
        return instant.atZone(userZone).toLocalDate();
    }


    public static long dayDiff(LocalDate refDate) {
        return now().until(refDate, DAYS);
    }

    public static Name dateToName(LocalDate refDate) {
        return localDateToName(refDate);
    }

    /**
     * Returns LocalDate using the user time zone.
     * @return now in user time zone
     */
    public static LocalDate now() {
         return LocalDate.now(userZone);
    }

    /**
     * Returns LocalDateTime using the user time zone.
     * @return now in user time zone
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now(userZone);
    }

    public static String localDateToReadableString(LocalDate refDate) {
        if (refDate == null) {
            return UNSCHEDULED.name();
        }
        LocalDate today = now();
        long daysUntil = today.until(refDate, DAYS);
        if (daysUntil < -1) {
            return OVERDUE.name() + " since " + (-daysUntil) + " days";
        }
        if (daysUntil == -1) {
            return Name.YESTERDAY.name();
        }
        if (daysUntil == 0) {
            return Name.TODAY.name();
        }
        if (daysUntil == 1) {
            return Name.TOMORROW.name();
        }
        if (daysUntil < 6) {
            return refDate.getDayOfWeek().name() + "( " + daysUntil + " days)";
        }

        return "In " + daysUntil + " days";
    }

    /**
     * Converts the
     * @param refDate
     * @return
     */
    public static Name localDateToName(LocalDate refDate) {
        if (refDate == null) {
            return UNSCHEDULED;
        }
        LocalDate today = now();
        long daysUntil = today.until(refDate, DAYS);

        if (daysUntil < -1) {
            return OVERDUE;
        }
        if (daysUntil == -1) {
            return Name.YESTERDAY;
        }
        if (daysUntil == 0) {
            return Name.TODAY;
        }
        if (daysUntil == 1) {
            return Name.TOMORROW;
        }

        if (daysUntil < 7) {
            DayOfWeek dayOfWeek = refDate.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                return NEXT_WEEKEND;
            }
        }

        if (daysUntil < 14) {
            return NEXT_WEEK;
        }

        if (daysUntil < 31) {
            return THIS_MONTH;
        }

        if (daysUntil < 62) {
            return NEXT_MONTH;
        }

        return FAR_FUTURE;
    }

}
