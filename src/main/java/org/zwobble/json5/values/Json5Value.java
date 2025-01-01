package org.zwobble.json5.values;

import org.zwobble.json5.sources.Json5SourceRange;

/**
 * A JSON5 value.
 * <p>
 * This is a sealed interface, allowing exhaustive matching against all possible
 * types of JSON5 value.
 */
public sealed interface Json5Value permits Json5Array, Json5Boolean, Json5Null, Json5Number, Json5Object, Json5String {
    Json5SourceRange sourceRange();
}
