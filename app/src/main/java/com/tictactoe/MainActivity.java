package com.tictactoe;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private int[][] board;
    private int width;
    private int height;
    private int count=0;
    private int flag=0;
    private boolean win = false;
    private int[] colours;
    private int theme;
    private String[] symbols;
    private int symbol;
    private Intent audioIntent;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        height = width = Integer.parseInt(prefs.getString("pref_game_size", "8"));
        theme = Integer.parseInt(prefs.getString("pref_game_colours", "0"));
        symbol = Integer.parseInt(prefs.getString("pref_game_symbols", "0"));

        audioIntent = new Intent(MainActivity.this, AudioService.class);

        colours = new int[]{
                getResources().getColor(R.color.green),
                getResources().getColor(R.color.red),
                getResources().getColor(R.color.magenta)
        };

        symbols = new String[]{
                "XO",
                "12",
                "-|"
        };

        // Create the board
        this.createBoard(width, height);

        // music!
        if(prefs.getBoolean("pref_audio", true)) {
            startService(audioIntent);
        }
    }

    private void createBoard(int width, int height) {
        this.board = new int[width][height];
        int button_id = 0;
        TableLayout tLayout = findViewById(R.id.board);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                0,
                TableLayout.LayoutParams.MATCH_PARENT,
                (float) 1.0);
        TableRow.LayoutParams row_params = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                (float) 1.0
        );
        for(int r=0; r < height; r++) {
             TableRow row = new TableRow(this);
             row.setLayoutParams(params);
             for(int c=0; c < width; c++) {
                 Button button = new Button(this);
                 button.setTextColor(Color.GRAY);
                 button.setLayoutParams(row_params);
                 button.setId(button_id);
                 button.setOnClickListener(this);
                 row.addView(button);
                 button_id++;
             }
             tLayout.addView(row);
        }
    }

    private void checkBoard() {
        // check for horizontal matches
        for(int row = 0; row < this.height; row++) {
            for(int column = 0; column < this.width; column++) {
                checkMatch(column, row, column-1, row);
            }
        }

        // check for vertical matches
        for(int column = 0; column < this.width; column++) {
            for(int row = 0; row < this.height; row++) {
                checkMatch(column, row, column, row-1);
            }
        }
        // check for diagonal matches
        for(int i = 0; i < this.width; i++) {
            for(int j = i, k=0; j < this.width; k++, j++) {
                checkMatch(j,k, j+1, k+1);
            }
            for(int j = 0, k=i; k < this.width; k++, j++) {
                checkMatch(j,k, j+1, k+1);
            }
            for(int j = i, k=0; j > 0; j--, k++) {
                checkMatch(j,k, j-1, k+1);
            }
            for(int j = this.width-1, k=i; k < this.width; k++, j--) {
                checkMatch(j,k, j-1, k+1);
            }
        }
    }

    private void checkMatch(int column, int row, int nColumn, int nRow) {
        if(board[column][row]>0) {
            if(flag == board[column][row]) {
                count++;
                if(count == 5) {
                    Log.i("Info", Integer.toString(flag) + " WON!");
                    gotWinner(flag);
                }
            } else {
                flag = board[column][row];
                count = 1;
            }
        } else {
            flag = 0;
            count = 0;
        }
    }

    private void gotWinner(int flag) {
        win = true;
        AlertDialog.Builder winner = new AlertDialog.Builder(this);
        winner.setMessage("Player " + String.valueOf(flag) + " won! \n\nAnother game?")
                .setTitle("We've got a winner!")
                .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recreate();
                    }
                })
                .show();
    }

    private void computerTurn() {
        int col = (int) Math.floor(Math.random() * width);
        int row = (int) Math.floor(Math.random() * height);
        Button button = findViewById(col*row);
        if (button.getText() != "") {
            computerTurn();
        } else {
            this.board[col][row] = 2;
            button.setBackgroundColor(Color.DKGRAY);
            button.setText(symbols[symbol].substring(1,2));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void newGame() {
        win=false;
        recreate();
    }

    @Override
    public void onClick(View view) {
        if(!win) {
            int id = view.getId();
            Button button = findViewById(id);
            int row = 0;
            int index = button.getId();
            if(index >= this.width) {
                do {
                    row++;
                    index = index - this.width;
                } while (index>this.width);
            }
            if(index == 8) {
                row++;
                index = 0;
            }
            Log.i("Info", "Column: " + Integer.toString(index) + " Row: " + Integer.toString(row));
            if(this.board[index][row] == 0) {
                this.board[index][row] = 1;
                button.setText(symbols[symbol].substring(0, 1));
                button.setBackgroundColor(colours[theme]);
                Log.i("Info", "Added");
                this.checkBoard();
                this.computerTurn();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_game:
                this.newGame();
                break;
            case R.id.menu_preferences:
                Intent preferencesIntent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(preferencesIntent);
                break;
            case R.id.menu_audio:
                SharedPreferences.Editor edit = prefs.edit();
                if(prefs.getBoolean("pref_audio", true)) {
                    stopService(audioIntent);
                    edit.putBoolean("pref_audio", false);
                } else {
                    startService(audioIntent);
                    edit.putBoolean("pref_audio", true);
                }
                edit.commit();
                break;
        }
        return true;
    }
}
