package de.todo4you.todo4you;

import android.support.annotation.Nullable;

import de.todo4you.todo4you.model.Todo;

public interface RefreshTasksCallback {
    /**
     * Called when tasks need to be refreshed. The implementation should replace all
     * messages with #newMessages. If it supports highlighting a task, it should highlight
     * #todoHighlight. If #todoHighlight is null, the implementation can choose to unhighlight or
     * keep the old highlighted Task.
     *
     * @param todoHighlight The highlighted task. Can be null
     * @param newMessages The full collection of new messages
     */
    void fullRefresh(@Nullable Todo todoHighlight, String[] newMessages);
}
