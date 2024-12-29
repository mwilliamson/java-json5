package org.zwobble.json5.sources;

import org.zwobble.precisely.Matcher;

import static org.zwobble.precisely.Matchers.*;

public class Json5SourceRangeMatchers {
    private Json5SourceRangeMatchers() {
    }

    public static Matcher<Json5SourceRange> isJson5SourceRange(
        int startCodePointIndex,
        int endCodePointIndex
    ) {
        return allOf(
            has("startCodePointIndex", x -> x.startCodePointIndex(), equalTo(startCodePointIndex)),
            has("endCodePointIndex", x -> x.endCodePointIndex(), equalTo(endCodePointIndex))
        );
    }
}
