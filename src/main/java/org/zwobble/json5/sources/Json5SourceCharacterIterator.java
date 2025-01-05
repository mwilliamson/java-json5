package org.zwobble.json5.sources;

import java.nio.CharBuffer;

public class Json5SourceCharacterIterator {
    public static Json5SourceCharacterIterator from(String text) {
        return new Json5SourceCharacterIterator(text);
    }

    private final Json5SourceText sourceText;
    private int characterIndex;

    private Json5SourceCharacterIterator(String text) {
        this.sourceText = Json5SourceText.fromString(text);
        this.characterIndex = 0;
    }

    public boolean isEnd() {
        return this.characterIndex >= this.sourceText.characterLength();
    }

    public int remaining() {
        return this.sourceText.characterLength() - this.characterIndex;
    }

    public int peek() {
        if (this.characterIndex >= this.sourceText.characterLength()) {
            return -1;
        }

        return this.sourceText.getCharacter(this.characterIndex);
    }

    public CharBuffer peekSequence(int length) {
        return this.sourceText.charBuffer(
            this.characterIndex,
            this.characterIndex + length
        );
    }

    public void skip() {
        if (this.characterIndex < this.sourceText.characterLength()) {
            this.characterIndex += 1;
        }
    }

    public void skip(int length) {
        this.characterIndex = Math.min(
            this.characterIndex + length,
            this.sourceText.characterLength()
        );
    }

    public Json5SourceRange characterSourceRange() {
        var end = isEnd()
            ? position()
            : new Json5SourcePosition(this.characterIndex + 1);

        return new Json5SourceRange(sourceText, position(), end);
    }

    public Json5SourcePosition position() {
        return new Json5SourcePosition(this.characterIndex);
    }

    public void position(Json5SourcePosition position) {
        this.characterIndex = position.characterIndex();
    }

    public Json5SourceRange sourceRange(
        Json5SourcePosition start,
        Json5SourcePosition end
    ) {
        return new Json5SourceRange(sourceText, start, end);
    }
}
