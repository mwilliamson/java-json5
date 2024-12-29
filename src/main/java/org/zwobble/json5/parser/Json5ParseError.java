package org.zwobble.json5.parser;

import org.zwobble.json5.sources.Json5SourceRange;

public class Json5ParseError extends RuntimeException {
    private final Json5SourceRange sourceRange;

    public Json5ParseError(String message, Json5SourceRange sourceRange) {
        super(message);
        this.sourceRange = sourceRange;
    }

    public Json5SourceRange sourceRange() {
        return sourceRange;
    }
}
