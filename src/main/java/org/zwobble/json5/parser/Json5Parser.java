package org.zwobble.json5.parser;

import org.zwobble.json5.values.Json5Null;
import org.zwobble.json5.values.Json5Object;
import org.zwobble.json5.values.Json5Value;

import java.util.LinkedHashMap;

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
        if (tokens.trySkip(Json5TokenType.NULL)) {
            return new Json5Null();
        } else if (tokens.trySkip(Json5TokenType.BRACE_OPEN)) {
            return new Json5Object(new LinkedHashMap<>());
        } else {
            throw new RuntimeException("TODO");
        }
    }
}
