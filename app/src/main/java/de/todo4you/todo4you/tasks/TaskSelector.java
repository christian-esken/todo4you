package de.todo4you.todo4you.tasks;

import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.todo4you.todo4you.TodoMainActivity;
import de.todo4you.todo4you.highlight.DueSelector;
import de.todo4you.todo4you.highlight.HighlightSelector;
import de.todo4you.todo4you.highlight.RandomSelector;
import de.todo4you.todo4you.highlight.ShortCircuitChainedSelector;
import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.util.StandardDates;

public class TaskSelector extends Thread {
    private final TodoMainActivity activity;
    private final HighlightSelector highlightSelector;
    private volatile Todo userHighlightedTodo;
    private volatile boolean running = true;

    public void shutdownNow() {
        this.running = false;
        this.interrupt();
    }

    public void highlightTask(Object taskDescription) {
        // findBySummary() is hacky. Two entries could have the same summary. We need
        // full tasks in the taskListView (or at least a key/reference)
        userHighlightedTodo = TaskDAO.instance().findBySummary(taskDescription);
        refreshHighlightArea(userHighlightedTodo);
    }

    private void refreshHighlightView() {
        this.interrupt();
    }

    enum ActionType {
        NONE,
        START, // start time reached
        DUE, // due date today
        OVERDUE // due date already in the past
    };

    public TaskSelector(TodoMainActivity activity) {
        this.activity = activity;

        highlightSelector = new ShortCircuitChainedSelector(new DueSelector(), new RandomSelector());
    }

    @Override
    public void run() {
        while (running) {
            List<Todo> todos = null;
            String error = "null";
            try {
                todos = TaskDAO.instance().getWithRefresh();
                todos = sortTodos(todos);
            } catch (Exception e) {
                error = e.getMessage();
            }

            Todo todoHighlight = null;
            final String[] newMessages;
            if (todos == null) {
                newMessages = new String[1];
                newMessages[0] = "Error: " + error;
            } else {
                newMessages = new String[todos.size()];
                for (int i=0; i<todos.size(); i++) {
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

            // Interruption policy: continue, and let the loop detrmine the "running" flag
            if (this.isInterrupted()) {
                continue;
            }
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                continue;
            }

            //adapter.notifyDataSetChanged();
        }
    }

    private void refreshHighlightArea(final Todo todoHighlight) {
        refreshCompleteArea(todoHighlight, null);
    }
    private void refreshCompleteArea(final Todo todoHighlight, String[] newMessages) {
        Todo thl = todoHighlight;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ArrayAdapter<String> adapter = activity.getTaskListViewAdapter();
                final TextView highlightedTextView = activity.getHighlightedTextView();
                final TextView highlightedInfoTextView = activity.getHighlightedInfoTextView();
                if (thl != null) {
                    ActionType actionType = determineAction(thl);
                    String prefixMessage = actionType == ActionType.NONE ?  "" : " | " + actionType.toString();
                    highlightedTextView.setText(thl.getSummary());
                    highlightedInfoTextView.setText(thl.getCompletionState() + prefixMessage + " | Stars: " + thl.getStars());
                    highlightedTextView.setBackgroundColor(Color.LTGRAY);
                    int color = Color.LTGRAY;
                    if (actionType == ActionType.DUE) {
                        color = Color.YELLOW;
                    } else if (actionType == ActionType.OVERDUE) {
                        color = Color.RED;
                    }
                    highlightedInfoTextView.setBackgroundColor(color);
                } else {
                    highlightedTextView.setText("No tasks. Add one with the + button");
                    highlightedInfoTextView.setText("");
                    highlightedTextView.setBackgroundColor(0xFF666666);
                    highlightedInfoTextView.setBackgroundColor(0xFF666666);
                }

                if (newMessages != null) {
                    adapter.clear();
                    adapter.addAll(newMessages);
                }
            }
        });
    }

    static class StandardTodoComparator implements Comparator<Todo> {

        NeeedsActionSoonComparator neeedsActionSoonComparator = new NeeedsActionSoonComparator();
        DateBasedTodoComparator dateBasedTodoComparator = new DateBasedTodoComparator();
        @Override
        public int compare(Todo o1, Todo o2) {
            int compare = neeedsActionSoonComparator.compare(o1, o2);
            if (compare != 0)
                return compare;

            return dateBasedTodoComparator.compare(o1, o2);
        }
    }

    /**
     * Sorts anything that is can or should be worked on to the front. This means, everything that
     * is due, soon due, or can be started now or soon. Anything that is not due soon is sorted
     * to the end. Please note that this comparator treats due and start dates equal.
     */
    static class NeeedsActionSoonComparator implements Comparator<Todo> {
        @Override
        public int compare(Todo o1, Todo o2) {
            LocalDate actionDate1 = o1.getDueDate();
            LocalDate sd1 = o1.getStartDate();
            if (actionDate1 == null || StandardDates.compare(sd1, actionDate1) < 0) {
                // If there is no due date or the start date is before it, then pick it
                actionDate1 = o1.getStartDate();
            }
            LocalDate actionDate2 = o2.getDueDate();
            LocalDate sd2 = o2.getStartDate();
            if (actionDate2 == null || StandardDates.compare(sd2, actionDate2) < 0) {
                // If there is no due date or the start date is before it, then pick it
                actionDate2 = o2.getStartDate();
            }

            if (actionDate1 == null && actionDate2 == null) {
                return 0; // Fast-path: Both TO DO entries have no dates set
            }

            boolean dueSoon1 = StandardDates.isDueSoon(actionDate1);
            boolean dueSoon2 = StandardDates.isDueSoon(actionDate2);
            if (dueSoon1 ^ dueSoon2) {
                // Only one is due soon
                return dueSoon1 ? -1 : 1;
            }

            // None is due soon
            return 0;
        }
    }

    /**
     * Sort entries with due date to the front, after it the entries with start date.
     */
    static class DateBasedTodoComparator implements Comparator<Todo> {
        @Override
        public int compare(Todo o1, Todo o2) {
            int compare = StandardDates.compare(o1.getDueDate(), o2.getDueDate());
            if (compare != 0) {
                return compare;
            }

            compare = StandardDates.compare(o1.getStartDate(), o2.getStartDate());
            if (compare != 0) {
                return compare;
            }
            return 0;
        }
    }

    private List<Todo> sortTodos(List<Todo> todos) {
        Todo[] todosArray = todos.toArray(new Todo[0]);
        Arrays.sort(todosArray, new StandardTodoComparator());

        return Arrays.asList(todosArray);
    }

    private String actionTypeToText(ActionType actionType) {
        if (actionType == null) {
            return ActionType.NONE.name();
        }
        return actionType.name();
    }

    private ActionType determineAction(Todo todo) {
        StandardDates.Name dueName = StandardDates.dateToName(todo.getDueDate());
        if (dueName.isDue()) {
            return dueName == StandardDates.Name.OVERDUE ? ActionType.OVERDUE : ActionType.DUE;
        } else {
            StandardDates.Name startName = StandardDates.dateToName(todo.getStartDate());
            if (startName.isDue()) {
                return ActionType.START;
            }
        }
        return ActionType.NONE;
    }


    private String determineWhenToDo(Todo todo) {
        LocalDate attentionDate = todo.getAttentionDate();
        if (attentionDate != null) {
            return StandardDates.dateToName(attentionDate).toString();
        }

        return "UNSCHEDULED";
    }

}
