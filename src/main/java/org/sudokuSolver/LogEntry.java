package org.sudokuSolver;

public class LogEntry {
    Cell[][] grid;
    String logMessage;

    LogEntry(Cell[][] grid, String logMessage) {
        this.grid = grid;
        this.logMessage = logMessage;
    }

    @Override
    public String toString() {
        return logMessage;
    }
}
