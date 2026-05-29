package org.zwobble.json5.reader;

import org.zwobble.json5.paths.Json5Path;
import org.zwobble.json5.values.*;
import org.zwobble.sourcetext.SourceRange;

import java.text.MessageFormat;

public class Json5ObjectReadError extends RuntimeException {
    public static Json5ObjectReadError missingMember(Json5Object object, String memberName) {
        return new Json5ObjectReadError(
            object.path(),
            // TODO: escape member name
            MessageFormat.format("missing member {0}", memberName),
            object.sourceRange()
        );
    }

    public static Json5ObjectReadError mustBe64BitInteger(Json5NumberFinite value) {
        return new Json5ObjectReadError(
            value.path(),
            "must be a 64-bit integer",
            value.sourceRange()
        );
    }

    public static Json5ObjectReadError unexpectedType(Json5Value value, Class<? extends Json5Value> expectedType) {
        return new Json5ObjectReadError(
            value.path(),
            MessageFormat.format(
                "expected to be {0}, but was {1}",
                describeJson5ValueType(expectedType),
                describeJson5ValueType(value.getClass())
            ),
            value.sourceRange()
        );
    }

    private static String describeJson5ValueType(Class<? extends Json5Value> expectedType) {
        if (expectedType.equals(Json5Array.class)) {
            return "array";
        } else if (expectedType.equals(Json5Boolean.class)) {
            return "boolean";
        } else if (expectedType.equals(Json5Null.class)) {
            return "null";
        } else if (expectedType.equals(Json5NumberFinite.class)) {
            return "finite number";
        } else if (expectedType.equals(Json5NumberNan.class)) {
            return "NaN";
        } else if (expectedType.equals(Json5NumberPositiveInfinity.class)) {
            return "Infinity";
        } else if (expectedType.equals(Json5NumberNegativeInfinity.class)) {
            return "-Infinity";
        } else if (expectedType.equals(Json5Object.class)) {
            return "object";
        } else if (expectedType.equals(Json5String.class)) {
            return "string";
        } else {
            return expectedType.getSimpleName();
        }
    }

    private final Json5Path path;
    private final SourceRange sourceRange;

    public Json5ObjectReadError(Json5Path path, String message, SourceRange sourceRange) {
        super(path + " " + message);
        this.path = path;
        this.sourceRange = sourceRange;
    }

    public Json5Path path() {
        return path;
    }

    public SourceRange sourceRange() {
        return sourceRange;
    }
}
