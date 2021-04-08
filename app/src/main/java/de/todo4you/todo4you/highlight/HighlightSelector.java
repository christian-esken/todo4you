package de.todo4you.todo4you.highlight;

import java.util.List;

import de.todo4you.todo4you.model.Idea;

public interface HighlightSelector {
    Idea select(List<Idea> ideas);
}
