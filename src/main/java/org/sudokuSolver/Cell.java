package org.sudokuSolver;

public class Cell {
    private final String BLUE = "\033[0;34m";
    private final String BLUE_END = "\033[0m";

    private int value;
    private boolean isCandidate;

    Cell(int value, boolean isCandidate) {
        this.value = value;
        this.isCandidate = isCandidate;
    }

    public int getValue() {
        return value;
    }

    public boolean getIsCandidate() {
        return isCandidate;
    }

    @Override
    public String toString() {
        String output = String.valueOf(value);

        if (isCandidate)
            output = BLUE + output + BLUE_END;

        return output;
    }
}
