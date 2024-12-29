package org.zwobble.json5.values;

import java.util.List;

public final class Json5Array implements Json5Value {
    private final List<Json5Value> elements;

    public Json5Array(List<Json5Value> elements) {
        this.elements = elements;
    }

    public Iterable<Json5Value> elements() {
        return elements;
    }
}
