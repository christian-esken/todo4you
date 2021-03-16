package de.todo4you.todo4you.storage;

import org.osaf.caldav4j.exceptions.CalDAV4JException;

import java.time.LocalDate;
import java.util.List;

import de.todo4you.todo4you.model.Todo;

public interface Storage extends AutoCloseable {
    List<Todo> get(LocalDate fromDate, LocalDate toDate, boolean onlyActive) throws Exception;
    Todo get(String uuid) throws CalDAV4JException;
    boolean add(Todo task) throws CalDAV4JException;
    boolean update(Todo task) throws CalDAV4JException;
}
