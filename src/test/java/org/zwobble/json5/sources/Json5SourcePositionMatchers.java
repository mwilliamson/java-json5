package org.zwobble.json5.sources;

import org.zwobble.precisely.Matcher;

import static org.zwobble.precisely.Matchers.*;

public class Json5SourcePositionMatchers {
    private Json5SourcePositionMatchers() {
    }

    public static Matcher<Json5SourcePosition> isJson5SourcePosition(
        int characterIndex
    ) {
        return has("characterIndex", x -> x.characterIndex(), equalTo(characterIndex));
    }
}
