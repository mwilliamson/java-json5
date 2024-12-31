package org.zwobble.json5.parser;

import org.zwobble.json5.sources.Json5SourceRange;
import org.zwobble.json5.values.*;

import java.math.BigDecimal;
import java.math.BigInteger;
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
        var json5Value = tryParseValue(tokens);
        if (json5Value.isPresent()) {
            return json5Value.get();
        }

        throw unexpectedTokenError("JSON value", tokens);
    }

    private static Optional<Json5Value> tryParseValue(TokenIterator tokens) {
        // JSON5Value :
        //     JSON5Null
        //     JSON5Boolean
        //     JSON5String
        //     JSON5Number
        //     JSON5Object
        //     JSON5Array

        var json5Null = tryParseNull(tokens);
        if (json5Null.isPresent()) {
            return json5Null;
        }

        var json5Boolean = tryParseBoolean(tokens);
        if (json5Boolean.isPresent()) {
            return json5Boolean;
        }

        var json5String = tryParseString(tokens);
        if (json5String.isPresent()) {
            return json5String;
        }

        var json5Number = tryParseNumber(tokens);
        if (json5Number.isPresent()) {
            return json5Number;
        }

        var json5Object = tryParseObject(tokens);
        if (json5Object.isPresent()) {
            return json5Object;
        }

        var json5Array = tryParseArray(tokens);
        if (json5Array.isPresent()) {
            return json5Array;
        }

        return Optional.empty();
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

            var stringCharacters = token.buffer()
                .subSequence(1, token.buffer().length() - 1);

            var stringValue = new StringBuilder();
            var index = 0;
            while (index < stringCharacters.remaining()) {
                var character = stringCharacters.charAt(index);
                if (character == '\\') {
                    if (stringCharacters.charAt(index + 1) == '0') {
                        stringValue.append('\0');
                        index += 2;
                    } else if (
                        stringCharacters.charAt(index + 1) == '\r' &&
                            stringCharacters.charAt(index + 2) == '\n'
                    ) {
                        index += 3;
                    } else {
                        index += 2;
                    }
                } else {
                    stringValue.append(character);
                    index += 1;
                }
            }

            return Optional.of(new Json5String(stringValue.toString(), token.sourceRange()));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Json5Value> tryParseNumber(TokenIterator tokens) {
        var token = tokens.peek();

        switch (token.tokenType()) {
            case IDENTIFIER -> {
                // The JSON5 lexical grammar is ambiguous in that Infinity and
                // NaN tokens can be interpreted as either identifiers or as
                // numbers. Since identifiers are always member names, not
                // JSON5 values, this isn't ambiguous in the syntactic grammar.
                //
                // Therefore, to keep identifier handling straightforward, we
                // interpret such tokens as identifiers, and handle the special
                // case when parsing numbers i.e. here.
                if (token.buffer().equals(BUFFER_INFINITY)) {
                    tokens.skip();
                    return Optional.of(new Json5NumberPositiveInfinity(
                        token.sourceRange()
                    ));
                } else if (token.buffer().equals(BUFFER_NAN)) {
                    tokens.skip();
                    return Optional.of(new Json5NumberNan(
                        token.sourceRange()
                    ));
                }
            }
            case NUMBER_DECIMAL -> {
                tokens.skip();
                return Optional.of(new Json5NumberFinite(
                    new BigDecimal(token.buffer().toString()),
                    token.sourceRange()
                ));
            }
            case NUMBER_HEX -> {
                tokens.skip();
                var integer = new BigInteger(
                    token.buffer().toString().substring(2),
                    16
                );
                return Optional.of(new Json5NumberFinite(
                    new BigDecimal(integer),
                    token.sourceRange()
                ));
            }
            case NUMBER_POSITIVE_INFINITY -> {
                tokens.skip();
                return Optional.of(new Json5NumberPositiveInfinity(
                    token.sourceRange()
                ));
            }
            case NUMBER_NEGATIVE_INFINITY -> {
                tokens.skip();
                return Optional.of(new Json5NumberNegativeInfinity(
                    token.sourceRange()
                ));
            }
            case NUMBER_NAN -> {
                tokens.skip();
                return Optional.of(new Json5NumberNan(
                    token.sourceRange()
                ));
            }
        }

        return Optional.empty();
    }

    private static final CharBuffer BUFFER_INFINITY = CharBuffer.wrap("Infinity");
    private static final CharBuffer BUFFER_NAN = CharBuffer.wrap("NaN");

    private static Optional<Json5Value> tryParseObject(TokenIterator tokens) {
        // JSON5Object :
        //     `{` `}`
        //     `{` JSON5MemberList `,`? `}`
        //
        //  JSON5MemberList :
        //      JSON5Member
        //      JSON5MemberList `,` JSON5Member

        var startToken = tokens.peek();
        if (!startToken.is(Json5TokenType.PUNCTUATOR_BRACE_OPEN)) {
            return Optional.empty();
        }
        tokens.skip();

        var members = new LinkedHashMap<String, Json5Member>();
        while (true) {
            var member = tryParseMember(tokens);
            if (member.isPresent()) {
                // TODO: handle duplicates
                members.put(member.get().name().value(), member.get());
            } else if (tokens.trySkip(Json5TokenType.PUNCTUATOR_BRACE_CLOSE)) {
                break;
            } else {
                throw unexpectedTokenError("JSON member or '}'", tokens);
            }

            if (tokens.trySkip(Json5TokenType.PUNCTUATOR_COMMA)) {
                // Next member
            } else if (tokens.trySkip(Json5TokenType.PUNCTUATOR_BRACE_CLOSE)) {
                break;
            } else {
                throw unexpectedTokenError("',' or '}'", tokens);
            }
        }

        var endToken = tokens.peek();
        return Optional.of(new Json5Object(
            members,
            sourceRange(startToken, endToken)
        ));
    }

    private static Optional<Json5Member> tryParseMember(TokenIterator tokens) {
        //  JSON5Member :
        //      JSON5MemberName `:` JSON5Value

        var memberName = tryParseMemberName(tokens);
        if (memberName.isEmpty()) {
            return Optional.empty();
        }

        var colonToken = tokens.peek();
        if (!colonToken.is(Json5TokenType.PUNCTUATOR_COLON)) {
            throw unexpectedTokenError("':'", tokens);
        }
        tokens.skip();

        var value = parseValue(tokens);

        var sourceRange = sourceRange(
            memberName.get().sourceRange(),
            value.sourceRange()
        );

        return Optional.of(new Json5Member(memberName.get(), value, sourceRange));
    }

    private static Optional<Json5MemberName> tryParseMemberName(TokenIterator tokens) {
        //  JSON5MemberName :
        //      JSON5Identifier
        //      JSON5String

        var token = tokens.peek();
        if (token.tokenType() == Json5TokenType.IDENTIFIER) {
            tokens.skip();
            var name = parseIdentifier(token.buffer());
            return Optional.of(new Json5MemberName(name, token.sourceRange()));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Json5Value> tryParseArray(TokenIterator tokens) {
        // JSON5Array :
        //     `[` `]`
        //     `[` JSON5ElementList `,`? `]`
        //
        // JSON5ElementList :
        //     JSON5Value
        //     JSON5ElementList `,` JSON5Value

        var startToken = tokens.peek();
        if (!startToken.is(Json5TokenType.PUNCTUATOR_SQUARE_OPEN)) {
            return Optional.empty();
        }
        tokens.skip();

        var elements = new ArrayList<Json5Value>();
        while (true) {
            var element = tryParseValue(tokens);
            if (element.isPresent()) {
                elements.add(element.get());
            } else if (tokens.trySkip(Json5TokenType.PUNCTUATOR_SQUARE_CLOSE)) {
                break;
            } else {
                throw unexpectedTokenError("JSON value or ']'", tokens);
            }

            if (tokens.trySkip(Json5TokenType.PUNCTUATOR_COMMA)) {
                // Next element
            } else if (tokens.trySkip(Json5TokenType.PUNCTUATOR_SQUARE_CLOSE)) {
                break;
            } else {
                throw unexpectedTokenError("',' or ']'", tokens);
            }
        }

        var endToken = tokens.peek();
        return Optional.of(new Json5Array(
            elements,
            sourceRange(startToken, endToken)
        ));
    }

    private static String parseIdentifier(CharBuffer buffer) {
        // We assume that any buffer is a valid identifier.
        var identifier = new StringBuilder();
        var index = 0;
        while (index < buffer.length()) {
            var rawCharacter = buffer.charAt(index);
            if (rawCharacter == '\\') {
                var character =
                    (parseHexDigit(buffer.charAt(index + 2)) << 12) +
                        (parseHexDigit(buffer.charAt(index + 3)) << 8) +
                        (parseHexDigit(buffer.charAt(index + 4)) << 4) +
                        parseHexDigit(buffer.charAt(index + 5));
                identifier.append((char)character);
                index += 6;
            } else {
                identifier.append(rawCharacter);
                index++;
            }
        }
        return identifier.toString();
    }

    private static int parseHexDigit(char digit) {
        if (digit >= '0' && digit <= '9') {
            return digit - '0';
        } else if (digit >= 'a' && digit <= 'f') {
            return digit - 'a' + 10;
        } else {
            return digit - 'A' + 10;
        }
    }

    private static Json5ParseError unexpectedTokenError(String expected, TokenIterator tokens) {
        var token = tokens.peek();
        return Json5ParseError.unexpectedTextError(
            expected,
            describeToken(token),
            token.sourceRange()
        );
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

            case PUNCTUATOR_COLON ->
                "':'";

            case PUNCTUATOR_COMMA ->
                "','";

            case STRING ->
                throw new UnsupportedOperationException("TODO");

            case NUMBER_DECIMAL ->
                throw new UnsupportedOperationException("TODO");

            case NUMBER_HEX ->
                throw new UnsupportedOperationException("TODO");

            case NUMBER_POSITIVE_INFINITY ->
                throw new UnsupportedOperationException("TODO");

            case NUMBER_NEGATIVE_INFINITY ->
                throw new UnsupportedOperationException("TODO");

            case NUMBER_NAN ->
                throw new UnsupportedOperationException("TODO");

            case END ->
                "end of document";
        };
    }

    private static Json5SourceRange sourceRange(
        Json5Token start,
        Json5Token end
    ) {
        return sourceRange(start.sourceRange(), end.sourceRange());
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
