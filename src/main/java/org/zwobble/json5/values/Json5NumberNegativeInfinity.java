package org.zwobble.json5.values;

import org.zwobble.json5.sources.Json5SourceRange;

public final class Json5NumberNegativeInfinity implements Json5Number {
    private final Json5SourceRange sourceRange;

    public Json5NumberNegativeInfinity(Json5SourceRange sourceRange) {
        this.sourceRange = sourceRange;
    }

    @Override
    public Json5SourceRange sourceRange() {
        return this.sourceRange;
    }
}
