package org.zwobble.json5.parser;

import org.zwobble.json5.sources.Json5SourceRange;
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

        if (tokenIterator.peek().tokenType() != Json5TokenType.END) {
            throw unexpectedTokenError("end of document", tokenIterator);
        }

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

        throw unexpectedTokenError("JSON value", tokens);
    }

    private static Optional<Json5Value> tryParseNull(TokenIterator tokens) {
        // JSON5Null ::
        //     NullLiteral
        //
        // NullLiteral ::
        //     `null`

        var token = tokens.peek();
        if (token.is(Json5TokenType.IDENTIFIER, BUFFER_NULL)) {
            tokens.skip();
            return Optional.of(new Json5Null(token.sourceRange()));
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

        var token = tokens.peek();
        if (token.is(Json5TokenType.IDENTIFIER, BUFFER_TRUE)) {
            tokens.skip();
            return Optional.of(new Json5Boolean(true, token.sourceRange()));
        } else if (token.is(Json5TokenType.IDENTIFIER, BUFFER_FALSE)) {
            tokens.skip();
            return Optional.of(new Json5Boolean(false, token.sourceRange()));
        } else {
            return Optional.empty();
        }
    }

    private static final CharBuffer BUFFER_TRUE = CharBuffer.wrap("true");
    private static final CharBuffer BUFFER_FALSE = CharBuffer.wrap("false");

    private static Optional<Json5Value> tryParseString(TokenIterator tokens) {
        var token = tokens.peek();
        if (token.is(Json5TokenType.STRING)) {
            tokens.skip();
            return Optional.of(new Json5String("", token.sourceRange()));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Json5Value> tryParseNumber(TokenIterator tokens) {
        var token = tokens.peek();
        if (token.is(Json5TokenType.NUMBER)) {
            tokens.skip();
            return Optional.of(new Json5Number("0", token.sourceRange()));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Json5Value> tryParseObject(TokenIterator tokens) {
        // JSON5Object ::
        //     `{` `}`
        //     `{` JSON5MemberList `,`? `}`

        var startToken = tokens.peek();
        if (!startToken.is(Json5TokenType.PUNCTUATOR_BRACE_OPEN)) {
            return Optional.empty();
        }
        tokens.skip();

        if (tokens.trySkip(Json5TokenType.PUNCTUATOR_BRACE_CLOSE)) {
            var endToken = tokens.peek();
            return Optional.of(new Json5Object(
                new LinkedHashMap<>(),
                sourceRange(
                    startToken.sourceRange(),
                    endToken.sourceRange()
                )
            ));
        }

        throw unexpectedTokenError("JSON value or '}'", tokens);
    }

    private static Optional<Json5Value> tryParseArray(TokenIterator tokens) {
        // JSON5Array ::
        //     `[` `]`
        //     `[` JSON5ElementList `,`? `]`

        var startToken = tokens.peek();
        if (!startToken.is(Json5TokenType.PUNCTUATOR_SQUARE_OPEN)) {
            return Optional.empty();
        }
        tokens.skip();

        if (tokens.trySkip(Json5TokenType.PUNCTUATOR_SQUARE_CLOSE)) {
            var endToken = tokens.peek();
            return Optional.of(new Json5Array(
                new ArrayList<>(),
                sourceRange(
                    startToken.sourceRange(),
                    endToken.sourceRange()
                )
            ));
        }

        throw unexpectedTokenError("JSON value or ']'", tokens);
    }

    private static Json5ParseError unexpectedTokenError(String expected, TokenIterator tokens) {
        var token = tokens.peek();
        var message = String.format(
            "Expected %s, but was %s",
            expected,
            describeToken(token)
        );
        return new Json5ParseError(message, token.sourceRange());
    }

    private static String describeToken(Json5Token tokens) {
        return switch (tokens.tokenType()) {
            case IDENTIFIER ->
                throw new UnsupportedOperationException("TODO");

            case PUNCTUATOR_BRACE_OPEN ->
                "'{'";

            case PUNCTUATOR_BRACE_CLOSE ->
                "'}'";

            case PUNCTUATOR_SQUARE_OPEN ->
                "'['";

            case PUNCTUATOR_SQUARE_CLOSE ->
                "']'";

            case STRING ->
                throw new UnsupportedOperationException("TODO");

            case NUMBER ->
                throw new UnsupportedOperationException("TODO");

            case END ->
                "end of document";
        };
    }

    private static Json5SourceRange sourceRange(
        Json5SourceRange start,
        Json5SourceRange end
    ) {
        return new Json5SourceRange(
            start.startCodePointIndex(),
            end.endCodePointIndex()
        );
    }
}
