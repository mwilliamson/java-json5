package org.zwobble.json5.values;

import org.zwobble.json5.sources.Json5SourceRange;

import java.util.LinkedHashMap;

public final class Json5Object implements Json5Value {
    private final LinkedHashMap<String, Json5Member> members;
    private final Json5SourceRange sourceRange;

    public Json5Object(
        LinkedHashMap<String, Json5Member> members,
        Json5SourceRange sourceRange
    ) {
        this.members = members;
        this.sourceRange = sourceRange;
    }

    public Iterable<Json5Member> members() {
        return members.values();
    }

    @Override
    public Json5SourceRange sourceRange() {
        return this.sourceRange;
    }
}
