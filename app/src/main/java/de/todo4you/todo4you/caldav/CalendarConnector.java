package de.todo4you.todo4you.caldav;

import java.time.LocalDate;
import java.util.List;

import de.todo4you.todo4you.model.Todo;

public interface CalendarConnector {
    List<Todo> get(LocalDate fromDate, LocalDate toDate, boolean onlyActive) throws Exception;
    ConnectionParameters probe(ConnectionParameters connectionParameters);
}
