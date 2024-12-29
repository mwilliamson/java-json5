package org.zwobble.json5.values;

/**
 * A JSON5 value.
 * <p>
 * This is sealed interface, allowing exhaustive matching against all possible
 * types of JSON5 value.
 */
public sealed interface Json5Value permits Json5Array, Json5Boolean, Json5Null, Json5Object, Json5String {
}
