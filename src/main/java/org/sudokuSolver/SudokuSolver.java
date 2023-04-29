package org.sudokuSolver;

public class SudokuSolver {
    final private Cell[][] grid;

    final private boolean CANDIDATE = true;
    final private boolean GIVEN = false;

    SudokuSolver(String sudokuString) {
        grid = createGridFromString(sudokuString);
        printGrid();
    }

    private Cell[][] createGridFromString(String str) {
        Cell[][] result = new Cell[9][9];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int y = i / 9 % 9;
            int x = i % 9;
            if (isValidEntry(c))
                result[y][x] = new Cell(Character.getNumericValue(c), GIVEN);
        }

        return result;
    }

    private void printGrid() {
        for (int y = 0; y < grid.length; y++) {
            if (y % 3 == 0 && y > 0) {
                String horizontalLine = "";
                for (int i = 0; i < 29; i++) {
                    horizontalLine += "-";
                }
                System.out.println(horizontalLine);
            }
            for (int x = 0; x < grid[y].length; x++) {
                if (x % 3 == 0 && x > 0)
                    System.out.print("| ");

                if (grid[y][x] == null)
                    System.out.print(".  ");
                else
                    System.out.print(grid[y][x] + "  ");
            }


            System.out.print("\n");
        }
    }

    private boolean isValidEntry(char c) {
        return c >= '0' && c <= '9';
    }
}
