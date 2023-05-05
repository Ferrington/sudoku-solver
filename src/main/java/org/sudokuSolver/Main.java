package org.sudokuSolver;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String sudokuString = getSudokuString(scan);

//        String testGame = "...9....3.....28....9..4572921....38....9....37....9641678..2....37.....2....9...";
        Sudoku sudoku = new Sudoku(sudokuString);
        sudoku.solve(SolveMode.BRUTE_FORCE);
//        sudoku.solve(SolveMode.STEP_BY_STEP);
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