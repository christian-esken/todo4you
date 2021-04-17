package de.todo4you.todo4you;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import de.todo4you.todo4you.model.Idea;
import de.todo4you.todo4you.storage.sqlite.SQLiteStorage;
import de.todo4you.todo4you.tasks.StoreResult;
import de.todo4you.todo4you.tasks.TaskStore;
import de.todo4you.todo4you.tasks.comparator.StandardTodoComparator;

public class OneTaskActivity extends RefreshableActivity {
    TextView highlightedTextView = null;
    TextView highlightedInfoTextView = null;
    TextView highlightedStatsView = null;

    Idea highlightedIdea = null;
    private SQLiteStorage deviceStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SQLiteStorage is initialized in every initial view, to make sure the TaskStore can sync.
        deviceStorage = SQLiteStorage.build(this);

        setContentView(R.layout.activity_one_task);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        highlightedTextView = findViewById(R.id.oneTitle);
        highlightedInfoTextView = findViewById(R.id.oneDetails);
        highlightedStatsView = findViewById(R.id.oneStatistics);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent myIntent = new Intent(OneTaskActivity.this, TodoMainActivity.class);
            OneTaskActivity.this.startActivity(myIntent);
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

    private void updateView(Idea idea, List<Idea> ideas) {
        updateHighlight(idea);
        int ideaCount = ideas.size();
        if (ideaCount > 0) {
            highlightedStatsView.setText("You have " + (ideaCount - 1) + " more ideas");
        } else {
            highlightedStatsView.setText("Add an idea which you want to focus on later.");
        }
    }

    @Override
    public void updateHighlight(Idea idea) {
        runOnUiThread(() -> {
            if (idea != null) {
                highlightedIdea = idea;
                highlightedTextView.setText(highlightedIdea.getSummary());
                highlightedInfoTextView.setText(todoMessage(highlightedIdea));
            }
        });
    }

    private String todoMessage(Idea highlightedIdea) {

        String cr = System.lineSeparator();
        StringBuilder sb = new StringBuilder(64);
        sb.append("Start: ").append(highlightedIdea.getStartDate()).append(cr);
        sb.append("Due  : ").append(highlightedIdea.getDueDate()).append(cr);
        sb.append("Info : ").append(highlightedIdea.getDescription());
        return sb.toString();
    }

    @Override
    public void update(StoreResult storeResult) {
        runOnUiThread(() -> {
            switch (storeResult.getStatus().status) {
                case LOADED:
                    // This feels Hacky. The highlighted todo is not taken from the storeResult,
                    //  but both come from the TaskStore.
                    // TODO Pick the highlighted Idea as stored in the local DB.
                    highlightedIdea = taskStore().getHighlightIdea();
                    List<Idea> ideas = storeResult.getTodos();
                    if (highlightedIdea == null) {
                        // Nothing picked yet. This can happen if this is the first opened view.
                        Idea[] todosArray = ideas.toArray(new Idea[ideas.size()]);
                        if (todosArray.length > 0) {
                            Arrays.sort(todosArray, new StandardTodoComparator());
                            highlightedIdea = todosArray[0];
                        }
                    }
                    updateView(highlightedIdea, ideas);
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