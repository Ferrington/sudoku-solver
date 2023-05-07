package org.sudokuSolver;

import java.awt.Point;
import java.util.*;

public class Sudoku {
    private Cell[][] grid;
    long recursionCount = 0;
    List<LogEntry> log;

    final private boolean CANDIDATE = false;
    final private boolean GIVEN = true;

    final private int EMPTY = 0;
    final private int REGION_SIZE = 9;
    final static private int SQUARE_SIZE = 3;

    enum Region {
        ROW,
        COLUMN,
        SQUARE;

        // i is expected to be between 0 and 8 inclusive
        public Point getRelativeCoords(Point coords, int i) {
            if (this == Region.ROW) {
                return new Point(i, coords.y);
            } else if (this == Region.COLUMN) {
                return new Point(coords.x, i);
            } else if (this == Region.SQUARE) {
                int xStart = coords.x / SQUARE_SIZE * SQUARE_SIZE;
                int x = xStart + (i % SQUARE_SIZE);
                int yStart = coords.y / SQUARE_SIZE * SQUARE_SIZE;
                int y = yStart + (i / SQUARE_SIZE);
                return new Point(x, y);
            }

            return null;
        }
    }

    Sudoku(String sudokuString) {
        grid = createGridFromString(sudokuString);
        log = new ArrayList<>();
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
                printLog();
            }
        } else {
            System.out.println("Either you entered an invalid string or the solver failed.");
        }
    }

    private Cell[][] stepByStepSolve(Cell[][] grid) {
        grid = deepCloneGrid(grid);
        addCandidates(grid);

        boolean gridChanged = true;
        while (gridChanged) {
            gridChanged = false;

            /*
             Place Numbers

             Will place numbers if only a single candidate remains
             */
            int placedNumbers = placeNumbers(grid);
            if (placedNumbers > 0) {
                gridChanged = true;
            }
            log.add(new LogEntry(
               deepCloneGrid(grid),
               String.format("Placed %s numbers.", placedNumbers)
            ));

            /*
             Eliminate Candidates

             Removes values from the candidate list in each cell
             when they are clearly ruled out by a given value.
             */
            if (eliminateCandidates(grid)) {
                gridChanged = true;
                log.add(new LogEntry(
                    deepCloneGrid(grid),
                    "Eliminated Obvious Candidates"
                ));
                continue;
            }

            /*
             Hidden Single

             Finds instances where a candidate has only one possible
             placement in each row/column/square
             */
            if (hiddenSingles(grid)) {
                gridChanged = true;
                log.add(new LogEntry(
                    deepCloneGrid(grid),
                    "1) Hidden Singles"
                ));
                continue;
            }

            /*
             Naked Pair
             */
            if (nakedPair(grid)) {
                gridChanged = true;
                log.add(new LogEntry(
                    deepCloneGrid(grid),
                    "2) Naked Pair"
                ));
                continue;
            }

            // naked triples
            // hidden pairs/triples
            // naked/hidden quads
            // pointing pairs
            // box/line reduction
        }
        return grid;
    }

    private boolean nakedPair(Cell[][] grid) {
        boolean eliminatedCandidates = false;
        // row
        for (int y = 0; y < REGION_SIZE; y++) {
            Map<Set<Integer>, Integer> pairCounts = new HashMap<>();
            for (int x = 0; x < REGION_SIZE; x++) {
                if (grid[y][x].getValue() != 0) continue;

                Set<Integer> candidates = grid[y][x].getCandidates();
                if (candidates.size() == 2)
                    pairCounts.merge(candidates, 1, Integer::sum);
            }
            for (Map.Entry<Set<Integer>, Integer> candidate : pairCounts.entrySet()) {
                if (candidate.getValue() == 2 && resolveNakedPairInRegion(new Point(0, y), candidate.getKey(), grid, Region.ROW)) {
                    eliminatedCandidates = true;
                }
            }
        }

        // column
        for (int x = 0; x < REGION_SIZE; x++) {
            Map<Set<Integer>, Integer> pairCounts = new HashMap<>();
            for (int y = 0; y < REGION_SIZE; y++) {
                if (grid[y][x].getValue() != 0) continue;

                Set<Integer> candidates = grid[y][x].getCandidates();
                if (candidates.size() == 2)
                    pairCounts.merge(candidates, 1, Integer::sum);
            }
            for (Map.Entry<Set<Integer>, Integer> candidate : pairCounts.entrySet()) {
                if (candidate.getValue() == 2 && resolveNakedPairInRegion(new Point(x, 0), candidate.getKey(), grid, Region.COLUMN)) {
                    eliminatedCandidates = true;
                }
            }
        }

        // square
        for (int square = 0; square < REGION_SIZE; square++) {
            int startX = square * SQUARE_SIZE % REGION_SIZE;
            int startY = square / SQUARE_SIZE * SQUARE_SIZE;
            Point startCoords = new Point(startX, startY);
            Map<Set<Integer>, Integer> pairCounts = new HashMap<>();
            for (int space = 0; space < REGION_SIZE; space++) {
                Point coords = Region.SQUARE.getRelativeCoords(startCoords, space);
                Cell cell = grid[coords.y][coords.x];
                if (cell.getValue() != 0) continue;

                Set<Integer> candidates = cell.getCandidates();
                if (candidates.size() == 2)
                    pairCounts.merge(candidates, 1, Integer::sum);
            }
            for (Map.Entry<Set<Integer>, Integer> candidate : pairCounts.entrySet()) {
                if (candidate.getValue() == 2 && resolveNakedPairInRegion(startCoords, candidate.getKey(), grid, Region.SQUARE)) {
                    eliminatedCandidates = true;
                }
            }
        }


        return eliminatedCandidates;
    }

    private boolean resolveNakedPairInRegion(Point coords, Set<Integer> candidates, Cell[][] grid, Region region) {
        boolean eliminatedCandidates = false;
        for (int i = 0; i < REGION_SIZE; i++) {
            Point cellCoords = region.getRelativeCoords(coords, i);
            Cell cell = grid[cellCoords.y][cellCoords.x];
            if (cell.getValue() == 0 && !cell.getCandidates().equals(candidates)) {
                for (Integer candidate : candidates) {
                    if (cell.eliminateCandidate(candidate))
                        eliminatedCandidates = true;
                }
            }
        }

        return eliminatedCandidates;
    }

    private boolean hiddenSingles(Cell[][] grid) {
        boolean eliminatedCandidates = false;
        // row
        for (int y = 0; y < REGION_SIZE; y++) {
            Map<Integer, Integer> candidateTracker = new HashMap<>();
            for (int x = 0; x < REGION_SIZE; x++) {
                if (grid[y][x].getValue() != 0) continue;

                Set<Integer> candidates = grid[y][x].getCandidates();
                for (Integer candidate : candidates) {
                    candidateTracker.merge(candidate, 1, Integer::sum);
                }
            }
            for (Map.Entry<Integer, Integer> candidate : candidateTracker.entrySet()) {
                if (candidate.getValue() == 1 && resolveSingleCandidateInRegion(new Point(0, y), candidate.getKey(), grid, Region.ROW)) {
                    eliminatedCandidates = true;
                }
            }
        }
        
        // column
        for (int x = 0; x < REGION_SIZE; x++) {
            Map<Integer, Integer> candidateTracker = new HashMap<>();
            for (int y = 0; y < REGION_SIZE; y++) {
                if (grid[y][x].getValue() != 0) continue;

                Set<Integer> candidates = grid[y][x].getCandidates();
                for (Integer candidate : candidates) {
                    candidateTracker.merge(candidate, 1, Integer::sum);
                }
            }
            for (Map.Entry<Integer, Integer> candidate : candidateTracker.entrySet()) {
                if (candidate.getValue() == 1 && resolveSingleCandidateInRegion(new Point(x, 0), candidate.getKey(), grid, Region.COLUMN)) {
                    eliminatedCandidates = true;
                }
            }
        }

        // square
        for (int square = 0; square < REGION_SIZE; square++) {
            int startX = square * SQUARE_SIZE % REGION_SIZE;
            int startY = square / SQUARE_SIZE * SQUARE_SIZE;
            Point startCoords = new Point(startX, startY);
            Map<Integer, Integer> candidateTracker = new HashMap<>();
            for (int space = 0; space < REGION_SIZE; space++) {
                Point coords = Region.SQUARE.getRelativeCoords(startCoords, space);
                Cell cell = grid[coords.y][coords.x];
                if (cell.getValue() != 0) continue;

                Set<Integer> candidates = cell.getCandidates();
                for (Integer candidate : candidates) {
                    candidateTracker.merge(candidate, 1, Integer::sum);
                }
            }
            for (Map.Entry<Integer, Integer> candidate : candidateTracker.entrySet()) {
                if (candidate.getValue() == 1 && resolveSingleCandidateInRegion(startCoords, candidate.getKey(), grid, Region.SQUARE)) {
                    eliminatedCandidates = true;
                }
            }
        }

        return eliminatedCandidates;
    }

    private boolean resolveSingleCandidateInRegion(Point coords, int value, Cell[][] grid, Region region) {
        for (int i = 0; i < REGION_SIZE; i++) {
            Point cellCoords = region.getRelativeCoords(coords, i);
            Cell cell = grid[cellCoords.y][cellCoords.x];
            if (cell.getValue() == 0 && cell.hasCandidate(value)) {
                cell.eliminateAllCandidatesExcept(value);
                return true;
            }
        }

        return false;
    }

    private boolean eliminateCandidates(Cell[][] grid) {
        boolean eliminatedCandidates = false;
        boolean changedGrid = true;
        while (changedGrid) {
            changedGrid = false;

            for (int y = 0; y < REGION_SIZE; y++) {
                for (int x = 0; x < REGION_SIZE; x++) {
                    int value = grid[y][x].getValue();
                    if (value == EMPTY) continue;

                    Point coords = new Point(x, y);

                    for (Region region : Region.values()) {
                        if (eliminateRegionCandidates(value, coords, grid, region)) {
                            eliminatedCandidates = true;
                            changedGrid = true;
                        }
                    }
                }
            }
        }

        return eliminatedCandidates;
    }

    private boolean eliminateRegionCandidates(int value, Point coords, Cell[][] grid, Region region) {
        boolean eliminatedCandidates = false;
        for (int i = 0; i < REGION_SIZE; i++) {
            Point cellCoords = region.getRelativeCoords(coords, i);
            Cell cell = grid[cellCoords.y][cellCoords.x];
            if (cell.getValue() == 0 && cell.eliminateCandidate(value))
                eliminatedCandidates = true;
        }

        return eliminatedCandidates;
    }

    private int placeNumbers(Cell[][] grid) {
        int placedCount = 0;

        for (int y = 0; y < REGION_SIZE; y++) {
            for (int x = 0; x < REGION_SIZE; x++) {
                Cell cell = grid[y][x];
                int finalCandidate = cell.getFinalCandidate();

                if (finalCandidate != 0) {
                    cell.eliminateCandidate(finalCandidate);
                    cell.setValue(finalCandidate);
                    placedCount++;
                }
            }
        }

        return placedCount;
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
        for (int i = 1; i <= REGION_SIZE; i++) {
            candidates.add(i);
        }

        for (Region region : Region.values()) {
            candidates.removeAll(findGivensInRegion(coords, grid, region));
        }

        return candidates;
    }

    private Set<Integer> findGivensInRegion(Point coords, Cell[][] grid, Region region) {
        Set<Integer> results = new HashSet<>();
        for (int i = 0; i < REGION_SIZE; i++) {
            Point cellCoords = region.getRelativeCoords(coords, i);
            if (grid[cellCoords.y][cellCoords.x] != null)
                results.add(grid[cellCoords.y][cellCoords.x].getValue());
        }

        return results;
    }

    private Point findOpenCellLocation(Cell[][] grid) {
        for (int y = 0; y < REGION_SIZE; y++) {
            for (int x = 0; x < REGION_SIZE; x++) {
                if (grid[y][x] == null)
                    return new Point(x, y);
            }
        }

        return null;
    }

    private Cell[][] createGridFromString(String str) {
        Cell[][] result = new Cell[REGION_SIZE][REGION_SIZE];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int y = i / REGION_SIZE % REGION_SIZE;
            int x = i % REGION_SIZE;
            if (isValidEntry(c))
                result[y][x] = new Cell(Character.getNumericValue(c), GIVEN);
        }

        return result;
    }

    private void printGridWithCandidates() {
        printGridWithCandidates(grid);
    }

    private void printGridWithCandidates(Cell[][] grid) {
        for (int y = 0; y < REGION_SIZE; y++) {
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
                for (int x = 0; x < REGION_SIZE; x++) {
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

                        if (cell.getValue() != EMPTY) {
                            if (numPosition == 5)
                                System.out.print(cell);
                            else
                                System.out.print(" ");
                        } else if (cell.hasCandidate(numPosition) && cell.getCandidates().size() == 1) {
                            System.out.print("\033[0;32m" + numPosition + "\033[0m");
                        } else if (cell.hasCandidate(numPosition)) {
                            System.out.print("\033[0;31m" + numPosition + "\033[0m");
                        } else {
                            System.out.print(" ");
                        }
                    }
                }
                System.out.print("\n");
            }
        }
        System.out.println("\n");
    }
    private void printGrid() {
        for (int y = 0; y < REGION_SIZE; y++) {
            if (y % 3 == 0 && y > 0) {
                String horizontalLine = "";
                for (int i = 0; i < 29; i++) {
                    horizontalLine += "-";
                }
                System.out.println(horizontalLine);
            }
            for (int x = 0; x < REGION_SIZE; x++) {
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

    private void printLog() {
        for (LogEntry entry : log) {
            System.out.println(entry);
            printGridWithCandidates(entry.grid);
        }
    }

    private boolean isValidEntry(char c) {
        return c >= '0' && c <= '9';
    }

    private Cell[][] deepCloneGrid(Cell[][] grid) {
        Cell[][] clone = new Cell[9][9];
        for (int y = 0; y < REGION_SIZE; y++) {
            for (int x = 0; x < REGION_SIZE; x++) {
                Cell cell = grid[y][x];
                if (cell != null)
                    clone[y][x] = new Cell(cell.getValue(), cell.getIsGiven(), new HashSet<>(cell.getCandidates()));
                else
                    clone[y][x] = null;
            }
        }

        return clone;
    }

    private void addCandidates(Cell[][] grid) {
        for (int y = 0; y < REGION_SIZE; y++) {
            for (int x = 0; x < REGION_SIZE; x++) {
                if (grid[y][x] == null)
                    grid[y][x] = new Cell(EMPTY, CANDIDATE, true);
            }
        }
    }
}
