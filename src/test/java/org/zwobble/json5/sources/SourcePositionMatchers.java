package org.zwobble.json5.sources;

import org.zwobble.precisely.Matcher;
import org.zwobble.sourcetext.SourcePosition;

import static org.zwobble.precisely.Matchers.*;

public class SourcePositionMatchers {
    private SourcePositionMatchers() {
    }

    public static Matcher<SourcePosition> isSourcePosition(
        int characterIndex
    ) {
        return has("characterIndex", x -> x.characterIndex(), equalTo(characterIndex));
    }
}
