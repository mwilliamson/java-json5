package org.zwobble.json5.values;

import org.zwobble.json5.paths.Json5Path;
import org.zwobble.json5.sources.Json5SourceRange;

public final class Json5String implements Json5Value {
    private final String value;
    private final Json5Path path;
    private final Json5SourceRange sourceRange;

    public Json5String(
        String value,
        Json5Path path,
        Json5SourceRange sourceRange
    ) {
        this.value = value;
        this.path = path;
        this.sourceRange = sourceRange;
    }

    public String value() {
        return value;
    }

    @Override
    public Json5Path path() {
        return this.path;
    }

    @Override
    public Json5SourceRange sourceRange() {
        return this.sourceRange;
    }
}
