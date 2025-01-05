package org.zwobble.json5.sources;

import java.nio.CharBuffer;

public class Json5SourceRange {
    private final Json5SourceText sourceText;
    private final Json5SourcePosition start;
    private final Json5SourcePosition end;

    public Json5SourceRange(
        Json5SourceText sourceText,
        Json5SourcePosition start,
        Json5SourcePosition end
    ) {
        this.sourceText = sourceText;
        this.start = start;
        this.end = end;
    }

    public CharBuffer charBuffer() {
        return sourceText.charBuffer(
            start.characterIndex(),
            end.characterIndex()
        );
    }

    public Json5SourceText sourceText() {
        return this.sourceText;
    }

    public Json5SourcePosition start() {
        return this.start;
    }

    public Json5SourcePosition end() {
        return this.end;
    }

    public Json5SourceRange to(Json5SourceRange end) {
        return new Json5SourceRange(
            this.sourceText,
            this.start,
            end.end
        );
    }
}
