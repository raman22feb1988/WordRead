package com.example.wordread;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    sqliteDB db;
    int letters = 0;
    String sqlQuery = "1 = 1";
    HashMap<String, String> dictionary;

    TextView t1;
    TextView t2;
    Button b1;
    Button b2;
    Button b3;
    Button b4;

    ArrayList<String> anagrams;
    int words;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1 = findViewById(R.id.textview1);
        t2 = findViewById(R.id.textview2);
        b1 = findViewById(R.id.button1);
        b2 = findViewById(R.id.button2);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);

        db = new sqliteDB(MainActivity.this);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        boolean prepared = pref.getBoolean("prepared", false);

        if(prepared) {
            getWordLength();
        } else {
            Toast.makeText(MainActivity.this, "Please give some time to prepare database of dictionary words only when opening this for the first time", Toast.LENGTH_LONG).show();
            db.prepareScores();
            prepareDictionary();
        }

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });
    }

    public void prepareDictionary()
    {
        dictionary = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("CSW2021.txt"), "UTF-8"));
            while(true)
            {
                String s = reader.readLine();
                if(s == null)
                {
                    break;
                }
                else
                {
                    String t[] = s.split("=");
                    dictionary.put(t[0], t[1]);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        prepareDatabase();
    }

    public void prepareDatabase()
    {
        Iterator<Map.Entry<String, String>> itr = dictionary.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String word = entry.getKey();
            char c[] = word.toCharArray();
            Arrays.sort(c);
            String anagram = new String(c);
            String definition = entry.getValue();
            StringBuilder front = new StringBuilder();
            StringBuilder back = new StringBuilder();
            for(char letter = 'A'; letter <= 'Z'; letter++)
            {
                if(dictionary.containsKey(word + letter))
                {
                    front.append(letter);
                }
                if(dictionary.containsKey(letter + word))
                {
                    back.append(letter);
                }
            }
            boolean q = db.insertWord(word, word.length(), anagram, definition, probability(word), new String(front), new String(back));
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("prepared", true);
        editor.commit();

        getWordLength();
    }

    public void getWordLength()
    {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        letters = Integer.parseInt((e1.getText()).toString());
                        if(letters < 2 || letters > 15)
                        {
                            Toast.makeText(MainActivity.this, "Enter a value between 2 and 15", Toast.LENGTH_LONG).show();
                            getWordLength();
                        }
                        else
                        {
                            start();
                        }
                    }
                }).create();
        dialog.show();
    }

    public void getSqlQuery()
    {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View yourCustomView = inflater.inflate(R.layout.sql_query, null);

        EditText e2 = yourCustomView.findViewById(R.id.edittext2);

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("SELECT back, word, front, definition FROM words WHERE")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sqlQuery = (e2.getText()).toString();
                        execute();
                    }
                }).create();
        dialog.show();
    }

    public void start()
    {
        anagrams = db.getAllAnagrams(letters);
        words = anagrams.size();
        counter = db.getCounter(Integer.toString(letters));

        nextWord();
    }

    public void execute()
    {
        anagrams = db.getSqlQuery(sqlQuery);
        words = anagrams.size();
        String query = "SELECT word, definition, front, back FROM words WHERE " + sqlQuery;
        int exist = db.getExist(query);

        if(exist == 0) {
            counter = 0;
            db.insertScores(query, counter);
        }
        else {
            counter = db.getCounter(query);
        }

        executeSqlQuery(query);
    }

    public void nextWord()
    {
        b1.setEnabled(true);
        b2.setEnabled(true);
        b3.setEnabled(true);

        t1.setText("Page " + (counter + 1) + " out of " + (((words - 1) / 100) + 1));
        t2.setText("");

        for(int i = 0; i < 100; i++)
        {
            int position = (counter * 100) + i;
            if(position >= words)
            {
                break;
            }
            String jumble = anagrams.get(position);
            if(i == 0)
            {
                t2.setText((position + 1) + ". " + jumble);
            }
            else
            {
                t2.setText(t2.getText() + "<br>" + (position + 1) + ". " + jumble);
            }
        }

        t2.setText(Html.fromHtml((t2.getText()).toString()));

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter--;
                if(counter < 0)
                {
                    counter = (words - 1) / 100;
                }
                db.updateScores(Integer.toString(letters), counter);
                nextWord();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;
                if(counter == ((words - 1) / 100) + 1)
                {
                    counter = 0;
                }
                db.updateScores(Integer.toString(letters), counter);
                nextWord();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSqlQuery();
            }
        });
    }

    public void executeSqlQuery(String query)
    {
        b1.setEnabled(true);
        b2.setEnabled(true);
        b3.setEnabled(true);

        t1.setText("Page " + (counter + 1) + " out of " + (((words - 1) / 100) + 1));
        t2.setText("");

        for(int i = 0; i < 100; i++)
        {
            int position = (counter * 100) + i;
            if(position >= words)
            {
                break;
            }
            String jumble = anagrams.get(position);
            if(i == 0)
            {
                if(jumble.contains("SQLiteException")) {
                    t2.setText(jumble);
                }
                else {
                    t2.setText((position + 1) + ". " + jumble);
                }
            }
            else
            {
                if(jumble.contains("SQLiteException")) {
                    t2.setText(t2.getText() + "<br>" + jumble);
                }
                else {
                    t2.setText(t2.getText() + "<br>" + (position + 1) + ". " + jumble);
                }
            }
        }

        t2.setText(Html.fromHtml((t2.getText()).toString()));

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter--;
                if(counter < 0)
                {
                    counter = (words - 1) / 100;
                }
                db.updateScores(query, counter);
                executeSqlQuery(query);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;
                if(counter == ((words - 1) / 100) + 1)
                {
                    counter = 0;
                }
                db.updateScores(query, counter);
                executeSqlQuery(query);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSqlQuery();
            }
        });
    }

    public double probability(String st)
    {
        int frequency[] = new int[]{9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
        int count = 100;
        double chance = 1;
        for(int j = 0; j < st.length(); j++)
        {
            char ch = st.charAt(j);
            int ord = ((int) ch) - 65;
            chance *= frequency[ord];
            chance /= count;
            if(frequency[ord] > 0) {
                frequency[ord]--;
            }
            count--;
        }
        return chance;
    }
}