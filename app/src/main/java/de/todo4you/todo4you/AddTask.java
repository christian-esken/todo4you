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

public class AddTask extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText summaryText = (EditText) findViewById(R.id.summary);
        summaryText.setOnFocusChangeListener(this);

        RadioButton rb = (RadioButton) findViewById(R.id.radioButton_start);
        rb.setOnClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus && (v instanceof EditText)) {
            //v.clearFocus();
            hideKeyboard(this, v);
            //v.clearFocus();
        }
    }

    public static void hideKeyboard(Activity activity, View v) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = v;
        }
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        //view.clearFocus();
    }

    @Override
    public void onClick(View v) {
        hideKeyboard(this, v);
    }
}


