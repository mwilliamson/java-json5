package org.zwobble.json5.sources;

public class Json5SourceRange {
    private final Json5SourcePosition start;
    private final Json5SourcePosition end;

    public Json5SourceRange(Json5SourcePosition start, Json5SourcePosition end) {
        this.start = start;
        this.end = end;
    }

    public Json5SourcePosition start() {
        return this.start;
    }

    public Json5SourcePosition end() {
        return this.end;
    }
}
