package org.zwobble.json5.parser;

import org.zwobble.json5.paths.Json5Path;
import org.zwobble.json5.values.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.util.ArrayList;
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

        var value = parseValue(tokenIterator, Json5Path.ROOT);

        if (!tokenIterator.isNext(Json5TokenType.END)) {
            throw unexpectedTokenError("end of document", tokenIterator);
        }

        return value;
    }

    private static Json5Value parseValue(TokenIterator tokens, Json5Path path) {
        var json5Value = tryParseValue(tokens, path);
        if (json5Value.isPresent()) {
            return json5Value.get();
        }

        throw unexpectedTokenError("JSON value", tokens);
    }

    private static Optional<Json5Value> tryParseValue(
        TokenIterator tokens,
        Json5Path path
    ) {
        // JSON5Value :
        //     JSON5Null
        //     JSON5Boolean
        //     JSON5String
        //     JSON5Number
        //     JSON5Object
        //     JSON5Array

        var json5Null = tryParseNull(tokens, path);
        if (json5Null.isPresent()) {
            return json5Null;
        }

        var json5Boolean = tryParseBoolean(tokens, path);
        if (json5Boolean.isPresent()) {
            return json5Boolean;
        }

        var json5String = tryParseString(tokens, path);
        if (json5String.isPresent()) {
            return json5String;
        }

        var json5Number = tryParseNumber(tokens, path);
        if (json5Number.isPresent()) {
            return json5Number;
        }

        var json5Object = tryParseObject(tokens, path);
        if (json5Object.isPresent()) {
            return json5Object;
        }

        var json5Array = tryParseArray(tokens, path);
        if (json5Array.isPresent()) {
            return json5Array;
        }

        return Optional.empty();
    }

    private static Optional<Json5Value> tryParseNull(
        TokenIterator tokens,
        Json5Path path
    ) {
        // JSON5Null ::
        //     NullLiteral
        //
        // NullLiteral ::
        //     `null`

        var token = tokens.peek();
        if (token.is(Json5TokenType.IDENTIFIER, BUFFER_NULL)) {
            tokens.skip();
            return Optional.of(new Json5Null(path, token.sourceRange()));
        } else {
            return Optional.empty();
        }
    }

    private static final CharBuffer BUFFER_NULL = CharBuffer.wrap("null");

    private static Optional<Json5Value> tryParseBoolean(
        TokenIterator tokens,
        Json5Path path
    ) {
        // JSON5Boolean ::
        //     BooleanLiteral
        //
        // BooleanLiteral ::
        //     `true`
        //     `false`

        var token = tokens.peek();
        if (token.is(Json5TokenType.IDENTIFIER, BUFFER_TRUE)) {
            tokens.skip();
            return Optional.of(new Json5Boolean(true, path, token.sourceRange()));
        } else if (token.is(Json5TokenType.IDENTIFIER, BUFFER_FALSE)) {
            tokens.skip();
            return Optional.of(new Json5Boolean(false, path, token.sourceRange()));
        } else {
            return Optional.empty();
        }
    }

    private static final CharBuffer BUFFER_TRUE = CharBuffer.wrap("true");
    private static final CharBuffer BUFFER_FALSE = CharBuffer.wrap("false");

    private static Optional<Json5Value> tryParseString(
        TokenIterator tokens,
        Json5Path path
    ) {
        var token = tokens.peek();
        if (token.is(Json5TokenType.STRING)) {
            tokens.skip();

            var stringValue = parseStringValue(token);

            return Optional.of(new Json5String(
                stringValue,
                path,
                token.sourceRange()
            ));
        } else {
            return Optional.empty();
        }
    }

    private static String parseStringValue(Json5Token token) {
        var stringCharacters = token.charBuffer()
            .subSequence(1, token.charBuffer().length() - 1);

        var stringValue = new StringBuilder();
        var index = 0;
        while (index < stringCharacters.remaining()) {
            var character = stringCharacters.charAt(index);
            if (character == '\\') {
                index = parseEscapeSequenceOrLineContinuation(stringCharacters, index, stringValue);
            } else {
                stringValue.append(character);
                index += 1;
            }
        }

        return stringValue.toString();
    }

    private static int parseEscapeSequenceOrLineContinuation(
        CharBuffer stringCharacters,
        int index,
        StringBuilder stringValue
    ) {
        // EscapeSequence ::
        //     CharacterEscapeSequence
        //     `0` [lookahead ∉ DecimalDigit]
        //     HexEscapeSequence
        //     UnicodeEscapeSequence
        //
        // HexEscapeSequence ::
        //     `x` HexDigit HexDigit
        //
        // UnicodeEscapeSequence ::
        //     `u` HexDigit HexDigit HexDigit HexDigit
        //
        // LineTerminatorSequence ::
        //     <LF>
        //     <CR> [lookahead ∉ <LF> ]
        //     <LS>
        //     <PS>
        //     <CR> <LF>

        var character1 = stringCharacters.charAt(index + 1);
        switch (character1) {
            case '\n':
            case '\u2028':
            case '\u2029':
                return index + 2;

            case '\r':
                if (stringCharacters.charAt(index + 2) == '\n') {
                    return index + 3;
                } else {
                    return index + 2;
                }

            case 'b':
                stringValue.append('\b');
                return index + 2;

            case 'f':
                stringValue.append('\f');
                return index + 2;

            case 'n':
                stringValue.append('\n');
                return index + 2;

            case 'r':
                stringValue.append('\r');
                return index + 2;

            case 't':
                stringValue.append('\t');
                return index + 2;

            case 'v':
                stringValue.append('\u000b');
                return index + 2;

            case '0':
                stringValue.append('\0');
                return index + 2;

            case 'x':
                var hexCodeUnit = (parseHexDigit(stringCharacters.charAt(index + 2)) << 4) +
                    parseHexDigit(stringCharacters.charAt(index + 3));
                stringValue.append((char)hexCodeUnit);
                return index + 4;

            case 'u':
                var unicodeCodeUnit = (parseHexDigit(stringCharacters.charAt(index + 2)) << 12) +
                    (parseHexDigit(stringCharacters.charAt(index + 3)) << 8) +
                    (parseHexDigit(stringCharacters.charAt(index + 4)) << 4) +
                    parseHexDigit(stringCharacters.charAt(index + 5));
                stringValue.append((char) unicodeCodeUnit);
                return index + 6;

            default:
                stringValue.append(stringCharacters.charAt(index + 1));
                return index + 2;
        }
    }

    private static Optional<Json5Value> tryParseNumber(
        TokenIterator tokens,
        Json5Path path
    ) {
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
                if (token.charBuffer().equals(BUFFER_INFINITY)) {
                    tokens.skip();
                    return Optional.of(new Json5NumberPositiveInfinity(
                        path,
                        token.sourceRange()
                    ));
                } else if (token.charBuffer().equals(BUFFER_NAN)) {
                    tokens.skip();
                    return Optional.of(new Json5NumberNan(
                        path,
                        token.sourceRange()
                    ));
                }
            }
            case NUMBER_DECIMAL -> {
                tokens.skip();
                return Optional.of(new Json5NumberFinite(
                    new BigDecimal(token.charBuffer().toString()),
                    path,
                    token.sourceRange()
                ));
            }
            case NUMBER_HEX -> {
                tokens.skip();

                var hasSign = false;
                var isNegative = false;

                var buffer = token.charBuffer();
                if (buffer.charAt(0) == '+') {
                    hasSign = true;
                } else if (buffer.charAt(0) == '-') {
                    hasSign = true;
                    isNegative = true;
                }

                var unsignedInteger = new BigInteger(
                    token.charBuffer().toString().substring(hasSign ? 3 : 2),
                    16
                );
                var integer = isNegative ? unsignedInteger.negate() : unsignedInteger;
                return Optional.of(new Json5NumberFinite(
                    new BigDecimal(integer),
                    path,
                    token.sourceRange()
                ));
            }
            case NUMBER_POSITIVE_INFINITY -> {
                tokens.skip();
                return Optional.of(new Json5NumberPositiveInfinity(
                    path,
                    token.sourceRange()
                ));
            }
            case NUMBER_NEGATIVE_INFINITY -> {
                tokens.skip();
                return Optional.of(new Json5NumberNegativeInfinity(
                    path,
                    token.sourceRange()
                ));
            }
            case NUMBER_NAN -> {
                tokens.skip();
                return Optional.of(new Json5NumberNan(
                    path,
                    token.sourceRange()
                ));
            }
        }

        return Optional.empty();
    }

    private static final CharBuffer BUFFER_INFINITY = CharBuffer.wrap("Infinity");
    private static final CharBuffer BUFFER_NAN = CharBuffer.wrap("NaN");

    private static Optional<Json5Value> tryParseObject(
        TokenIterator tokens,
        Json5Path path
    ) {
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

        var objectBuilder = Json5Object.builder();
        while (true) {
            var member = tryParseMember(tokens, path);
            if (member.isPresent()) {
                // TODO: handle duplicates
                objectBuilder.addMember(member.get());
            } else if (tokens.isNext(Json5TokenType.PUNCTUATOR_BRACE_CLOSE)) {
                break;
            } else {
                throw unexpectedTokenError("JSON member or '}'", tokens);
            }

            if (tokens.trySkip(Json5TokenType.PUNCTUATOR_COMMA)) {
                // Next member
            } else if (tokens.isNext(Json5TokenType.PUNCTUATOR_BRACE_CLOSE)) {
                break;
            } else {
                throw unexpectedTokenError("',' or '}'", tokens);
            }
        }

        var endToken = tokens.peek();
        var sourceRange = startToken.sourceRange().to(endToken.sourceRange());
        tokens.skip();
        return Optional.of(objectBuilder.build(path, sourceRange));
    }

    private static Optional<Json5Member> tryParseMember(
        TokenIterator tokens,
        Json5Path path
    ) {
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

        var value = parseValue(tokens, path.member(memberName.get().value()));

        var sourceRange = memberName.get().sourceRange().to(value.sourceRange());

        return Optional.of(new Json5Member(memberName.get(), value, sourceRange));
    }

    private static Optional<Json5MemberName> tryParseMemberName(TokenIterator tokens) {
        //  JSON5MemberName :
        //      JSON5Identifier
        //      JSON5String

        var token = tokens.peek();
        switch (token.tokenType()) {
            case IDENTIFIER -> {
                tokens.skip();
                var name = parseIdentifier(token.charBuffer());
                return Optional.of(new Json5MemberName(name, token.sourceRange()));
            }

            case STRING -> {
                tokens.skip();
                var name = parseStringValue(token);
                return Optional.of(new Json5MemberName(name, token.sourceRange()));
            }

            default -> {
                return Optional.empty();
            }
        }
    }

    private static Optional<Json5Value> tryParseArray(
        TokenIterator tokens,
        Json5Path path
    ) {
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
            var element = tryParseValue(tokens, path.index(elements.size()));
            if (element.isPresent()) {
                elements.add(element.get());
            } else if (tokens.isNext(Json5TokenType.PUNCTUATOR_SQUARE_CLOSE)) {
                break;
            } else {
                throw unexpectedTokenError("JSON value or ']'", tokens);
            }

            if (tokens.trySkip(Json5TokenType.PUNCTUATOR_COMMA)) {
                // Next element
            } else if (tokens.isNext(Json5TokenType.PUNCTUATOR_SQUARE_CLOSE)) {
                break;
            } else {
                throw unexpectedTokenError("',' or ']'", tokens);
            }
        }

        var endToken = tokens.peek();
        var sourceRange = startToken.sourceRange().to(endToken.sourceRange());
        tokens.skip();
        return Optional.of(new Json5Array(elements, path, sourceRange));
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
            token.describe(),
            token.sourceRange()
        );
    }
}
