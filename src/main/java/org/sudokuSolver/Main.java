package org.sudokuSolver;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
//        String sudokuString = getSudokuString(scan);
//        Sudoku sudoku = new Sudoku(sudokuString);

        String testGame = "4......38..2..41....53..24..7.6.9..4.2.....7.6..7.3.9..57..83....39..4..24......9";
        Sudoku sudoku = new Sudoku(testGame);

//        sudoku.solve(SolveMode.BRUTE_FORCE);
        sudoku.solve(SolveMode.STEP_BY_STEP);
    }

    private static String getSudokuString(Scanner scan) {
        while (true) {
            System.out.println("Please enter a Sudoku string:");
            String response = scan.nextLine();

            if (response.length() == 81)
                return response;
            else
                System.out.println("The string should have 81 characters.");
        }
    }
}