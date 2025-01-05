package org.zwobble.json5.parser;

import org.zwobble.sourcetext.SourceRange;

public class Json5ParseError extends RuntimeException {
    public static Json5ParseError unexpectedTextError(
        String expected,
        String actual,
        SourceRange sourceRange
    ) {
        var message = String.format(
            "Expected %s, but was %s",
            expected,
            actual
        );
        return new Json5ParseError(message, sourceRange);
    }

    private final SourceRange sourceRange;

    public Json5ParseError(String message, SourceRange sourceRange) {
        super(message);
        this.sourceRange = sourceRange;
    }

    public SourceRange sourceRange() {
        return sourceRange;
    }
}
