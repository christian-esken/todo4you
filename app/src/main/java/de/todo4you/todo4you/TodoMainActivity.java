package de.todo4you.todo4you;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.todo4you.todo4you.tasks.TaskSelector;
import de.todo4you.todo4you.tasks.TaskStore;

public class TodoMainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    TaskSelector ts = null;
    ListView taskListView = null;
    ArrayAdapter<String> tasklistAdapter = null;
    TextView highlightedTextView = null;
    TextView highlightedInfoTextView = null;
    SwipeRefreshLayout pullToRefresh = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String messages[] = {"Loading Tasks" };
        List<String> msgList = new ArrayList<>(Arrays.asList(messages));
        tasklistAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, msgList);


        taskListView = findViewById(R.id.listView);
        taskListView.setAdapter(tasklistAdapter);

        highlightedTextView = findViewById(R.id.highlightedTask);
        highlightedInfoTextView = findViewById(R.id.highlightedTaskInfo);

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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pullToRefresh.setRefreshing(true);
            }
        });


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

        // TaskSelector should be created at the end. It does a lot fancy stuff like registering
        // listeners and also has a reference to this View.
        ts = new TaskSelector(this);

    }


    public ArrayAdapter<String> getTaskListViewAdapter() {
        return tasklistAdapter;
    }

    public TextView getHighlightedTextView() {
        return highlightedTextView;
    }

    public TextView getHighlightedInfoTextView() {
        return highlightedInfoTextView;
    }

    public TextView getHighlightedInfoDescTextView() {
        return findViewById(R.id.highlightedTaskDesc);
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
        Object itemAtPosition = parent.getItemAtPosition(position);
        ts.highlightTask(itemAtPosition); // This is currently the String
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
}
