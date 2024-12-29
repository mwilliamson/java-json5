package org.zwobble.json5.sources;

public class Json5SourceRange {
    private final int startCodePointIndex;
    private final int endCodePointIndex;

    public Json5SourceRange(int startCodePointIndex, int codePointEndIndex) {
        this.startCodePointIndex = startCodePointIndex;
        this.endCodePointIndex = codePointEndIndex;
    }

    public int startCodePointIndex() {
        return startCodePointIndex;
    }

    public int endCodePointIndex() {
        return endCodePointIndex;
    }
}
