package org.sudokuSolver;

import java.awt.*;

public class LogEntry {
    Point coords;
    int value;
    String method;

    LogEntry(Point coords, int value, String method) {
        this.coords = coords;
        this.value = value;
        this.method = method;
    }

    @Override
    public String toString() {
        return String.format(
            "%s placed at %s by %s",
            value,
            String.format("[%s, %s]", coords.x + 1, 9 - coords.y),
            method
        );
    }
}
