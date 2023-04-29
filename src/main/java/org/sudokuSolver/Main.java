package org.sudokuSolver;

public class Main {
    public static void main(String[] args) {
        String testGame = "8..2.4.19.7.86...2.9......87.8..2...............9..8.34......7.2...41.9.95.6.7..4";
        SudokuSolver solver = new SudokuSolver(testGame);
    }
}