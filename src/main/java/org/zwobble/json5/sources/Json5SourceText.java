package org.zwobble.json5.sources;

import java.nio.CharBuffer;

public class Json5SourceText {
    public static Json5SourceText fromString(String string) {
        return new Json5SourceText(CharBuffer.wrap(string));
    }

    private final CharBuffer charBuffer;

    private Json5SourceText(CharBuffer charBuffer) {
        this.charBuffer = charBuffer;
    }

    public CharBuffer charBuffer(
        int startCharacterIndex,
        int endCharacterIndex
    ) {
        return this.charBuffer.subSequence(
            startCharacterIndex,
            endCharacterIndex
        );
    }

    public int characterLength() {
        return this.charBuffer.length();
    }

    public int getCharacter(int characterIndex) {
        return this.charBuffer.get(characterIndex);
    }
}
