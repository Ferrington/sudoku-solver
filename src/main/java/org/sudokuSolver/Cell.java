package org.sudokuSolver;

import java.util.HashSet;
import java.util.Set;

public class Cell {
    private final String BLUE = "\033[0;34m";
    private final String BLUE_END = "\033[0m";

    private int value;
    private boolean isGiven;
    private Set<Integer> candidates;

    Cell(int value, boolean isGiven, boolean generateCandidates) {
        this.value = value;
        this.isGiven = isGiven;

        if (generateCandidates) {
            candidates = new HashSet<>();
            for (int i = 1; i <= 9; i++) {
                candidates.add(i);
            }
        }
    }

    Cell(int value, boolean isGiven) {
        this(value, isGiven, false);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean getIsGiven() {
        return isGiven;
    }

    public Set<Integer> getCandidates() {
        return candidates;
    }

    public boolean hasCandidate(int n) {
        if (candidates == null) return false;

        return candidates.contains(n);
    }

    public boolean eliminateCandidate(int value) {
        candidates.remove(value);

        if (candidates.size() == 1) {
            this.value = candidates.iterator().next();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String output = String.valueOf(value);

        if (!isGiven)
            output = BLUE + output + BLUE_END;

        return output;
    }
}

