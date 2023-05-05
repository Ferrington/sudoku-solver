package org.sudokuSolver;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Sudoku {
    private Cell[][] grid;
    long recursionCount = 0;

    final private boolean CANDIDATE = false;
    final private boolean GIVEN = true;
    final private int EMPTY = 0;

    Sudoku(String sudokuString) {
        grid = createGridFromString(sudokuString);
        printGrid();
    }

    public void solve(SolveMode mode) {
        if (mode == SolveMode.BRUTE_FORCE)
            grid = bruteForceSolve(grid);
        else if (mode == SolveMode.STEP_BY_STEP)
            grid = stepByStepSolve(grid);

        System.out.format("%,d recursive calls required.\n\n", recursionCount);

        if (grid != null) {
            if (mode == SolveMode.BRUTE_FORCE) {
                printGrid();
            } else {
                printGridWithCandidates();
            }
        } else {
            System.out.println("Either you entered an invalid string or the solver failed.");
        }
    }

    private Cell[][] stepByStepSolve(Cell[][] grid) {
        grid = deepCloneGrid(grid);
        addCandidates(grid);

        boolean cellPlaced = true;
        while (cellPlaced) {
            cellPlaced = false;

            // this removes candidates from the candidate list in each cell
            // where they are clearly ruled out by a given value
            // if a number is placed into a cell, we continue to eliminate more candidates
            if (eliminateCandidates(grid)) {
                cellPlaced = true;
                continue;
            }

            // hidden singles
            // naked pairs/triples
            // hidden pairs/triples
            // naked/hidden quads
            // pointing pairs
            // box/line reduction
        }
        return grid;
    }

    private boolean eliminateCandidates(Cell[][] grid) {
        boolean placedNumber = false;

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                int value = grid[y][x].getValue();
                if (value != EMPTY) {
                    Point coords = new Point(x, y);
                    if (eliminateRowCandidates(value, coords, grid))
                        placedNumber = true;
                    if (eliminateColumnCandidates(value, coords, grid))
                        placedNumber = true;
                    if (eliminateSquareCandidates(value, coords, grid))
                        placedNumber = true;
                }
            }
        }

        return placedNumber;
    }

    private boolean eliminateRowCandidates(int value, Point coords, Cell[][] grid) {
        boolean placedNumber = false;

        for (int x = 0; x < grid[coords.y].length; x++) {
            Cell cell = grid[coords.y][x];
            if (cell.getValue() == 0 && cell.eliminateCandidate(value))
                placedNumber = true;
        }

        return placedNumber;
    }

    private boolean eliminateColumnCandidates(int value, Point coords, Cell[][] grid) {
        boolean placedNumber = false;

        for (int y = 0; y < grid.length; y++) {
            Cell cell = grid[y][coords.x];
            if (cell.getValue() == 0 && cell.eliminateCandidate(value))
                placedNumber = true;
        }

        return placedNumber;
    }

    private boolean eliminateSquareCandidates(int value, Point coords, Cell[][] grid) {
        boolean placedNumber = false;

        int xStart = coords.x / 3 * 3;
        int yStart = coords.y / 3 * 3;
        for (int y = yStart; y < yStart + 3; y++) {
            for (int x = xStart; x < xStart + 3; x++) {
                Cell cell = grid[y][x];
                if (cell.getValue() == 0 && cell.eliminateCandidate(value)) {
                    placedNumber = true;
                }
            }
        }

        return placedNumber;
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

    private void printGridWithCandidates() {
        for (int y = 0; y < grid.length; y++) {
            if (y % 3 == 0 && y > 0) {
                String horizontalLine = "";
                for (int i = 0; i < 51; i++) {
                    horizontalLine += "-";
                }
                System.out.println(horizontalLine);
            } else if (y != 0) {
                System.out.print("\n");
            }
            for (int line = 0; line < 3; line++) {
                for (int x = 0; x < grid[y].length; x++) {
                    if (x % 3 == 0 && x > 0)
                        System.out.print(" | ");
                    else if (x > 0)
                        System.out.print("   ");
                    for (int n = 0; n < 3; n++) {
                        Cell cell = grid[y][x];
                        int numPosition = line * 3 + n + 1;
                        if (cell == null) {
                            System.out.print(".");
                            continue;
                        }

                        if (cell.getValue() != EMPTY)
                            if (numPosition == 5)
                                System.out.print(cell);
                            else
                                System.out.print(" ");
                        else if (cell.hasCandidate(numPosition))
                            System.out.print("\033[0;31m" + numPosition + "\033[0m");
                        else
                            System.out.print(" ");
                    }
                }
                System.out.print("\n");
            }
        }
        System.out.println("\n");
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
                    clone[y][x] = new Cell(cell.getValue(), cell.getIsGiven());
                else
                    clone[y][x] = null;
            }
        }

        return clone;
    }

    private void addCandidates(Cell[][] grid) {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] == null)
                    grid[y][x] = new Cell(EMPTY, CANDIDATE, true);
            }
        }
    }
}
