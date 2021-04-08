package de.todo4you.todo4you;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.todo4you.todo4you.model.Idea;
import de.todo4you.todo4you.tasks.TaskStore;
import de.todo4you.todo4you.util.StandardDates;

public class QuickSortActivity extends AppCompatActivity implements View.OnClickListener {

    Button quickScheduleButton;
    Button quickTodayButton;
    Button quickTomorrowButton;
    Button quick1weekButton;
    Button quickStatusButton;
    Button quickDelegateButton;

    Idea idea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String taskRef = getIntent().getStringExtra("taskRef");

        setContentView(R.layout.activity_quick_sort);

        idea = TaskStore.instance().findBySummary(taskRef);
        if (idea == null) {
            throw new IllegalArgumentException("Task "+ taskRef + " is unknown");
        }

        TextView overview = findViewById(R.id.quick_overview);
        overview.setText(idea.getSummary());

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
        boolean scheduled = false;
        if (button == quickTodayButton) {
            idea.setDueDate(StandardDates.now());
            scheduled = true;
        } else if (button == quickTomorrowButton) {
            idea.setDueDate(StandardDates.now().plusDays(1));
            scheduled = true;
        } if (button == quick1weekButton) {
            idea.setDueDate(StandardDates.now().plusDays(7));
            scheduled = true;
        }
        if (scheduled) {
            TaskStore.instance().taskModifed(idea);
            Snackbar.make(v, "Task rescheduled", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
