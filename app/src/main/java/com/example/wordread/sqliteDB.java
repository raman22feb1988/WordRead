package com.example.wordread;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
                "create table words(word text, length integer, anagram text, definition text, probability real, back text, front text)"
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

    public ArrayList<String> getTableNames()
    {
        ArrayList<String> tableList = new ArrayList<>();
        int idx = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);

                if(idx > 0) {
                    tableList.add(data);
                }
                idx++;
            } while(cursor.moveToNext());
        }
        return tableList;
    }

    public void exportDB(Context situation)
    {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        ArrayList<String> tables = getTableNames();
        for(String table : tables)
        {
            File file = new File(exportDir, "Android/data/com.example.wordread/files/" + table + ".csv");
            try
            {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                SQLiteDatabase db = this.getReadableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM " + table,null);
                String columnsList[] = curCSV.getColumnNames();
                csvWrite.writeNext(columnsList);
                while(curCSV.moveToNext())
                {
                    String arrStr[] = new String[columnsList.length];
                    for(int index = 0; index < columnsList.length; index++)
                    {
                        arrStr[index] = curCSV.getString(index);
                    }
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();
                alertBox("Export CSV", "Export CSV complete.", situation);
            }
            catch(Exception sqlEx)
            {
                alertBox("Export CSV", sqlEx.toString(), situation);
            }
        }
    }

    public void importDB(Context situation)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        String path = "Android/data/com.example.wordread/files/words.csv";
        String database = "words";

        LayoutInflater inflater = LayoutInflater.from(situation);
        final View yourCustomView = inflater.inflate(R.layout.path, null);

        TextView t3 = yourCustomView.findViewById(R.id.textview5);
        EditText e2 = yourCustomView.findViewById(R.id.edittext3);
        EditText e3 = yourCustomView.findViewById(R.id.edittext4);

        t3.setText(exportDir.toString() + "/");
        e2.setText(path);
        e3.setText(database);

        AlertDialog dialog = new AlertDialog.Builder(situation)
                .setTitle("File name")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String databaseName = (e3.getText()).toString();
                        ArrayList<String> databases = getTableNames();
                        if(databases.contains(databaseName))
                        {
                            File file = new File(exportDir, (e2.getText()).toString());
                            try
                            {
                                CSVReader csvRead = new CSVReader(new FileReader(file));
                                try {
                                    String columns[] = csvRead.readNext();
                                    String nextLine[] = csvRead.readNext();
                                    do {
                                        ContentValues contentValues = new ContentValues();
                                        for(int column = 0; column < columns.length; column++) {
                                            contentValues.put(columns[column], nextLine[column]);
                                        }
                                        db.insert(databaseName, null, contentValues);
                                        nextLine = csvRead.readNext();
                                    } while (nextLine != null);
                                    csvRead.close();
                                    alertBox("Import CSV", "Import CSV complete.", situation);
                                }
                                catch(IOException e)
                                {
                                    alertBox("Import CSV", e.toString(), situation);
                                }
                            }
                            catch(FileNotFoundException e)
                            {
                                alertBox("Import CSV", e.toString(), situation);
                            }
                        }
                        else
                        {
                            alertBox("Import CSV", "Table not found. Create a new table with the name '" + databaseName + "' first.", situation);
                        }
                    }
                }).create();
        dialog.show();
    }

    public void importLabels(Context situation)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        String path = "Android/data/com.example.wordread/files/labels.csv";

        LayoutInflater inflater = LayoutInflater.from(situation);
        final View yourCustomView = inflater.inflate(R.layout.message, null);

        TextView t2 = yourCustomView.findViewById(R.id.textview6);
        EditText e1 = yourCustomView.findViewById(R.id.edittext5);

        t2.setText(exportDir.toString() + "/");
        e1.setText(path);

        AlertDialog dialog = new AlertDialog.Builder(situation)
                .setTitle("File name")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File file = new File(exportDir, (e1.getText()).toString());
                        try
                        {
                            CSVReader csvRead = new CSVReader(new FileReader(file));
                            try {
                                String columns[] = csvRead.readNext();
                                String nextLine[] = csvRead.readNext();
                                do {
                                    ContentValues contentValues = new ContentValues();
                                    int wordIndex = 0;
                                    for(int column = 0; column < columns.length; column++) {
                                        if(columns[column].equals("word"))
                                        {
                                            wordIndex = column;
                                        }
                                        else
                                        {
                                            contentValues.put(columns[column], nextLine[column]);
                                        }
                                    }
                                    db.update("words", contentValues, "word = ?",
                                            new String[] {columns[wordIndex]});
                                    nextLine = csvRead.readNext();
                                } while (nextLine != null);
                                csvRead.close();
                                alertBox("Import labels", "Import labels complete.", situation);
                            }
                            catch(IOException e)
                            {
                                alertBox("Import labels", e.toString(), situation);
                            }
                        }
                        catch(FileNotFoundException e)
                        {
                            alertBox("Import labels", e.toString(), situation);
                        }
                    }
                }).create();
        dialog.show();
    }

    public String getSchema()
    {
        String schema = new String();
        ArrayList<String> tablesList = getTableNames();

        SQLiteDatabase db = this.getReadableDatabase();
        for(String tableName : tablesList)
        {
            Cursor cursor = db.query(tableName, null, null, null, null, null, null);
            String columnList[] = cursor.getColumnNames();
            schema += (schema.length() == 0 ? tableName + "\n" + Arrays.toString(columnList) : "\n" + tableName + "\n" + Arrays.toString(columnList));
        }
        return schema;
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

    public boolean insertWord(String word, int length, String anagram, String definition, double probability, String back, String front)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("word", word);
        contentValues.put("length", length);
        contentValues.put("anagram", anagram);
        contentValues.put("definition", definition);
        contentValues.put("probability", probability);
        contentValues.put("back", back);
        contentValues.put("front", front);

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
        Cursor cursor = db.rawQuery("SELECT word, definition, back, front FROM words WHERE length = " + letters + " ORDER BY probability DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                String meaning = cursor.getString(1);
                String back = cursor.getString(2);
                String front = cursor.getString(3);

                anagramList.add("<b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + meaning);
            } while (cursor.moveToNext());
        }

        return anagramList;
    }

    public ArrayList<String> getSqlQuery(String sqlQuery, Context activity)
    {
        try {
            ArrayList<String> anagramList = new ArrayList<>();

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT word, definition, back, front FROM words WHERE " + sqlQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    String data = cursor.getString(0);
                    String meaning = cursor.getString(1);
                    String back = cursor.getString(2);
                    String front = cursor.getString(3);

                    anagramList.add("<b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + meaning);
                } while (cursor.moveToNext());
            }

            return anagramList;
        }
        catch(SQLiteException e)
        {
            alertBox("Error", e.toString(), activity);
            return null;
        }
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

    public void alertBox(String title, String message, Context location)
    {
        LayoutInflater inflater = LayoutInflater.from(location);
        final View yourCustomView = inflater.inflate(R.layout.display, null);

        TextView t1 = yourCustomView.findViewById(R.id.textview4);
        t1.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(location)
            .setTitle(title)
            .setView(yourCustomView)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).create();
        dialog.show();
    }
}