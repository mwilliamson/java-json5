package org.zwobble.json5.values;

import org.zwobble.json5.paths.Json5Path;
import org.zwobble.sourcetext.SourceRange;

/**
 * A JSON5 value.
 * <p>
 * This is a sealed interface, allowing exhaustive matching against all possible
 * types of JSON5 value.
 */
public sealed interface Json5Value permits Json5Array, Json5Boolean, Json5Null, Json5Number, Json5Object, Json5String {
    /**
     * The path to the value in the containing document.
     */
    Json5Path path();

    SourceRange sourceRange();
}
