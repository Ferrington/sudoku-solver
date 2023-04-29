package org.sudokuSolver;

public class Cell {
    int value;
    boolean isCandidate;
    Cell(int value, boolean isCandidate) {
        this.value = value;
        this.isCandidate = isCandidate;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
