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
            } else if (trySkipLineTerminator(iterator)) {
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

    private static boolean trySkipLineTerminator(CodePointIterator codePoints) {
        var whitespace = false;

        while (isLineTerminator(codePoints.peek())) {
            codePoints.skip();
            whitespace = true;
        }

        return whitespace;
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
                throw new Json5ParseError("Expected '*/', but was end of document", sourceRange);
            }
            codePoints.skip();
        }

        return true;

        // TODO: handle unclosed comment
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

        while (!isLineTerminator(codePoints.peek())) {
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
        if (isUnicodeLetter(first)) {
            codePoints.skip();
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

        if (trySkipIdentifierStart(codePoints)) {
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

    private static boolean isUnicodeCombiningMark(int codePoint) {
        // UnicodeCombiningMark ::
        //     any character in the Unicode categories “Non-spacing mark (Mn)”
        //     or “Combining spacing mark (Mc)”

        return false;
    }

    private static boolean isUnicodeDigit(int codePoint) {
        // UnicodeDigit ::
        //     any character in the Unicode category “Decimal number (Nd)”

        return false;
    }

    private static boolean isUnicodeConnectorPunctuation(int codePoint) {
        // UnicodeConnectorPunctuation ::
        //     any character in the Unicode category “Connector punctuation (Pc)”

        return false;
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

    private static Optional<Json5Token> tokenizeJson5Number(CodePointIterator codePoints) {
        // JSON5Number ::
        //     JSON5NumericLiteral
        //     `+` JSON5NumericLiteral
        //     `-` JSON5NumericLiteral

        if (!trySkipNumericLiteral(codePoints)) {
            return Optional.empty();
        }

        var token = createToken(codePoints, Json5TokenType.NUMBER);
        return Optional.of(token);
    }

    private static boolean trySkipJson5NumericLiteral(CodePointIterator codePoints) {
        // JSON5NumericLiteral ::
        //     NumericLiteral
        //     `Infinity`
        //     `Nan`
        //
        // TODO: The source character immediately following a NumericLiteral
        // must not be an IdentifierStart or DecimalDigit.

        return trySkipNumericLiteral(codePoints);
    }

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

        return codePoints.trySkip('0');
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
            if (this.buffer.get(this.index) == skip) {
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

        void startToken() {
            this.tokenStartIndex = this.index;
        }

        CharBuffer tokenSubBuffer() {
            var startIndex = this.tokenStartIndex;
            this.tokenStartIndex = this.index;
            return this.buffer.subSequence(startIndex, this.index);
        }
    }

    record CodePointPosition(int bufferIndex) {
    }
}
