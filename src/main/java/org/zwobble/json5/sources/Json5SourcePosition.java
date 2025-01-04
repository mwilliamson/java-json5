package org.zwobble.json5.sources;

/**
 * A position in a source text. Rather than referring to a specific character,
 * a position is between characters, or at the start or end of the source text.
 */
public class Json5SourcePosition {
    private final int characterIndex;

    public Json5SourcePosition(int characterIndex) {
        this.characterIndex = characterIndex;
    }

    public int characterIndex() {
        return characterIndex;
    }
}
