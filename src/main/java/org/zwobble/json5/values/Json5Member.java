package org.zwobble.json5.values;

import org.zwobble.sourcetext.SourceRange;

public class Json5Member {
    private final Json5MemberName name;
    private final Json5Value value;
    private final SourceRange sourceRange;

    public Json5Member(
        Json5MemberName name,
        Json5Value value,
        SourceRange sourceRange
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

    public SourceRange sourceRange() {
        return this.sourceRange;
    }
}
