package org.zwobble.json5.sources;

import org.zwobble.precisely.Matcher;

import static org.zwobble.json5.sources.Json5SourcePositionMatchers.isJson5SourcePosition;
import static org.zwobble.precisely.Matchers.*;

public class Json5SourceRangeMatchers {
    private Json5SourceRangeMatchers() {
    }

    public static Matcher<Json5SourceRange> isJson5SourceRange(
        int startCodePointIndex,
        int endCodePointIndex
    ) {
        return allOf(
            has("startCodePointIndex", x -> x.start(), isJson5SourcePosition(startCodePointIndex)),
            has("endCodePointIndex", x -> x.end(), isJson5SourcePosition(endCodePointIndex))
        );
    }
}
