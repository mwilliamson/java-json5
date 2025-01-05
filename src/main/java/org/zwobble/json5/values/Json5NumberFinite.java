package org.zwobble.json5.values;

import org.zwobble.json5.paths.Json5Path;
import org.zwobble.sourcetext.SourceRange;

import java.math.BigDecimal;

public final class Json5NumberFinite implements Json5Number {
    private final BigDecimal value;
    private final Json5Path path;
    private final SourceRange sourceRange;

    public Json5NumberFinite(
        BigDecimal value,
        Json5Path path,
        SourceRange sourceRange
    ) {
        this.value = value;
        this.path = path;
        this.sourceRange = sourceRange;
    }

    @Override
    public Json5Path path() {
        return this.path;
    }

    public BigDecimal value() {
        return value;
    }

    @Override
    public SourceRange sourceRange() {
        return this.sourceRange;
    }
}
