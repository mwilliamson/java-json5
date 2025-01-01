package org.zwobble.json5.values;

public sealed interface Json5Number extends Json5Value permits Json5NumberFinite, Json5NumberNan, Json5NumberNegativeInfinity, Json5NumberPositiveInfinity {
}
