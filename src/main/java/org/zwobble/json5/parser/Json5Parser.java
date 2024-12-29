package org.zwobble.json5.parser;

import org.zwobble.json5.values.*;

import java.nio.CharBuffer;
import java.util.ArrayList;
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

        var json5String = tryParseString(tokens);
        if (json5String.isPresent()) {
            return json5String.get();
        }

        var json5Number = tryParseNumber(tokens);
        if (json5Number.isPresent()) {
            return json5Number.get();
        }

        var json5Object = tryParseObject(tokens);
        if (json5Object.isPresent()) {
            return json5Object.get();
        }

        var json5Array = tryParseArray(tokens);
        if (json5Array.isPresent()) {
            return json5Array.get();
        }

        throw new RuntimeException("TODO");
    }

    private static Optional<Json5Value> tryParseNull(TokenIterator tokens) {
        // JSON5Null ::
        //     NullLiteral
        //
        // NullLiteral ::
        //     `null`

        if (tokens.trySkip(Json5TokenType.IDENTIFIER, BUFFER_NULL)) {
            return Optional.of(new Json5Null());
        } else {
            return Optional.empty();
        }
    }

    private static final CharBuffer BUFFER_NULL = CharBuffer.wrap("null");

    private static Optional<Json5Value> tryParseBoolean(TokenIterator tokens) {
        // JSON5Boolean ::
        //     BooleanLiteral
        //
        // BooleanLiteral ::
        //     `true`
        //     `false`

        if (tokens.trySkip(Json5TokenType.IDENTIFIER, BUFFER_TRUE)) {
            return Optional.of(new Json5Boolean(true));
        } else if (tokens.trySkip(Json5TokenType.IDENTIFIER, BUFFER_FALSE)) {
            return Optional.of(new Json5Boolean(false));
        } else {
            return Optional.empty();
        }
    }

    private static final CharBuffer BUFFER_TRUE = CharBuffer.wrap("true");
    private static final CharBuffer BUFFER_FALSE = CharBuffer.wrap("false");

    private static Optional<Json5Value> tryParseString(TokenIterator tokens) {
        if (tokens.trySkip(Json5TokenType.STRING)) {
            return Optional.of(new Json5String(""));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Json5Value> tryParseNumber(TokenIterator tokens) {
        if (tokens.trySkip(Json5TokenType.NUMBER)) {
            return Optional.of(new Json5Number("0"));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Json5Value> tryParseObject(TokenIterator tokens) {
        // JSON5Object ::
        //     `{` `}`
        //     `{` JSON5MemberList `,`? `}`

        if (!tokens.trySkip(Json5TokenType.PUNCTUATOR_BRACE_OPEN)) {
            return Optional.empty();
        }

        if (tokens.trySkip(Json5TokenType.PUNCTUATOR_BRACE_CLOSE)) {
            return Optional.of(new Json5Object(new LinkedHashMap<>()));
        }

        throw unexpectedTokenError("JSON value or closing brace", tokens);
    }

    private static Optional<Json5Value> tryParseArray(TokenIterator tokens) {
        // JSON5Array ::
        //     `[` `]`
        //     `[` JSON5ElementList `,`? `]`

        if (!tokens.trySkip(Json5TokenType.PUNCTUATOR_SQUARE_OPEN)) {
            return Optional.empty();
        }

        if (tokens.trySkip(Json5TokenType.PUNCTUATOR_SQUARE_CLOSE)) {
            return Optional.of(new Json5Array(new ArrayList<>()));
        }

        throw unexpectedTokenError("JSON value or closing square bracket", tokens);
    }

    private static Json5ParseError unexpectedTokenError(String expected, TokenIterator tokens) {
        var message = String.format(
            "Expected %s, but was %s",
            expected,
            describeToken(tokens.peek())
        );
        return new Json5ParseError(message);
    }

    private static String describeToken(Json5Token tokens) {
        return switch (tokens.tokenType()) {
            case IDENTIFIER ->
                throw new UnsupportedOperationException("TODO");

            case PUNCTUATOR_BRACE_OPEN ->
                "{";

            case PUNCTUATOR_BRACE_CLOSE ->
                "}";

            case PUNCTUATOR_SQUARE_OPEN ->
                "[";

            case PUNCTUATOR_SQUARE_CLOSE ->
                "]";

            case STRING ->
                throw new UnsupportedOperationException("TODO");

            case NUMBER ->
                throw new UnsupportedOperationException("TODO");

            case END ->
                "end of document";
        };
    }
}
