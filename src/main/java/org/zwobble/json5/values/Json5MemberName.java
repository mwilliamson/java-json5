package org.zwobble.json5.values;

import org.zwobble.sourcetext.SourceRange;

public class Json5MemberName {
    private final String value;
    private final SourceRange sourceRange;

    public Json5MemberName(String value, SourceRange sourceRange) {
        this.value = value;
        this.sourceRange = sourceRange;
    }

    public String value() {
        return this.value;
    }

    public SourceRange sourceRange() {
        return this.sourceRange;
    }
}
