package de.todo4you.todo4you.util;

import de.todo4you.todo4you.model.Idea;
import de.todo4you.todo4you.tasks.StoreResult;

public interface StoreUpdateNotifier {
    void update(StoreResult storeResult);
    void updateHighlight(Idea idea);
}
