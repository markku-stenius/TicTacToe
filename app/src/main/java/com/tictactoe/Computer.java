package com.tictactoe;

public class Computer {

    public int column;
    public int row;
    public int val;

    public Computer(int column, int row, int value) {
        this.column = column;
        this.row = row;
        this.val = value;
    }

    public void addVal(int val) {
        this.val = this.val + val;
    }
}
