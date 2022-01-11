package com.example.myclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME  = "folderPathDB";
    public static final String TABLE_PATH = "pathFolder";

    //заголовки столбцов таблицы
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME_BUTTON = "nameButton";
    public static final String KEY_PATH = "path";
    public static final String KEY_POSITION = "buttonPosition";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_PATH +
                " (" + KEY_ID + " integer primary key autoincrement,"
                    + KEY_NAME_BUTTON + " text,"
                    + KEY_PATH +" text,"
                    + KEY_POSITION + " text"
                    + ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_PATH);
        onCreate(db);
    }
}
