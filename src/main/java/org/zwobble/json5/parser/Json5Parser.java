package org.zwobble.json5.parser;

import org.zwobble.json5.values.Json5Boolean;
import org.zwobble.json5.values.Json5Null;
import org.zwobble.json5.values.Json5Object;
import org.zwobble.json5.values.Json5Value;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * A parser for JSON5 documents.
 */
public class Json5Parser {
    private Json5Parser() {
    }

    /**
     * Parse JSON5 text into a JSON5 value.
     *
     * @param text The JSON5 text to parse.
     * @return A structured representation of the JSON5 value represented by
     * {@code text}.
     */
    public static Json5Value parseText(String text) {
        var tokens = Json5Tokenizer.tokenize(text);
        var tokenIterator = new TokenIterator(tokens);

        var value = parseValue(tokenIterator);

        return value;
    }

    private static Json5Value parseValue(TokenIterator tokens) {
        // JSON5Value :
        //     JSON5Null
        //     JSON5Boolean
        //     JSON5String
        //     JSON5Number
        //     JSON5Object
        //     JSON5Array

        var json5Null = tryParseNull(tokens);
        if (json5Null.isPresent()) {
            return json5Null.get();
        }

        var json5Boolean = tryParseBoolean(tokens);
        if (json5Boolean.isPresent()) {
            return json5Boolean.get();
        }

        var json5Object = tryParseObject(tokens);
        if (json5Object.isPresent()) {
            return json5Object.get();
        }

        throw new RuntimeException("TODO");
    }

    private static Optional<Json5Value> tryParseNull(TokenIterator tokens) {
        // JSON5Null ::
        //     NullLiteral
        //
        // NullLiteral ::
        //     `null`
        if (tokens.trySkip(Json5TokenType.NULL)) {
            return Optional.of(new Json5Null());
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Json5Value> tryParseBoolean(TokenIterator tokens) {
        // JSON5Boolean ::
        //     BooleanLiteral
        //
        // BooleanLiteral ::
        //     `true`
        //     `false`
        if (tokens.trySkip(Json5TokenType.TRUE)) {
            return Optional.of(new Json5Boolean(true));
        } else if (tokens.trySkip(Json5TokenType.FALSE)) {
            return Optional.of(new Json5Boolean(false));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Json5Value> tryParseObject(TokenIterator tokens) {
        // JSON5Object ::
        //     `{` `}`
        //     `{` JSON5MemberList `,`? `}`

        if (tokens.trySkip(Json5TokenType.BRACE_OPEN)) {
            return Optional.of(new Json5Object(new LinkedHashMap<>()));
        } else {
            return Optional.empty();
        }
    }
}
