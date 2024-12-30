package org.zwobble.json5.values;

import org.zwobble.json5.sources.Json5SourceRange;

public sealed interface Json5Number extends Json5Value permits Json5NumberFinite, Json5NumberNan, Json5NumberNegativeInfinity, Json5NumberPositiveInfinity {
}
