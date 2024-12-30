package org.zwobble.json5.values;

import org.zwobble.json5.sources.Json5SourceRange;

public class Json5MemberName {
    private final String value;
    private final Json5SourceRange sourceRange;

    public Json5MemberName(String value, Json5SourceRange sourceRange) {
        this.value = value;
        this.sourceRange = sourceRange;
    }

    public String value() {
        return this.value;
    }

    public Json5SourceRange sourceRange() {
        return this.sourceRange;
    }
}
