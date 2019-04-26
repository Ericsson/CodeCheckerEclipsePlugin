package org.codechecker.eclipse.plugin.report;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import java.util.Objects;

public class BugPathItem implements Comparable<BugPathItem> {

    private final Position startPosition;
    private final Position endPosition;
    private final String message;
    private final String file;

    public BugPathItem(Position startPosition, Position endPosition, String message, String file) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.message = message;
        this.file = file;
    }

    @Override
    public int compareTo(BugPathItem o) {
        return ComparisonChain.start().compare(file, o.file).compare(startPosition, o
                .startPosition).compare(endPosition, o.endPosition).compare(message, o.message)
                .result();
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getEndPosition() {
        return endPosition;
    }

    public String getMessage() {
        return message;
    }

    public String getFile() {
        return file;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("file", file).add("start", startPosition).add
                ("end", endPosition).add("message", message).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, message, file);
    }

    public boolean equalPosition(Object obj) {
        if (obj instanceof BugPathItem) {
            BugPathItem oth = (BugPathItem) obj;

            return Objects.equals(startPosition, oth.startPosition) && Objects.equals
                    (endPosition, oth.endPosition) && Objects.equals(file, oth.file);
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BugPathItem) {
            BugPathItem oth = (BugPathItem) obj;

            return Objects.equals(startPosition, oth.startPosition) && Objects.equals
                    (endPosition, oth.endPosition) && Objects.equals(message, oth.message) &&
                    Objects.equals(file, oth.file);
        }

        return false;
    }

    public static class Position implements Comparable<Position> {
        private final long line;
        private final long column;

        public Position(long line, long column) {
            this.line = line;
            this.column = column;
        }

        public long getLine() {
            return line;
        }

        public long getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("l", line).add("c", column).toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, column);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Position) {
                Position oth = (Position) obj;

                return Objects.equals(line, oth.line) && Objects.equals(column, oth.column);
            }

            return false;
        }

        @Override
        public int compareTo(Position o) {
            return ComparisonChain.start().compare(line, o.line).compare(column, o.column).result();
        }
    }
}
