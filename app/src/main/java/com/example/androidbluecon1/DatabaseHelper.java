package com.example.androidbluecon1;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;
import java.util.ArrayList;

class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Database6";
    public static final String CONTACTS_TABLE_NAME = "Details";
    public DatabaseHelper(MainActivity context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(
                    "create table " + CONTACTS_TABLE_NAME + "(id INTEGER PRIMARY KEY, name text,amount int,datetime default (datetime('now','localtime')) )"
            );
        } catch (SQLiteException e) {
            try {
                throw new IOException(e);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insert(String s, String s1) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", s);
        // contentValues.put("amount", s1);
        db.replace(CONTACTS_TABLE_NAME, null, contentValues);
        return true;
    }

    public ArrayList getAllCotacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> array_list = new ArrayList<String>();
        Cursor res = db.rawQuery("select (id ||' : '||name || ', 수량: ' || amount || ', 인식시간: '|| strftime('%d-%m-%Y %H:%M:%S', datetime)) as fullname from " + CONTACTS_TABLE_NAME, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            if ((res != null) && (res.getCount() > 0))
                array_list.add(res.getString(res.getColumnIndex("fullname")));
            res.moveToNext();
        }
        return array_list;
    }

    public boolean delete() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE from " + CONTACTS_TABLE_NAME);
        return true;
    }
}
