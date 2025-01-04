package org.zwobble.json5.sources;

import java.nio.CharBuffer;

public class Json5SourceCharacterIterator {
    public static Json5SourceCharacterIterator from(String text) {
        return new Json5SourceCharacterIterator(text);
    }

    private final CharBuffer buffer;
    private int characterIndex;

    private Json5SourceCharacterIterator(String text) {
        this.buffer = CharBuffer.wrap(text);
        this.characterIndex = 0;
    }

    public boolean isEnd() {
        return this.characterIndex >= this.buffer.length();
    }

    public int remaining() {
        return this.buffer.length() - this.characterIndex;
    }

    public int peek() {
        if (this.characterIndex >= this.buffer.length()) {
            return -1;
        }

        return this.buffer.get(this.characterIndex);
    }

    public CharBuffer peekSequence(int length) {
        return this.buffer.subSequence(
            this.characterIndex,
            this.characterIndex + length
        );
    }

    public void skip() {
        if (this.characterIndex < this.buffer.length()) {
            this.characterIndex += 1;
        }
    }

    public void skip(int length) {
        this.characterIndex = Math.min(
            this.characterIndex + length,
            this.buffer.length()
        );
    }

    public Json5SourceRange characterSourceRange() {
        var end = isEnd()
            ? position()
            : new Json5SourcePosition(this.characterIndex + 1);

        return new Json5SourceRange(buffer, position(), end);
    }

    public Json5SourcePosition position() {
        return new Json5SourcePosition(this.characterIndex);
    }

    public void position(Json5SourcePosition position) {
        this.characterIndex = position.characterIndex();
    }

    public CharBuffer sequence(
        Json5SourcePosition start,
        Json5SourcePosition end
    ) {
        return this.buffer.subSequence(
            start.characterIndex(),
            end.characterIndex()
        );
    }

    public Json5SourceRange sourceRange(
        Json5SourcePosition start,
        Json5SourcePosition end
    ) {
        return new Json5SourceRange(buffer, start, end);
    }
}
