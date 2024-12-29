package org.zwobble.json5.values;

import org.zwobble.json5.sources.Json5SourceRange;

public final class Json5Null implements Json5Value {
    private final Json5SourceRange sourceRange;

    public Json5Null(Json5SourceRange sourceRange) {
        this.sourceRange = sourceRange;
    }

    @Override
    public Json5SourceRange sourceRange() {
        return this.sourceRange;
    }
}
