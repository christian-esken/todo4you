package de.todo4you.todo4you.caldav;

import org.osaf.caldav4j.exceptions.CalDAV4JException;

import java.time.LocalDate;
import java.util.List;

import de.todo4you.todo4you.model.Todo;

public interface CalendarConnector {
    List<Todo> get(LocalDate fromDate, LocalDate toDate, boolean onlyActive) throws Exception;
    boolean add(Todo task) throws CalDAV4JException;
    ConnectionParameters probe(ConnectionParameters connectionParameters);
}
