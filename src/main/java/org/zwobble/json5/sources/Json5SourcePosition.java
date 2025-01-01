package org.zwobble.json5.sources;

public class Json5SourcePosition {
    private final int codePointIndex;

    public Json5SourcePosition(int codePointIndex) {
        this.codePointIndex = codePointIndex;
    }

    public int codePointIndex() {
        return codePointIndex;
    }
}
