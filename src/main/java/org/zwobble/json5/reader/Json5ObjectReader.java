package org.zwobble.json5.reader;

import org.zwobble.json5.parser.Json5Parser;
import org.zwobble.json5.values.*;
import org.zwobble.sourcetext.SourceText;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public class Json5ObjectReader {
    private final Json5Object object;

    private Json5ObjectReader(Json5Object object) {
        this.object = object;
    }

    public static Json5ObjectReader parse(SourceText sourceText) {
        var document = Json5Parser.parse(sourceText);

        return new Json5ObjectReader(castJson5Value(Json5Object.class, document));
    }

    public Json5ObjectReader getObject(String memberName) {
        return getObjectOrNone(memberName)
            .orElseThrow(() -> Json5ObjectReadError.missingMember(
                object,
                memberName
            ));
    }

    public Optional<Json5ObjectReader> getObjectOrNone(String memberName) {
        return getValueOfType(Json5Object.class, memberName)
            .map(object -> new Json5ObjectReader(object));
    }

    private Optional<Json5Array> getArrayOrNone(String memberName) {
        return getValueOfType(Json5Array.class, memberName);
    }

    public List<Json5ObjectReader> getArrayOfObjects(String memberName) {
        return getArrayOfObjectsOrNone(memberName)
            .orElseThrow(() -> Json5ObjectReadError.missingMember(
                object,
                memberName
            ));
    }

    public Optional<List<Json5ObjectReader>> getArrayOfObjectsOrNone(String memberName) {
        return getArrayOrNone(memberName).map(array -> {
            var objects = new ArrayList<Json5ObjectReader>();
            for (var element : array.elements()) {
                var object = castJson5Value(
                    Json5Object.class,
                    element
                );
                objects.add(new Json5ObjectReader(object));
            }
            return objects;
        });
    }

    public Optional<Json5String> getStringOrNone(String memberName) {
        return getValueOfType(Json5String.class, memberName);
    }

    public Json5String getString(String memberName) {
        return getStringOrNone(memberName)
            .orElseThrow(() -> Json5ObjectReadError.missingMember(
                object,
                memberName
            ));
    }

    public OptionalLong getLongOrNone(String memberName) {
        var numberFinite = getValueOfType(Json5NumberFinite.class, memberName);
        if (numberFinite.isEmpty()) {
            return OptionalLong.empty();
        }

        try {
            return OptionalLong.of(numberFinite.get().value().longValueExact());
        } catch (ArithmeticException exception) {
            throw Json5ObjectReadError.mustBe64BitInteger(numberFinite.get());
        }
    }

    public long getLong(String memberName) {
        return getLongOrNone(memberName)
            .orElseThrow(() -> Json5ObjectReadError.missingMember(
                object,
                memberName
            ));
    }

    private <TValue extends Json5Value> Optional<TValue> getValueOfType(
        Class<TValue> valueType,
        String memberName
    ) {
        return object.getValue(memberName)
            .map(value -> castJson5Value(
                valueType,
                value
            ));
    }

    private static <TValue extends Json5Value> TValue castJson5Value(
        Class<TValue> valueType,
        Json5Value value
    ) {
        try {
            return valueType.cast(value);
        } catch (ClassCastException exception) {
            throw Json5ObjectReadError.unexpectedType(value, valueType);
        }
    }
}
