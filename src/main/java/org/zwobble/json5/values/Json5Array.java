package org.zwobble.json5.values;

import org.zwobble.json5.sources.Json5SourceRange;

import java.util.List;

public final class Json5Array implements Json5Value {
    private final List<Json5Value> elements;
    private final Json5SourceRange sourceRange;

    public Json5Array(List<Json5Value> elements, Json5SourceRange sourceRange) {
        this.elements = elements;
        this.sourceRange = sourceRange;
    }

    public Iterable<Json5Value> elements() {
        return elements;
    }

    @Override
    public Json5SourceRange sourceRange() {
        return this.sourceRange;
    }
}
