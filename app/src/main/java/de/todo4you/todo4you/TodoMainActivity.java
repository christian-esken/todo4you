package de.todo4you.todo4you;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.todo4you.todo4you.highlight.DueSelector;
import de.todo4you.todo4you.highlight.HighlightSelector;
import de.todo4you.todo4you.highlight.RandomSelector;
import de.todo4you.todo4you.highlight.ShortCircuitChainedSelector;
import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.tasks.StoreResult;
import de.todo4you.todo4you.tasks.StoreState;
import de.todo4you.todo4you.tasks.TaskStore;
import de.todo4you.todo4you.tasks.comparator.StandardTodoComparator;
import de.todo4you.todo4you.util.StandardDates;

public class TodoMainActivity extends RefreshableActivity implements AdapterView.OnItemClickListener {
    static String NO_TASKS_MESSAGE = "Add your first idea with the + button";

    ListView taskListView = null;
    ArrayAdapter<String> tasklistAdapter = null;
    TextView highlightedTextView = null;
    TextView highlightedInfoTextView = null;
    SwipeRefreshLayout pullToRefresh = null;
    private HighlightSelector highlightSelector;
    private volatile Todo userHighlightedTodo;
    private TextView highlightedInfoDescTextView;

    public enum ActionType {
        NONE,
        START, // start time reached
        DUE, // due date today
        OVERDUE // due date already in the past
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        highlightSelector = new ShortCircuitChainedSelector(new DueSelector(), new RandomSelector());

        String[] messages = {"Loading Tasks" };
        List<String> msgList = new ArrayList<>(Arrays.asList(messages));
        tasklistAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, msgList);


        taskListView = findViewById(R.id.listView);
        taskListView.setAdapter(tasklistAdapter);

        highlightedTextView = findViewById(R.id.highlightedTask);
        highlightedInfoTextView = findViewById(R.id.highlightedTaskInfo);

        highlightedInfoDescTextView = findViewById(R.id.highlightedTaskDesc);

        taskListView.setItemsCanFocus(true);
        taskListView.setOnItemClickListener(this);

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                               int refreshcounter = 1; //Counting how many times user have refreshed the layout

                                               @Override
                                               public void onRefresh() {
                                                   refreshcounter++;
                                                   TaskStore.instance().refresh();
                                               }
                                           });

        runOnUiThread(() -> pullToRefresh.setRefreshing(true));


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_task_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(TodoMainActivity.this, AddTask.class);
                myIntent.putExtra("key", "value"); //Optional parameters
                TodoMainActivity.this.startActivity(myIntent);
/*                Snackbar.make(view, "Add task hover", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
            }
        });

        ImageButton quicksortButton = findViewById(R.id.quickSortLaunchButton);
        quicksortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(TodoMainActivity.this, QuickSortActivity.class);
                if (highlightedTextView == null) {
                    Snackbar.make(view, "Please slect a task before editing.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    myIntent.putExtra("taskRef", highlightedTextView.getText()); //Optional parameters
                    TodoMainActivity.this.startActivity(myIntent);
                }
            }
        });

        ImageButton highlightLikeButton = findViewById(R.id.highlightLike);
        highlightLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(TodoMainActivity.this, OneTaskActivity.class);
                if (highlightedTextView == null) {
                    Snackbar.make(view, "Please select a task before switching to one-task mode.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    myIntent.putExtra("taskRef", highlightedTextView.getText()); //Optional parameters
                    TodoMainActivity.this.startActivity(myIntent);
                }
            }
        });


        // TaskStore listener should be created at the end. It requires that all initialization of
        // this object has completed, e.g. the references to the GUI components have been resolved.
        TaskStore taskStore = taskStore();
        taskStore.registerListener(this);
        update(taskStore.getAll()); // immediate update, e.g. to show "loading"
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        // When doing the add task activity, this onStop() is called. We do not shut down our
        // TaskSelector, so it can continue to listen on changed tasks, e.g. via the AddTask
        // activity or any modifying activities (title, due date, ...).
        /*
        TaskSelector tsRef = ts;
        if (tsRef != null)
            tsRef.shutdownNow();
            */
        super.onStop();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object itemAtPositionTaskSummary = parent.getItemAtPosition(position);
        // findBySummary() is hacky. Two entries could have the same summary. We need
        // full tasks in the taskListView (or at least a key/reference)
        Todo todo = taskStore().findBySummary(itemAtPositionTaskSummary);
        taskStore().setHighlightTodo(todo);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todo_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public SwipeRefreshLayout pullToRefresh() {
        return pullToRefresh;
    }

    private void fullRefresh(Todo thl, String[] newMessages) {
        runOnUiThread(() -> {
            String prefixMessage = StandardDates.localDateToReadableString(thl.getAttentionDate());
            highlightedTextView.setText(thl.getSummary());
            highlightedInfoTextView.setText(prefixMessage);
            highlightedTextView.setBackgroundColor(Color.LTGRAY);

            highlightedInfoDescTextView.setText(thl.getDescription());

            ActionType actionType = determineAction(thl);
            int color = Color.LTGRAY;
            if (actionType == ActionType.DUE) {
                color = Color.YELLOW;
            } else if (actionType == ActionType.OVERDUE) {
                color = Color.RED;
            }
            highlightedInfoTextView.setBackgroundColor(color);

            if (newMessages != null) {
                tasklistAdapter.clear();
                tasklistAdapter.addAll(newMessages);
            }
            pullToRefresh().setRefreshing(false);
        });
    }

    private void updateHighlightView(String statusMessage) {
        highlightedTextView.setText(statusMessage);
        highlightedInfoTextView.setText("");
        highlightedInfoDescTextView.setText("");
    }

    private TaskStore taskStore() {
        return TaskStore.instance();
    }

    @Override
    public void update(StoreResult storeResult) {
        // TODO currentPollHasIssues should warn the user if error remains for a longer time.
        boolean currentPollHasIssues = storeResult.getStatus() == StoreState.ERROR;

        Todo todoHighlight = null;
        List<Todo> todos = storeResult.getTodos();

        if (todos.isEmpty()) {
            // For now, handle errors only if we never load tasks successfully
            switch (storeResult.getStatus()) {
                case LOADING:
                    updateHighlightView("LOADING");
                    break;
                case ERROR:
                    updateHighlightView("ERROR");
                    break;
                default:
                    // LOADED and EMPTY
                    updateHighlightView(NO_TASKS_MESSAGE);
                    break;
            }
        } else {
            // There are todos
            todos = sortTodos(todos);
            String[] newMessages = new String[todos.size()];
            for (int i = 0; i < todos.size(); i++) {
                Todo todo = todos.get(i);
                ActionType actionType = determineAction(todo);
                final String duePrefix;
                if (actionType != ActionType.NONE) {
                    duePrefix = " (" + actionTypeToText(actionType) + ")";
                } else {
                    duePrefix = " (" + determineWhenToDo(todo) + ")";
                }
                newMessages[i] = todo.getSummary() + duePrefix;
            }

            if (userHighlightedTodo != null)
                todoHighlight = userHighlightedTodo; // already selected
            else
                todoHighlight = highlightSelector.select(todos); // auto-select

            fullRefresh(todoHighlight, newMessages);
        }


        //adapter.notifyDataSetChanged();

    }

    @Override
    public void updateHighlight(Todo todo) {
        runOnUiThread(() -> {
            if (todo != null) {
                userHighlightedTodo = todo;
                fullRefresh(todo, null);
            }
        });
    }

    public static ActionType determineAction(Todo todo) {
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


    private static String actionTypeToText(ActionType actionType) {
        if (actionType == null) {
            return ActionType.NONE.name();
        }
        return actionType.name();
    }


    // Helper method. Can be moved
    public static List<Todo> sortTodos(List<Todo> todos) {
        Todo[] todosArray = todos.toArray(new Todo[todos.size()]);
        Arrays.sort(todosArray, new StandardTodoComparator());

        return Arrays.asList(todosArray);
    }


    private static String determineWhenToDo(Todo todo) {
        LocalDate attentionDate = todo.getAttentionDate();
        if (attentionDate != null) {
            return StandardDates.dateToName(attentionDate).toString();
        }

        return "UNSCHEDULED";
    }

}
