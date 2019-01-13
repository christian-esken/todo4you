package de.todo4you.todo4you.tasks;

import java.time.LocalDate;
import java.time.LocalTime;

public class Task {
    final String id;
    String name;
    LocalDate date;
    LocalTime time;

    public Task(String id) {
        this(id, "", LocalDate.now(), LocalTime.now());
    }

    public Task(String id, String name, LocalDate date, LocalTime time) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
