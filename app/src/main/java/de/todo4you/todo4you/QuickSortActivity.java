package de.todo4you.todo4you;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDate;

import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.tasks.TaskStore;
import de.todo4you.todo4you.util.StandardDates;

public class QuickSortActivity extends AppCompatActivity implements View.OnClickListener {

    Button quickScheduleButton;
    Button quickTodayButton;
    Button quickTomorrowButton;
    Button quick1weekButton;
    Button quickStatusButton;
    Button quickDelegateButton;

    Todo todo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String taskRef = getIntent().getStringExtra("taskRef");

        setContentView(R.layout.activity_quick_sort);

        todo = TaskStore.instance().findBySummary(taskRef);
        if (todo == null) {
            throw new IllegalArgumentException("Task "+ taskRef + " is unknown");
        }

        TextView overview = findViewById(R.id.quick_overview);
        overview.setText(todo.getSummary());

        quickScheduleButton = (Button)findViewById(R.id.quick_when_schedule);
        quickTodayButton = (Button)findViewById(R.id.quick_when_today);
        quickTomorrowButton = (Button)findViewById(R.id.quick_when_tomorrow);
        quick1weekButton = (Button)findViewById(R.id.quick_when_1week);

        quickStatusButton = (Button)findViewById(R.id.quick_status);
        quickDelegateButton = (Button)findViewById(R.id.quick_delegate);

        quickTodayButton.setOnClickListener(this);
        quickTomorrowButton.setOnClickListener(this);
        quick1weekButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Button button = (Button)v;
        if (button == quickTodayButton) {
            todo.setDueDate(StandardDates.now());
        } else if (button == quickTomorrowButton) {
            todo.setDueDate(StandardDates.now().plusDays(1));
        } if (button == quick1weekButton) {
            todo.setDueDate(StandardDates.now().plusDays(7));
        }
    }
}
