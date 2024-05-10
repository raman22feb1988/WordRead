package com.example.wordread;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class sqliteDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CSW2021.db";
    public Context con;

    public sqliteDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
        // TODO Auto-generated constructor stub
        con = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table words(word text, length integer, anagram text, definition text, probability real, front text, back text)"
        );
        db.execSQL(
                "create table scores(length text, counter integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS words");
        db.execSQL("DROP TABLE IF EXISTS scores");
        onCreate(db);
    }

    public boolean prepareScores()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        for(int i = 2; i <= 15; i++) {
            contentValues.put("length", Integer.toString(i));
            contentValues.put("counter", 0);

            db.insert("scores", null, contentValues);
        }

        return true;
    }

    public boolean insertScores(String query, int counter)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("length", query);
        contentValues.put("counter", counter);

        db.insert("scores", null, contentValues);

        return true;
    }

    public boolean insertWord(String word, int length, String anagram, String definition, double probability, String front, String back)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("word", word);
        contentValues.put("length", length);
        contentValues.put("anagram", anagram);
        contentValues.put("definition", definition);
        contentValues.put("probability", probability);
        contentValues.put("front", front);
        contentValues.put("back", back);

        db.insert("words", null, contentValues);

        return true;
    }

    public int getCounter(String letters)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT counter FROM scores WHERE length = \"" + letters + "\"", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }

        return Integer.parseInt(data);
    }

    public int getExist(String sqlQuery)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(counter) FROM scores WHERE length = \"" + sqlQuery + "\"", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }

        return Integer.parseInt(data);
    }

    public ArrayList<String> getAllAnagrams(int letters)
    {
        ArrayList<String> anagramList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT word, definition, front, back FROM words WHERE length = " + letters + " ORDER BY probability DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                String meaning = cursor.getString(1);
                String front = cursor.getString(2);
                String back = cursor.getString(3);

                anagramList.add("<b><small>" + back + "</small> " + data + " <small>" + front + "</small></b> " + meaning);
            } while (cursor.moveToNext());
        }

        return anagramList;
    }

    public ArrayList<String> getSqlQuery(String sqlQuery)
    {
        ArrayList<String> anagramList = new ArrayList<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT word, definition, front, back FROM words WHERE " + sqlQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    String data = cursor.getString(0);
                    String meaning = cursor.getString(1);
                    String front = cursor.getString(2);
                    String back = cursor.getString(3);

                    anagramList.add("<b><small>" + back + "</small> " + data + " <small>" + front + "</small></b> " + meaning);
                } while (cursor.moveToNext());
            }
        }
        catch(SQLiteException e)
        {
            anagramList.add(e.toString());
        }

        return anagramList;
    }

    public int updateScores(String letters, int counter) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("counter", counter);

        return db.update("scores", values, "length = ?",
                new String[] {letters});
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }
}