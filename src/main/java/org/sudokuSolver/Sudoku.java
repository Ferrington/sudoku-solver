package org.sudokuSolver;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Sudoku {
    private Cell[][] grid;
    long recursionCount = 0;

    final private boolean CANDIDATE = true;
    final private boolean GIVEN = false;

    Sudoku(String sudokuString) {
        grid = createGridFromString(sudokuString);
        printGrid();
    }

    public void solve() {
        grid = bruteForceSolve(grid);

        System.out.format("%,d recursive calls required.\n\n", recursionCount);

        if (grid != null)
            printGrid();
        else
            System.out.println("Either you entered an invalid string or the solver failed.");
    }

    private Cell[][] bruteForceSolve(Cell[][] grid) {
        grid = deepCloneGrid(grid);

        // find first open Cell location
        Point coords = findOpenCellLocation(grid);

        // TERMINAL CONDITION - if no open cells, return grid
        if (coords == null) return grid;

        recursionCount++;

        // check for candidates
        Set<Integer> candidates = findCandidates(coords, grid);

        // loop through candidates
        for (Integer candidate : candidates) {
            // place new candidate in grid
            grid[coords.y][coords.x] = new Cell(candidate, CANDIDATE);

            // RECURSION - call bruteForceSolve with new grid
            Cell[][] result = bruteForceSolve(grid);

            // if result is Cell[][], return the result
            if (result != null)
                return result;
        }

        // else return null;
        return null;
    }

    private Set<Integer> findCandidates(Point coords, Cell[][] grid) {
        Set<Integer> candidates = new HashSet<>();
        for (int i = 1; i <= 9; i++) {
            candidates.add(i);
        }

        candidates.removeAll(findNumbersInRow(coords, grid));
        candidates.removeAll(findNumbersInColumn(coords, grid));
        candidates.removeAll(findNumbersInSquare(coords, grid));

        return candidates;
    }

    private Set<Integer> findNumbersInSquare(Point coords, Cell[][] grid) {
        Set<Integer> results = new HashSet<>();
        int xStart = coords.x / 3 * 3;
        int yStart = coords.y / 3 * 3;
        for (int y = yStart; y < yStart + 3; y++) {
            for (int x = xStart; x < xStart + 3; x++) {
                if (grid[y][x] != null)
                    results.add(grid[y][x].getValue());

            }
        }

        return results;
    }

    private Set<Integer> findNumbersInColumn(Point coords, Cell[][] grid) {
        Set<Integer> results = new HashSet<>();
        for (int y = 0; y < grid.length; y++) {
            if (grid[y][coords.x] != null)
                results.add(grid[y][coords.x].getValue());
        }

        return results;
    }

    private Set<Integer> findNumbersInRow(Point coords, Cell[][] grid) {
        Set<Integer> results = new HashSet<>();
        for (int x = 0; x < grid[coords.y].length; x++) {
            if (grid[coords.y][x] != null)
                results.add(grid[coords.y][x].getValue());
        }

        return results;
    }

    private Point findOpenCellLocation(Cell[][] grid) {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] == null)
                    return new Point(x, y);
            }
        }

        return null;
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
        System.out.println("\n");
    }

    private boolean isValidEntry(char c) {
        return c >= '0' && c <= '9';
    }

    private Cell[][] deepCloneGrid(Cell[][] grid) {
        Cell[][] clone = new Cell[9][9];
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                Cell cell = grid[y][x];
                if (cell != null)
                    clone[y][x] = new Cell(cell.getValue(), cell.getIsCandidate());
                else
                    clone[y][x] = null;
            }
        }

        return clone;
    }
}
