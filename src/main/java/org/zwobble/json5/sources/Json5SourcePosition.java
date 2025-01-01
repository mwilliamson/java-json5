package org.zwobble.json5.sources;

public class Json5SourcePosition {
    private final int characterIndex;

    public Json5SourcePosition(int characterIndex) {
        this.characterIndex = characterIndex;
    }

    public int characterIndex() {
        return characterIndex;
    }
}
