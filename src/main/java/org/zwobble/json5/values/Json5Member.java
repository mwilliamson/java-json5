package org.zwobble.json5.values;

import org.zwobble.json5.sources.Json5SourceRange;

public class Json5Member {
    private final Json5MemberName name;
    private final Json5Value value;
    private final Json5SourceRange sourceRange;

    public Json5Member(
        Json5MemberName name,
        Json5Value value,
        Json5SourceRange sourceRange
    ) {
        this.name = name;
        this.value = value;
        this.sourceRange = sourceRange;
    }

    public Json5MemberName name() {
        return this.name;
    }

    public Json5Value value() {
        return this.value;
    }

    public Json5SourceRange sourceRange() {
        return this.sourceRange;
    }
}
