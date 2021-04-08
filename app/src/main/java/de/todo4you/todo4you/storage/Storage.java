package de.todo4you.todo4you.storage;

import org.osaf.caldav4j.exceptions.CalDAV4JException;

import java.time.LocalDate;
import java.util.List;

import de.todo4you.todo4you.model.Idea;

public interface Storage extends AutoCloseable {
    List<Idea> get(LocalDate fromDate, LocalDate toDate, boolean onlyActive) throws Exception;
    Idea get(String uuid) throws CalDAV4JException;
    boolean add(Idea task) throws CalDAV4JException;
    boolean update(Idea task) throws CalDAV4JException;
}
