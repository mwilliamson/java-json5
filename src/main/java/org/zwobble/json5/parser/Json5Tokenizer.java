package org.zwobble.json5.parser;

import org.zwobble.json5.sources.Json5SourceRange;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class Json5Tokenizer {
    private Json5Tokenizer() {
    }

    static List<Json5Token> tokenize(String text) {
        var iterator = new CodePointIterator(text);
        var tokens = new ArrayList<Json5Token>();

        while (!iterator.isEnd()) {
            // JSON5InputElement ::
            //     WhiteSpace
            //     LineTerminator
            //     Comment
            //     JSON5Token
            if (trySkipWhiteSpace(iterator)) {
                // Skip whitespace.
            } else if (trySkipLineTerminators(iterator)) {
                // Skip line terminator.
            } else if (trySkipComment(iterator)) {
                // Skip comment
            } else {
                iterator.startToken();
                var token = tokenizeJson5Token(iterator);
                // TODO: handle no token
                tokens.add(token.get());
            }
        }

        return tokens;
    }

    private static boolean trySkipWhiteSpace(CodePointIterator codePoints) {
        var whitespace = false;

        while (isWhiteSpace(codePoints.peek())) {
            codePoints.skip();
            whitespace = true;
        }

        return whitespace;
    }

    private static boolean isWhiteSpace(int codePoint) {
        // WhiteSpace ::
        //     <TAB>
        //     <VT>
        //     <FF>
        //     <SP>
        //     <NBSP>
        //     <BOM>
        //     <USP>

        return codePoint == '\t' ||
            codePoint == 0xb ||
            codePoint == '\f' ||
            codePoint == ' ' ||
            codePoint == 0xa0 ||
            codePoint == 0xfeff ||
            Character.getType(codePoint) == Character.SPACE_SEPARATOR;
    }

    private static boolean trySkipLineTerminators(CodePointIterator codePoints) {
        var skipped = false;

        while (isLineTerminator(codePoints.peek())) {
            codePoints.skip();
            skipped = true;
        }

        return skipped;
    }

    private static boolean isLineTerminator(int codePoint) {
        // LineTerminator ::
        //     <LF>
        //     <CR>
        //     <LS>
        //     <PS>

        return codePoint == '\n' ||
            codePoint == '\r' ||
            codePoint == '\u2028' ||
            codePoint == '\u2029';
    }

    private static boolean trySkipComment(CodePointIterator codePoints) {
        // Comment ::
        //     MultiLineComment
        //     SingleLineComment

        return trySkipMultiLineComment(codePoints) || trySkipSingleLineComment(codePoints);
    }

    private static boolean trySkipMultiLineComment(CodePointIterator codePoints) {
        // MultiLineComment ::
        //     `/*` MultiLineCommentChars? `*/`
        //
        // MultiLineCommentChars ::
        //     MultiLineNotAsteriskChar MultiLineCommentChars?
        //     `*` PostAsteriskCommentChars?
        //
        // PostAsteriskCommentChars ::
        //     MultiLineNotForwardSlashOrAsteriskChar MultiLineCommentChars?
        //     * PostAsteriskCommentChars?
        //
        // MultiLineNotAsteriskChar ::
        //     SourceCharacter but not `*`
        //
        // MultiLineNotForwardSlashOrAsteriskChar ::
        //     SourceCharacter but not one of `/` or `*`

        // TODO: if a MultiLineComment contains a line terminator character,
        // then the entire comment is considered to be a LineTerminator for
        // purposes of parsing by the syntactic grammar.

        if (!codePoints.trySkip(BUFFER_FORWARD_SLASH_ASTERISK)) {
            return false;
        }

        while (!codePoints.trySkip(BUFFER_ASTERISK_FORWARD_SLASH)) {
            if (codePoints.isEnd()) {
                var sourceRange = new Json5SourceRange(codePoints.index, codePoints.index);
                throw Json5ParseError.unexpectedTextError("'*/'", "end of document", sourceRange);
            }
            codePoints.skip();
        }

        return true;
    }

    private static final CharBuffer BUFFER_FORWARD_SLASH_ASTERISK = CharBuffer.wrap("/*");
    private static final CharBuffer BUFFER_ASTERISK_FORWARD_SLASH = CharBuffer.wrap("*/");

    private static boolean trySkipSingleLineComment(CodePointIterator codePoints) {
        // SingleLineComment ::
        //     `//` SingleLineCommentChars?
        //
        // SingleLineCommentChars ::
        //     SingleLineCommentChar SingleLineCommentChars?
        //
        // SingleLineCommentChar ::
        //     SourceCharacter but not LineTerminator

        if (!codePoints.trySkip(BUFFER_DOUBLE_FORWARD_SLASH)) {
            return false;
        }

        while (!isLineTerminator(codePoints.peek()) && !codePoints.isEnd()) {
            codePoints.skip();
        }

        return true;
    }

    private static final CharBuffer BUFFER_DOUBLE_FORWARD_SLASH = CharBuffer.wrap("//");

    private static Optional<Json5Token> tokenizeJson5Token(CodePointIterator codePoints) {
        // JSON5Token ::
        //     JSON5Identifier
        //     JSON5Punctuator
        //     JSON5String
        //     JSON5Number

        var json5Identifier = tokenizeJson5Identifier(codePoints);
        if (json5Identifier.isPresent()) {
            return json5Identifier;
        }

        var json5Punctuator = tokenizeJson5Punctuator(codePoints);
        if (json5Punctuator.isPresent()) {
            return json5Punctuator;
        }

        var json5String = tokenizeJson5String(codePoints);
        if (json5String.isPresent()) {
            return json5String;
        }

        var json5Number = tokenizeJson5Number(codePoints);
        if (json5Number.isPresent()) {
            return json5Number;
        }

        return Optional.empty();
    }

    private static Optional<Json5Token> tokenizeJson5Identifier(CodePointIterator codePoints) {
        // JSON5Identifier ::
        //     IdentifierName
        //
        // IdentifierName ::
        //     IdentifierStart
        //     IdentifierName IdentifierPart

        if (!trySkipIdentifierStart(codePoints)) {
            return Optional.empty();
        }

        while (trySkipIdentifierPart(codePoints)) {
        }

        var token = createToken(codePoints, Json5TokenType.IDENTIFIER);

        return Optional.of(token);
    }

    private static boolean trySkipIdentifierStart(CodePointIterator codePoints) {
        // IdentifierStart ::
        //     UnicodeLetter
        //     `$`
        //     `_`
        //     `\` UnicodeEscapeSequence

        var first = codePoints.peek();
        if (isUnicodeLetter(first) || first == '$' || first == '_') {
            codePoints.skip();
            return true;
        } else if (first == '\\') {
            codePoints.skip();
            skipUnicodeEscapeSequence(codePoints);
            return true;
        } else {
            return false;
        }
    }

    private static boolean isIdentifierStart(CodePointIterator codePoints) {
        var index = codePoints.index;
        if (trySkipIdentifierStart(codePoints)) {
            codePoints.index = index;
            return true;
        } else {
            return false;
        }
    }

    private static boolean trySkipIdentifierPart(CodePointIterator codePoints) {
        // IdentifierPart ::
        //     IdentifierStart
        //     UnicodeCombiningMark
        //     UnicodeDigit
        //     UnicodeConnectorPunctuation
        //     <ZWNJ>
        //     <ZWJ>
        //
        // UnicodeCombiningMark ::
        //     any character in the Unicode categories “Non-spacing mark (Mn)”
        //     or “Combining spacing mark (Mc)”
        //
        // UnicodeDigit ::
        //     any character in the Unicode category “Decimal number (Nd)”
        //
        // UnicodeConnectorPunctuation ::
        //     any character in the Unicode category “Connector punctuation (Pc)”

        if (trySkipIdentifierStart(codePoints)) {
            return true;
        }

        var codePoint = codePoints.peek();
        var mask = (1 << Character.NON_SPACING_MARK) |
            (1 << Character.COMBINING_SPACING_MARK) |
            (1 << Character.DECIMAL_DIGIT_NUMBER) |
            (1 << Character.CONNECTOR_PUNCTUATION);
        if (((mask >> Character.getType(codePoint)) & 1) != 0) {
            codePoints.skip();
            return true;
        } else if (codePoint == 0x200c || codePoint == 0x200d) {
            codePoints.skip();
            return true;
        } else {
            return false;
        }
    }

    private static boolean isUnicodeLetter(int codePoint) {
        // UnicodeLetter ::
        //     any character in the Unicode categories “Uppercase letter (Lu)”,
        //     “Lowercase letter (Ll)”, “Titlecase letter (Lt)”, “Modifier
        //     letter (Lm)”, “Other letter (Lo)”, or “Letter number (Nl)”.

        var mask = (1 << Character.UPPERCASE_LETTER) |
            (1 << Character.LOWERCASE_LETTER) |
            (1 << Character.TITLECASE_LETTER) |
            (1 << Character.MODIFIER_LETTER) |
            (1 << Character.OTHER_LETTER) |
            (1 << Character.LETTER_NUMBER);
        return ((mask >> Character.getType(codePoint)) & 1) != 0;
    }

    private static Optional<Json5Token> tokenizeJson5Punctuator(CodePointIterator codePoints) {
        // JSON5Punctuator :: one of
        //     `{` `}` `[` `]` `:` `,`

        Json5TokenType tokenType;
        switch (codePoints.peek()) {
            case '{':
                tokenType = Json5TokenType.PUNCTUATOR_BRACE_OPEN;
                break;

            case '}':
                tokenType = Json5TokenType.PUNCTUATOR_BRACE_CLOSE;
                break;

            case '[':
                tokenType = Json5TokenType.PUNCTUATOR_SQUARE_OPEN;
                break;

            case ']':
                tokenType = Json5TokenType.PUNCTUATOR_SQUARE_CLOSE;
                break;

            case ':':
                tokenType = Json5TokenType.PUNCTUATOR_COLON;
                break;

            case ',':
                tokenType = Json5TokenType.PUNCTUATOR_COMMA;
                break;

            default:
                return Optional.empty();
        }

        codePoints.skip();

        var token = createToken(codePoints, tokenType);
        return Optional.of(token);
    }

    private static Optional<Json5Token> tokenizeJson5String(CodePointIterator codePoints) {
        // JSON5String ::
        //     `"` JSON5DoubleStringCharacters? `"`
        //     `'` JSON5SingleStringCharacters? `'`
        //
        // JSON5DoubleStringCharacters ::
        //     JSON5DoubleStringCharacter JSON5DoubleStringCharacters?
        //
        // JSON5SingleStringCharacters ::
        //     JSON5SingleStringCharacter JSON5SingleStringCharacters?

        if (codePoints.trySkip('"')) {
            while (trySkipJson5DoubleStringCharacter(codePoints)) {
            }
            if (codePoints.trySkip('"')) {
                return Optional.of(createToken(codePoints, Json5TokenType.STRING));
            } else {
                throw Json5ParseError.unexpectedTextError(
                    "string character or '\"'",
                    describeCodePoint(codePoints.peek()),
                    codePoints.codePointSourceRange()
                );
            }
        } else if (codePoints.trySkip('\'')) {
            while (trySkipJson5SingleStringCharacter(codePoints)) {
            }
            if (codePoints.trySkip('\'')) {
                return Optional.of(createToken(codePoints, Json5TokenType.STRING));
            } else {
                throw Json5ParseError.unexpectedTextError(
                    "string character or '\\''",
                    describeCodePoint(codePoints.peek()),
                    codePoints.codePointSourceRange()
                );
            }
        } else {
            return Optional.empty();
        }
    }

    private static boolean trySkipJson5DoubleStringCharacter(CodePointIterator codePoints) {
        return trySkipJson5StringCharacter(codePoints, StringType.DOUBLE_STRING);
    }

    private static boolean trySkipJson5SingleStringCharacter(CodePointIterator codePoints) {
        return trySkipJson5StringCharacter(codePoints, StringType.SINGLE_STRING);
    }

    private enum StringType {
        DOUBLE_STRING,
        SINGLE_STRING,
    }

    private static boolean trySkipJson5StringCharacter(
        CodePointIterator codePoints,
        StringType stringType
    ) {
        // JSON5DoubleStringCharacter ::
        //     SourceCharacter but not one of `"` or `\` or LineTerminator
        //     `\` EscapeSequence
        //     LineContinuation
        //     U+2028
        //     U+2029
        //
        // JSON5SingleStringCharacter ::
        //     SourceCharacter but not one of `'` or `\` or LineTerminator
        //     `\` EscapeSequence
        //     LineContinuation
        //     U+2028
        //     U+2029
        //
        // LineContinuation ::
        //     `\` LineTerminatorSequence
        //
        // LineTerminator ::
        //     <LF>
        //     <CR>
        //     <LS>
        //     <PS>

        var codePoint = codePoints.peek();
        switch (codePoint) {
            case '"':
                if (stringType == StringType.DOUBLE_STRING) {
                    return false;
                }
                break;

            case '\'':
                if (stringType == StringType.SINGLE_STRING) {
                    return false;
                }
                break;

            case '\\':
                codePoints.skip();
                if (trySkipLineTerminatorSequence(codePoints)) {
                    return true;
                } else {
                    throw new UnsupportedOperationException("TODO");
                }

            case '\n':
            case '\r':
            case -1:
                return false;
        }

        codePoints.skip();
        return true;
    }

    private static boolean trySkipLineTerminatorSequence(CodePointIterator codePoints) {
        // LineTerminatorSequence ::
        //     <LF>
        //     <CR> [lookahead ∉ <LF> ]
        //     <LS>
        //     <PS>
        //     <CR> <LF>

        switch (codePoints.peek()) {
            case '\n':
            case '\u2028':
            case '\u2029':
                codePoints.skip();
                return true;

            case '\r':
                codePoints.skip();
                codePoints.trySkip('\n');
                return true;

            default:
                return false;
        }
    }

    private static void skipUnicodeEscapeSequence(CodePointIterator codePoints) {
        // UnicodeEscapeSequence ::
        //     `u` HexDigit HexDigit HexDigit HexDigit
        //

        codePoints.skip('u');
        skipHexDigit(codePoints);
        skipHexDigit(codePoints);
        skipHexDigit(codePoints);
        skipHexDigit(codePoints);
    }

    private static Optional<Json5Token> tokenizeJson5Number(CodePointIterator codePoints) {
        // JSON5Number ::
        //     JSON5NumericLiteral
        //     `+` JSON5NumericLiteral
        //     `-` JSON5NumericLiteral
        //
        // JSON5NumericLiteral ::
        //     NumericLiteral
        //     `Infinity`
        //     `NaN`
        //
        // NumericLiteral ::
        //     DecimalLiteral
        //     HexIntegerLiteral
        //
        // The source character immediately following a NumericLiteral
        // must not be an IdentifierStart or DecimalDigit.

        var hasPlusSign = codePoints.trySkip('+');
        var isNegative = false;

        if (!hasPlusSign) {
            isNegative = codePoints.trySkip('-');
        }

        if (codePoints.trySkip(BUFFER_INFINITY)) {
            var tokenType = isNegative
                ? Json5TokenType.NUMBER_NEGATIVE_INFINITY
                : Json5TokenType.NUMBER_POSITIVE_INFINITY;

            var token = createToken(codePoints, tokenType);
            return Optional.of(token);
        }

        if (codePoints.trySkip(BUFFER_NAN)) {
            var token = createToken(codePoints, Json5TokenType.NUMBER_NAN);
            return Optional.of(token);
        }

        Json5Token token;
        if (trySkipHexIntegerLiteral(codePoints)) {
            token = createToken(codePoints, Json5TokenType.NUMBER_HEX);
        } else if (trySkipDecimalLiteral(codePoints)) {
            token = createToken(codePoints, Json5TokenType.NUMBER_DECIMAL);
        } else if (hasPlusSign || isNegative) {
            throw Json5ParseError.unexpectedTextError(
                "numeric literal",
                describeCodePoint(codePoints.peek()),
                codePoints.codePointSourceRange()
            );
        } else {
            return Optional.empty();
        }

        if (isIdentifierStart(codePoints)) {
            throw new Json5ParseError(
                "The source character immediately following a numeric " +
                    "literal must not be the start of an identifier",
                codePoints.codePointSourceRange()
            );
        }

        return Optional.of(token);
    }

    private static final CharBuffer BUFFER_INFINITY = CharBuffer.wrap("Infinity");
    private static final CharBuffer BUFFER_NAN = CharBuffer.wrap("NaN");

    private static boolean trySkipDecimalLiteral(CodePointIterator codePoints) {
        // DecimalLiteral ::
        //     DecimalIntegerLiteral `.` DecimalDigits? ExponentPart?
        //     `.` DecimalDigits ExponentPart?
        //     DecimalIntegerLiteral ExponentPart?

        if (trySkipDecimalIntegerLiteral(codePoints)) {
            if (codePoints.trySkip('.')) {
                trySkipDecimalDigits(codePoints);
            }
        } else if (codePoints.trySkip('.')) {
            if (!trySkipDecimalDigits(codePoints)) {
                throw Json5ParseError.unexpectedTextError(
                    "decimal digit",
                    describeCodePoint(codePoints.peek()),
                    codePoints.codePointSourceRange()
                );
            }
        } else {
            return false;
        }

        trySkipExponentPart(codePoints);

        return true;
    }

    private static boolean trySkipDecimalIntegerLiteral(CodePointIterator codePoints) {
        // DecimalIntegerLiteral ::
        //     `0`
        //     NonZeroDigit DecimalDigits?
        //
        // NonZeroDigit :: one of
        //     `1` `2` `3 `4` `5` `6` `7` `8` `9`

        var firstCodePoint = codePoints.peek();
        if (firstCodePoint == '0') {
            codePoints.skip();
            if (trySkipDecimalDigits(codePoints)) {
                var sourceRange = codePoints.tokenSourceRange();
                throw new Json5ParseError(
                    "Integer part of number cannot have leading zeroes",
                    sourceRange
                );
            } else {
                return true;
            }
        } else if (firstCodePoint >= '1' && firstCodePoint <= '9') {
            codePoints.skip();
            trySkipDecimalDigits(codePoints);
            return true;
        } else {
            return false;
        }
    }

    private static void skipDecimalDigits(CodePointIterator codePoints) {
        if (!trySkipDecimalDigits(codePoints)) {
            throw Json5ParseError.unexpectedTextError(
                "decimal digit",
                describeCodePoint(codePoints.peek()),
                codePoints.codePointSourceRange()
            );
        }
    }

    private static boolean trySkipDecimalDigits(CodePointIterator codePoints) {
        // DecimalDigits ::
        //     DecimalDigit
        //     DecimalDigits DecimalDigit

        var initialIndex = codePoints.index;

        while (true) {
            var codePoint = codePoints.peek();
            if (isDecimalDigit(codePoint)) {
                codePoints.skip();
            } else {
                return initialIndex != codePoints.index;
            }
        }
    }

    private static boolean isDecimalDigit(int codePoint) {
        // DecimalDigit :: one of
        //     `0` `1` `2` `3 `4` `5` `6` `7` `8` `9`

        return codePoint >= '0' && codePoint <= '9';
    }

    private static void trySkipExponentPart(CodePointIterator codePoints) {
        // ExponentPart ::
        //     ExponentIndicator SignedInteger
        //
        // ExponentIndicator :: one of
        //     `e` `E`
        //
        // SignedInteger ::
        //     DecimalDigits
        //     `+` DecimalDigits
        //     `-` DecimalDigits

        var codePoint = codePoints.peek();
        if (!(codePoint == 'e' || codePoint == 'E')) {
            return;
        }
        codePoints.skip();

        if (!codePoints.trySkip('+')) {
            codePoints.trySkip('-');
        }

        skipDecimalDigits(codePoints);
    }

    private static boolean trySkipHexIntegerLiteral(CodePointIterator codePoints) {
        // HexIntegerLiteral ::
        //     `0x` HexDigit
        //     `0X` HexDigit
        //     HexIntegerLiteral HexDigit

        if (
            codePoints.trySkip(BUFFER_HEX_INTEGER_LITERAL_PREFIX_LOWERCASE) ||
                codePoints.trySkip(BUFFER_HEX_INTEGER_LITERAL_PREFIX_UPPERCASE)
        ) {
            skipHexDigit(codePoints);
            while (trySkipHexDigit(codePoints)) {
            }
            return true;
        } else {
            return false;
        }
    }

    private static final CharBuffer BUFFER_HEX_INTEGER_LITERAL_PREFIX_LOWERCASE = CharBuffer.wrap("0x");
    private static final CharBuffer BUFFER_HEX_INTEGER_LITERAL_PREFIX_UPPERCASE = CharBuffer.wrap("0X");

    private static void skipHexDigit(CodePointIterator codePoints) {
        if (!trySkipHexDigit(codePoints)) {
            var sourceRange = codePoints.codePointSourceRange();
            throw Json5ParseError.unexpectedTextError(
                "hex digit",
                describeCodePoint(codePoints.peek()),
                sourceRange
            );
        }
    }

    private static boolean trySkipHexDigit(CodePointIterator codePoints) {
        // HexDigit :: one of
        //     `0` `1` `2` `3` `4` `5` `6` `7` `8` `9` `a` `b` `c` `d` `e` `f` `A` `B` `C` `D` `E` `F`

        var codePoint = codePoints.peek();
        if (
            (codePoint >= '0' && codePoint <= '9') ||
                (codePoint >= 'a' && codePoint <= 'f') ||
                (codePoint >= 'A' && codePoint <= 'F')
        ) {
            codePoints.skip();
            return true;
        } else {
            return false;
        }
    }

    private static String describeCodePoint(int codePoint) {
        // TODO: handle codepoints that should be escaped
        if (codePoint == -1) {
            return "end of document";
        } else {
            return String.format("'%s'", new String(new int[]{codePoint}, 0, 1));
        }
    }

    private static Json5Token createToken(
        CodePointIterator codePoints,
        Json5TokenType tokenType
    ) {
        var sourceRange = codePoints.tokenSourceRange();
        var buffer = codePoints.tokenSubBuffer();
        return new Json5Token(tokenType, buffer, sourceRange);
    }

    private static class CodePointIterator {
        private final CharBuffer buffer;
        private int index;
        private int tokenStartIndex;

        private CodePointIterator(String text) {
            this.buffer = CharBuffer.wrap(text);
            this.index = 0;
            this.tokenStartIndex = 0;
        }

        private boolean isEnd() {
            return this.index >= this.buffer.length();
        }

        private boolean trySkip(char skip) {
            if (peek() == skip) {
                this.skip();
                return true;
            } else {
                return false;
            }
        }

        private boolean trySkip(CharBuffer skip) {
            if (remaining() < skip.length()) {
                return false;
            }

            if (this.buffer.subSequence(this.index, this.index + skip.length()).equals(skip)) {
                this.index += skip.length();
                return true;
            } else {
                return false;
            }
        }

        private int remaining() {
            return this.buffer.length() - this.index;
        }

        int peek() {
            if (this.index >= this.buffer.length()) {
                return -1;
            }

            // TODO: handle codepoints instead of char
            return this.buffer.get(this.index);
        }

        void skip() {
            if (this.index < this.buffer.length()) {
                this.index += 1;
            }
        }

        void skip(int expectedCodePoint) {
            var actualCodePoint = peek();
            if (actualCodePoint == expectedCodePoint) {
                skip();
            } else {
                throw Json5ParseError.unexpectedTextError(
                    describeCodePoint(expectedCodePoint),
                    describeCodePoint(actualCodePoint),
                    codePointSourceRange()
                );
            }
        }

        Json5SourceRange codePointSourceRange() {
            if (isEnd()) {
                return new Json5SourceRange(this.index, this.index);
            } else {
                return new Json5SourceRange(this.index, this.index + 1);
            }
        }

        void startToken() {
            this.tokenStartIndex = this.index;
        }

        Json5SourceRange tokenSourceRange() {
            var startCodePointIndex = this.tokenStartIndex;
            var endCodePointIndex = this.index;
            return new Json5SourceRange(startCodePointIndex, endCodePointIndex);
        }

        CharBuffer tokenSubBuffer() {
            var startIndex = this.tokenStartIndex;
            return this.buffer.subSequence(startIndex, this.index);
        }
    }
}
