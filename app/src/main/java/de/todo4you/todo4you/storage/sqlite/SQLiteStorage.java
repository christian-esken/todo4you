package de.todo4you.todo4you.storage.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.osaf.caldav4j.exceptions.CalDAV4JException;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import de.todo4you.todo4you.model.Idea;
import de.todo4you.todo4you.storage.Storage;

public class SQLiteStorage extends SQLiteOpenHelper implements Storage {
    private static final String TAG = "SQLiteStorage";
    private static volatile SQLiteStorage instance;
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "ideas";
    private static final String IDEAS_TABLE = "ideas";
    private static final String METADATA_TABLE = "metadata";
    public static final String ID_COLUMN = "id";
    public static final String UID_COLUMN = "uid";
    public static final String SUMMARY_COLUMN = "summary";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String COMPLETIONSTATE_COLUMN = "completion";

    private SQLiteStorage(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (instance != null) {
            throw new IllegalStateException("SQLiteStorage already created. Call instance(Context ctx) instead.");
        }
        instance = this;
    }

    /**
     * Returns the static SQLiteStorage.
     * @return The static SQLiteStorage. null if it has not yet been created
     */
    public static SQLiteStorage instance() {
        return instance;
    }

    /**
     * Returns the static SQLiteStorage, creating it on the first call.
     * @return The static SQLiteStorage. @NotNull
     */
    public synchronized static SQLiteStorage build(Context context) {
        if (instance == null) {
            // Note: context is used for the paths to the DB. It should not matter which view
            // of the app calls the bulld() method first.
            instance = new SQLiteStorage(context);
            Log.i(TAG, "Created SQLiteStorage for context " + context);
        }
        Log.i(TAG, "Using SQLiteStorage in context " + context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createIdeas = "CREATE TABLE " +  IDEAS_TABLE + " ("
                + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UID_COLUMN + " TEXT UNIQUE, "
                + SUMMARY_COLUMN + " TEXT, "
                + DESCRIPTION_COLUMN + " TEXT,"
                + COMPLETIONSTATE_COLUMN + " TEXT DEFAULT 'NEW')";
        String createMeta  = "CREATE TABLE " +  METADATA_TABLE + " (k TEXT PRIMARY KEY, value TEXT)";

        db.execSQL(createIdeas);
        db.execSQL(createMeta);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public List<Idea> get(LocalDate fromDate, LocalDate toDate, boolean onlyActive) throws Exception {
        return null;
    }

    @Override
    public Idea get(String uuid) throws CalDAV4JException {
        return null;
    }

    //@SuppressLint("Recycle")
    public List<Idea> getAll() throws Exception {
        String sql = "SELECT "
                + UID_COLUMN
                + ","+ SUMMARY_COLUMN
                + ","+ DESCRIPTION_COLUMN
                + "," + COMPLETIONSTATE_COLUMN
                + " FROM " + IDEAS_TABLE;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        List<Idea> ideas = new LinkedList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String uid = cursor.getString(0);
            String summary = cursor.getString(1);
            String description = cursor.getString(2);
            String completionState = cursor.getString(3);
            ideas.add(new Idea(uid, summary, description, completionState, false));
            cursor.moveToNext();
        }
        cursor.close();
        return ideas;
    }


    @Override
    public boolean add(Idea task) throws CalDAV4JException {
        ContentValues cv = new ContentValues();
        cv.put(UID_COLUMN, task.getUid());
        cv.put(SUMMARY_COLUMN, task.getSummary());
        cv.put(DESCRIPTION_COLUMN, task.getDescription());
        cv.put(COMPLETIONSTATE_COLUMN, task.getCompletionState().toString());

        SQLiteDatabase db = getWritableDatabase();
        long rowId = db.insertWithOnConflict(IDEAS_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return rowId != -1;
    }

    public boolean setFocusIdea(String uid) {
        ContentValues cv = new ContentValues();
        cv.put("k", "focus_id");
        cv.put("value", uid);

        SQLiteDatabase db = getWritableDatabase();
        long rowId = db.insertWithOnConflict(METADATA_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return rowId != -1;
    }

    public String getFocusIdea(String uid) {
        ContentValues cv = new ContentValues();
        cv.put("focus_id", uid);

        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * from " + METADATA_TABLE + " where k = 'focus_uid'";
        Cursor result = db.rawQuery(query, null);
        if (result.moveToFirst()) {
            return result.getString(1);
        }
        result.close();
        db.close();
        return null;
    }

    @Override
    public boolean update(Idea task) throws CalDAV4JException {
        return false;
    }
}
