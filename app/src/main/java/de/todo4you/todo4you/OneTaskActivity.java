package de.todo4you.todo4you;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.tasks.StoreResult;
import de.todo4you.todo4you.tasks.TaskStore;

public class OneTaskActivity extends RefreshableActivity {
    TextView highlightedTextView = null;
    TextView highlightedInfoTextView = null;
    TextView highlightedStatsView = null;

    Todo highlightedTodo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_one_task);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        highlightedTextView = findViewById(R.id.oneTitle);
        highlightedInfoTextView = findViewById(R.id.oneDetails);
        highlightedStatsView = findViewById(R.id.oneStatistics);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        TaskStore taskStore = taskStore();
        taskStore.registerListener(this);
        update(taskStore.getAll());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TaskStore taskStore = taskStore();
        taskStore.unregisterListener(this);
    }

    private TaskStore taskStore() {
        return TaskStore.instance();
    }

    private void updateView(String statusMessage) {
        highlightedTextView.setText(statusMessage);
        highlightedInfoTextView.setText("");
        highlightedStatsView.setText("");
    }

    private void updateView(Todo todo, List<Todo> todos) {
        updateHighlight(todo);
        int ideaCount = todos.size();
        if (ideaCount > 0) {
            highlightedStatsView.setText("You have " + (ideaCount - 1) + " more ideas");
        } else {
            highlightedStatsView.setText("Add an idea which you want to focus on later.");
        }
    }

    @Override
    public void updateHighlight(Todo todo) {
        runOnUiThread(() -> {
            if (todo != null) {
                highlightedTodo = todo;
                highlightedTextView.setText(highlightedTodo.getSummary());
                highlightedInfoTextView.setText(todoMessage(highlightedTodo));
            }
        });
    }

    private String todoMessage(Todo highlightedTodo) {

        String cr = System.lineSeparator();
        StringBuffer sb = new StringBuffer();
        sb.append("Start: ").append(highlightedTodo.getStartDate()).append(cr);
        sb.append("Due  : ").append(highlightedTodo.getDueDate()).append(cr);
        sb.append("Info : ").append(highlightedTodo.getDescription());
        return sb.toString();
    }

    @Override
    public void update(StoreResult storeResult) {
        runOnUiThread(() -> {
            switch (storeResult.getStatus()) {
                case LOADED:
                    // This feels Hacky. The highlighted todo is not taken from the storeResult,
                    //  but both come from the TaskStore.
                    highlightedTodo = taskStore().getHighlightTodo();
                    updateView(highlightedTodo, storeResult.getTodos());
                    break;
                case LOADING:
                    updateView("LOADING");
                    break;
                case ERROR:
                    updateView("ERROR");
                    break;
                default:
            }
       });
    }

}