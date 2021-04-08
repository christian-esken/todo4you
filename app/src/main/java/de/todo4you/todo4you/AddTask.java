package de.todo4you.todo4you;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;

import de.todo4you.todo4you.model.Idea;
import de.todo4you.todo4you.tasks.TaskStore;

public class AddTask extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText summaryText = (EditText) findViewById(R.id.summary);
        summaryText.setOnFocusChangeListener(this);

        // Hide keyboard when the user clicks on something else
        makeHideMenu(R.id.radioButton_start);
        makeHideMenu(R.id.radioButton_due);
        makeHideMenu(R.id.radioButton_notscheduled);

        makeHideMenu(R.id.radioButton_today);
        makeHideMenu(R.id.radioButton_tomorrow);
        makeHideMenu(R.id.radioButton_thisweek);
        makeHideMenu(R.id.radioButton_weekend);
        makeHideMenu(R.id.radioButton_nextweek);
        makeHideMenu(R.id.radioButton_fixedDate);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Adding task", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Idea task = new Idea(summaryText.getText().toString());
                TaskStore.instance().addNewTask(task);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void makeHideMenu(int idRadioButton) {
        RadioButton rb = (RadioButton) findViewById(idRadioButton);
        rb.setOnClickListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus && (v instanceof EditText)) {
            hideKeyboard(this, v);
        }
    }

    public static void hideKeyboard(Activity activity, View v) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        /*
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = v;
        }
        */
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        hideKeyboard(this, v);
    }
}


