package org.sudokuSolver;

import java.util.Collections;
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
        this.candidates = new HashSet<>();

        if (generateCandidates) {
            for (int i = 1; i <= 9; i++) {
                candidates.add(i);
            }
        }
    }

    Cell(int value, boolean isGiven) {
        this(value, isGiven, false);
    }

    Cell(int value, boolean isGiven, Set<Integer> candidates) {
        this.value = value;
        this.isGiven = isGiven;
        this.candidates = candidates;
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

    public int getFinalCandidate() {
        if (candidates.size() == 1) {
            return candidates.iterator().next();
        }

        return 0;
    }

    public boolean eliminateCandidate(int value) {
        return candidates.remove(value);
    }

    public boolean eliminateAllCandidatesExcept(int value) {
        return candidates.retainAll(Collections.singleton(value));
    }

    @Override
    public String toString() {
        String output = String.valueOf(value);

        if (!isGiven)
            output = BLUE + output + BLUE_END;

        return output;
    }
}

