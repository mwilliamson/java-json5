package org.zwobble.json5.parser;

import org.zwobble.json5.sources.Json5SourceRange;

public class Json5ParseError extends RuntimeException {
    public static Json5ParseError unexpectedTextError(
        String expected,
        String actual,
        Json5SourceRange sourceRange
    ) {
        var message = String.format(
            "Expected %s, but was %s",
            expected,
            actual
        );
        return new Json5ParseError(message, sourceRange);
    }

    private final Json5SourceRange sourceRange;

    public Json5ParseError(String message, Json5SourceRange sourceRange) {
        super(message);
        this.sourceRange = sourceRange;
    }

    public Json5SourceRange sourceRange() {
        return sourceRange;
    }
}
