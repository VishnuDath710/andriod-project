package com.project.dataplotter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    // Constants for database name and version
    private static final String DATABASE_NAME = "usertasks.db";
    private static final int DATABASE_VERSION = 1;

    // Constants for tasks table
    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_TASK = "task";
    private static final String COLUMN_IMAGE = "image";

    // Constants for categories table
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_TASK_COUNT = "task_count";

    // SQL statement to create the tasks table
    private static final String SQL_CREATE_TABLE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_TASK + " TEXT, " +
                    COLUMN_IMAGE + " BLOB)";

    // SQL statement to create the categories table
    private static final String SQL_CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CATEGORY_NAME + " TEXT UNIQUE, " +
                    COLUMN_TASK_COUNT + " INTEGER DEFAULT 0)";

    // SQL statement to create the trigger
    private static final String SQL_CREATE_TRIGGER_INCREMENT_TASK_COUNT =
            "CREATE TRIGGER increment_task_count " +
                    "AFTER INSERT ON " + TABLE_TASKS + " " +
                    "BEGIN " +
                    "UPDATE " + TABLE_CATEGORIES + " " +
                    "SET " + COLUMN_TASK_COUNT + " = " + COLUMN_TASK_COUNT + " + 1 " +
                    "WHERE " + COLUMN_CATEGORY_NAME + " = NEW." + COLUMN_CATEGORY + "; " +
                    "END;";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tasks table
        db.execSQL(SQL_CREATE_TABLE_TASKS);
        // Create categories table
        db.execSQL(SQL_CREATE_TABLE_CATEGORIES);
        // Create trigger
        db.execSQL(SQL_CREATE_TRIGGER_INCREMENT_TASK_COUNT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }

    // Add other database operations as needed
}
