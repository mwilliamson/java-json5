package org.zwobble.json5.values;

import org.zwobble.json5.paths.Json5Path;
import org.zwobble.sourcetext.SourceRange;

public final class Json5NumberPositiveInfinity implements Json5Number {
    private final Json5Path path;
    private final SourceRange sourceRange;

    public Json5NumberPositiveInfinity(
        Json5Path path,
        SourceRange sourceRange
    ) {
        this.path = path;
        this.sourceRange = sourceRange;
    }

    @Override
    public Json5Path path() {
        return this.path;
    }

    @Override
    public SourceRange sourceRange() {
        return this.sourceRange;
    }
}
