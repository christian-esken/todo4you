package de.todo4you.todo4you.tasks;

import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import de.todo4you.todo4you.TodoMainActivity;
import de.todo4you.todo4you.highlight.DueSelector;
import de.todo4you.todo4you.highlight.HighlightSelector;
import de.todo4you.todo4you.highlight.RandomSelector;
import de.todo4you.todo4you.highlight.ShortCircuitChainedSelector;
import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.tasks.comparator.StandardTodoComparator;
import de.todo4you.todo4you.util.StandardDates;
import de.todo4you.todo4you.util.StoreUpdateNotifier;

public class TaskSelector implements StoreUpdateNotifier {
    private final TodoMainActivity activity;
    private final HighlightSelector highlightSelector;
    private volatile Todo userHighlightedTodo;

    static String NO_TASKS_MESSAGE = "No tasks. Add one with the + button";

    public void shutdownNow() {
        taskStore().unregisterListener(this);
    }

    public void highlightTask(Object taskDescription) {
        // findBySummary() is hacky. Two entries could have the same summary. We need
        // full tasks in the taskListView (or at least a key/reference)
        userHighlightedTodo = taskStore().findBySummary(taskDescription);
        refreshHighlightArea(userHighlightedTodo);
    }

    private TaskStore taskStore() {
        return TaskStore.instance();
    }

    public enum ActionType {
        NONE,
        START, // start time reached
        DUE, // due date today
        OVERDUE // due date already in the past
    }

    public TaskSelector(TodoMainActivity activity) {
        this.activity = activity;
        highlightSelector = new ShortCircuitChainedSelector(new DueSelector(), new RandomSelector());
        TaskStore taskStore = taskStore();
        taskStore.registerListener(this);
        update(taskStore.getAll());
    }


    @Override
    public void update(StoreResult storeResult) {
        // TODO currentPollHasIssues should warn the user if error remains for a longer time.
        boolean currentPollHasIssues = storeResult.getStatus() == StoreState.ERROR;

        String[] newMessages;
        Todo todoHighlight = null;
        List<Todo> todos = storeResult.getTodos();

        if (todos.isEmpty()) {
            // For now, handle errors only if we never load tasks successfully
            switch (storeResult.getStatus()) {
                case LOADING:
                    newMessages = new String[1];
                    newMessages[0] = "Loading tasks ...";
                    break;
                case ERROR:
                    newMessages = new String[1];
                    newMessages[0] = "Error: " + storeResult.getUserErrorMessaage();
                    break;
                default:
                    // LOADED and EMPTY
                    newMessages = new String[1];
                    newMessages[0] = NO_TASKS_MESSAGE;
                    break;
            }
        } else {
            // There are todos
            todos = sortTodos(todos);
            newMessages = new String[todos.size()];
            for (int i = 0; i < todos.size(); i++) {
                Todo todo = todos.get(i);
                ActionType actionType = determineAction(todo);
                String duePrefix = "";
                if (actionType != ActionType.NONE) {
                    duePrefix = " (" + actionTypeToText(actionType) + ")";
                } else {
                    duePrefix = " (" + determineWhenToDo(todo) + ")";
                }
                newMessages[i] = todo.getSummary() + duePrefix;
            }

            if (userHighlightedTodo != null)
                todoHighlight = userHighlightedTodo;
            else
                todoHighlight = highlightSelector.select(todos);
        }


        refreshCompleteArea(todoHighlight, newMessages);
        //adapter.notifyDataSetChanged();
    }

    private void refreshHighlightArea(final Todo todoHighlight) {
        refreshCompleteArea(todoHighlight, null);
    }

    private void refreshCompleteArea(final Todo todoHighlight, String[] newMessages) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.fullRefresh(todoHighlight, newMessages);
            }
        });
    }

    // Helper method. Can be moved
    public static List<Todo> sortTodos(List<Todo> todos) {
        Todo[] todosArray = todos.toArray(new Todo[todos.size()]);
        Arrays.sort(todosArray, new StandardTodoComparator());

        return Arrays.asList(todosArray);
    }

    private String actionTypeToText(ActionType actionType) {
        if (actionType == null) {
            return ActionType.NONE.name();
        }
        return actionType.name();
    }

    private String determineWhenToDo(Todo todo) {
        LocalDate attentionDate = todo.getAttentionDate();
        if (attentionDate != null) {
            return StandardDates.dateToName(attentionDate).toString();
        }

        return "UNSCHEDULED";
    }

    public static TaskSelector.ActionType determineAction(Todo todo) {
        StandardDates.Name dueName = StandardDates.dateToName(todo.getDueDate());
        if (dueName.isDue()) {
            return dueName == StandardDates.Name.OVERDUE ? TaskSelector.ActionType.OVERDUE : TaskSelector.ActionType.DUE;
        } else {
            StandardDates.Name startName = StandardDates.dateToName(todo.getStartDate());
            if (startName.isDue()) {
                return TaskSelector.ActionType.START;
            }
        }
        return TaskSelector.ActionType.NONE;
    }
}
