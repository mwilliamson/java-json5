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

        if (codePoints.trySkip('"')) {
            if (codePoints.trySkip('"')) {
                return Optional.of(createToken(codePoints, Json5TokenType.STRING));
            } else {
                throw new UnsupportedOperationException("TODO");
            }
        } else if (codePoints.trySkip('\'')) {
            if (codePoints.trySkip('\'')) {
                return Optional.of(createToken(codePoints, Json5TokenType.STRING));
            } else {
                throw new UnsupportedOperationException("TODO");
            }
        } else {
            return Optional.empty();
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
        // TODO: The source character immediately following a NumericLiteral
        // must not be an IdentifierStart or DecimalDigit.

        // TODO: handle sign not followed by numeric literal

        var isNegative = false;

        if (!codePoints.trySkip('+')) {
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

        if (!trySkipNumericLiteral(codePoints)) {
            return Optional.empty();
        }

        var token = createToken(codePoints, Json5TokenType.NUMBER_FINITE);
        return Optional.of(token);
    }

    private static final CharBuffer BUFFER_INFINITY = CharBuffer.wrap("Infinity");
    private static final CharBuffer BUFFER_NAN = CharBuffer.wrap("NaN");

    private static boolean trySkipNumericLiteral(CodePointIterator codePoints) {
        // NumericLiteral ::
        //     DecimalLiteral
        //     HexIntegerLiteral

        return trySkipDecimalLiteral(codePoints);
    }

    private static boolean trySkipDecimalLiteral(CodePointIterator codePoints) {
        // DecimalLiteral ::
        //     DecimalIntegerLiteral `.` DecimalDigits? ExponentPart?
        //     `.` DecimalDigits ExponentPart?
        //     DecimalIntegerLiteral ExponentPart?

        var firstCodePoint = codePoints.peek();
        if (!(firstCodePoint >= '0' && firstCodePoint <= '9')) {
            return false;
        }

        while (true) {
            var codePoint = codePoints.peek();
            if (codePoint >= '0' && codePoint <= '9') {
                codePoints.skip();
            } else {
                break;
            }
        }

        codePoints.trySkip('.');

        return true;
    }

    private static void skipHexDigit(CodePointIterator codePoints) {
        // HexDigit :: one of
        //     `0` `1` `2` `3` `4` `5` `6` `7` `8` `9` `a` `b` `c` `d` `e` `f` `A` `B` `C` `D` `E` `F`

        var codePoint = codePoints.peek();
        if (
            (codePoint >= '0' && codePoint <= '9') ||
                (codePoint >= 'a' && codePoint <= 'f') ||
                (codePoint >= 'A' && codePoint <= 'F')
        ) {
            codePoints.skip();
        } else {
            var sourceRange = codePoints.codePointSourceRange();
            throw Json5ParseError.unexpectedTextError(
                "hex digit",
                describeCodePoint(codePoint),
                sourceRange
            );
        }
    }

    private static String describeCodePoint(int codePoint) {
        // TODO: handle codepoints that should be escaped
        return String.format("'%s'", new String(new int[]{codePoint}, 0, 1));
    }

    private static Json5Token createToken(
        CodePointIterator codePoints,
        Json5TokenType tokenType
    ) {
        var startCodePointIndex = codePoints.tokenStartIndex;
        var endCodePointIndex = codePoints.index;
        var sourceRange = new Json5SourceRange(startCodePointIndex, endCodePointIndex);

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

        CharBuffer tokenSubBuffer() {
            var startIndex = this.tokenStartIndex;
            this.tokenStartIndex = this.index;
            return this.buffer.subSequence(startIndex, this.index);
        }
    }
}
