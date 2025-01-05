package org.zwobble.json5.sources;

import org.zwobble.precisely.Matcher;
import org.zwobble.sourcetext.SourceRange;

import static org.zwobble.json5.sources.SourcePositionMatchers.isSourcePosition;
import static org.zwobble.precisely.Matchers.*;

public class SourceRangeMatchers {
    private SourceRangeMatchers() {
    }

    public static Matcher<SourceRange> isSourceRange(
        int startCharacterIndex,
        int endCharacterIndex
    ) {
        return allOf(
            has("start", x -> x.start(), isSourcePosition(startCharacterIndex)),
            has("end", x -> x.end(), isSourcePosition(endCharacterIndex))
        );
    }
}
