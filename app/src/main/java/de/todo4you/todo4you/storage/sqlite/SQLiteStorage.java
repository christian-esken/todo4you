package de.todo4you.todo4you.storage.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import org.osaf.caldav4j.exceptions.CalDAV4JException;

import java.time.LocalDate;
import java.util.List;

import de.todo4you.todo4you.model.Todo;
import de.todo4you.todo4you.storage.Storage;

public class SQLiteStorage extends SQLiteOpenHelper implements Storage {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "ideas";
    private static final String IDEAS_TABLE = "ideas";
    private static final String METADATA_TABLE = "metadata";
    public static final String UID_COLUMN = "uid";
    public static final String SUMMARY_COLUMN = "summary";

    public SQLiteStorage(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createIdeas = "CREATE TABLE " +  IDEAS_TABLE + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + UID_COLUMN + " TEXT, " + SUMMARY_COLUMN + " TEXT, description TEXT, sync_state INT)";
        String createMeta  = "CREATE TABLE " +  METADATA_TABLE + " (k TEXT PRIMARY KEY, value TEXT)";

        db.execSQL(createIdeas);
        db.execSQL(createMeta);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public List<Todo> get(LocalDate fromDate, LocalDate toDate, boolean onlyActive) throws Exception {
        return null;
    }

    @Override
    public Todo get(String uuid) throws CalDAV4JException {
        return null;
    }

    @Override
    public boolean add(Todo task) throws CalDAV4JException {
        ContentValues cv = new ContentValues();
        cv.put(UID_COLUMN, task.getUid());
        cv.put(SUMMARY_COLUMN, task.getDescription());

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
    public boolean update(Todo task) throws CalDAV4JException {
        return false;
    }
}
