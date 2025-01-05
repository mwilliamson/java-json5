package org.zwobble.json5.parser;

import org.zwobble.sourcetext.SourceCharacterIterator;
import org.zwobble.sourcetext.SourcePosition;
import org.zwobble.sourcetext.SourceRange;
import org.zwobble.sourcetext.SourceText;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Optional;

class Json5Tokenizer {
    private Json5Tokenizer() {
    }

    static TokenIterator tokenize(SourceText sourceText) {
        var iterator = new CharacterIterator(sourceText);
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
                if (token.isPresent()) {
                    tokens.add(token.get());
                } else {
                    throw Json5ParseError.unexpectedTextError(
                        "JSON5 token",
                        describeCharacter(iterator.peek()),
                        iterator.characterSourceRange()
                    );
                }
            }
        }

        var tokenEnd = new Json5Token(
            Json5TokenType.END,
            iterator.characterSourceRange()
        );

        return new TokenIterator(tokens, tokenEnd);
    }

    private static boolean trySkipWhiteSpace(CharacterIterator characters) {
        var whitespace = false;

        while (isWhiteSpace(characters.peek())) {
            characters.skip();
            whitespace = true;
        }

        return whitespace;
    }

    private static boolean isWhiteSpace(int character) {
        // WhiteSpace ::
        //     <TAB>
        //     <VT>
        //     <FF>
        //     <SP>
        //     <NBSP>
        //     <BOM>
        //     <USP>

        return character == '\t' ||
            character == 0xb ||
            character == '\f' ||
            character == ' ' ||
            character == 0xa0 ||
            character == 0xfeff ||
            Character.getType(character) == Character.SPACE_SEPARATOR;
    }

    private static boolean trySkipLineTerminators(CharacterIterator characters) {
        var skipped = false;

        while (isLineTerminator(characters.peek())) {
            characters.skip();
            skipped = true;
        }

        return skipped;
    }

    private static boolean isLineTerminator(int character) {
        // LineTerminator ::
        //     <LF>
        //     <CR>
        //     <LS>
        //     <PS>

        return character == '\n' ||
            character == '\r' ||
            character == '\u2028' ||
            character == '\u2029';
    }

    private static boolean trySkipComment(CharacterIterator characters) {
        // Comment ::
        //     MultiLineComment
        //     SingleLineComment

        return trySkipMultiLineComment(characters) || trySkipSingleLineComment(characters);
    }

    private static boolean trySkipMultiLineComment(CharacterIterator characters) {
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

        if (!characters.trySkip(BUFFER_FORWARD_SLASH_ASTERISK)) {
            return false;
        }

        while (!characters.trySkip(BUFFER_ASTERISK_FORWARD_SLASH)) {
            if (characters.isEnd()) {
                var sourceRange = characters.characterSourceRange();
                throw Json5ParseError.unexpectedTextError("'*/'", "end of document", sourceRange);
            }
            characters.skip();
        }

        return true;
    }

    private static final CharBuffer BUFFER_FORWARD_SLASH_ASTERISK = CharBuffer.wrap("/*");
    private static final CharBuffer BUFFER_ASTERISK_FORWARD_SLASH = CharBuffer.wrap("*/");

    private static boolean trySkipSingleLineComment(CharacterIterator characters) {
        // SingleLineComment ::
        //     `//` SingleLineCommentChars?
        //
        // SingleLineCommentChars ::
        //     SingleLineCommentChar SingleLineCommentChars?
        //
        // SingleLineCommentChar ::
        //     SourceCharacter but not LineTerminator

        if (!characters.trySkip(BUFFER_DOUBLE_FORWARD_SLASH)) {
            return false;
        }

        while (!isLineTerminator(characters.peek()) && !characters.isEnd()) {
            characters.skip();
        }

        return true;
    }

    private static final CharBuffer BUFFER_DOUBLE_FORWARD_SLASH = CharBuffer.wrap("//");

    private static Optional<Json5Token> tokenizeJson5Token(CharacterIterator characters) {
        // JSON5Token ::
        //     JSON5Identifier
        //     JSON5Punctuator
        //     JSON5String
        //     JSON5Number

        var json5Identifier = tokenizeJson5Identifier(characters);
        if (json5Identifier.isPresent()) {
            return json5Identifier;
        }

        var json5Punctuator = tokenizeJson5Punctuator(characters);
        if (json5Punctuator.isPresent()) {
            return json5Punctuator;
        }

        var json5String = tokenizeJson5String(characters);
        if (json5String.isPresent()) {
            return json5String;
        }

        var json5Number = tokenizeJson5Number(characters);
        if (json5Number.isPresent()) {
            return json5Number;
        }

        return Optional.empty();
    }

    private static Optional<Json5Token> tokenizeJson5Identifier(CharacterIterator characters) {
        // JSON5Identifier ::
        //     IdentifierName
        //
        // IdentifierName ::
        //     IdentifierStart
        //     IdentifierName IdentifierPart

        if (!trySkipIdentifierStart(characters)) {
            return Optional.empty();
        }

        while (trySkipIdentifierPart(characters)) {
        }

        var token = createToken(characters, Json5TokenType.IDENTIFIER);

        return Optional.of(token);
    }

    private static boolean trySkipIdentifierStart(CharacterIterator characters) {
        // IdentifierStart ::
        //     UnicodeLetter
        //     `$`
        //     `_`
        //     `\` UnicodeEscapeSequence

        var first = characters.peek();
        if (isUnicodeLetter(first) || first == '$' || first == '_') {
            characters.skip();
            return true;
        } else if (first == '\\') {
            characters.skip();
            skipUnicodeEscapeSequence(characters);
            return true;
        } else {
            return false;
        }
    }

    private static boolean isIdentifierStart(CharacterIterator characters) {
        var position = characters.position();
        if (trySkipIdentifierStart(characters)) {
            characters.position(position);
            return true;
        } else {
            return false;
        }
    }

    private static boolean trySkipIdentifierPart(CharacterIterator characters) {
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

        if (trySkipIdentifierStart(characters)) {
            return true;
        }

        var character = characters.peek();
        var mask = (1 << Character.NON_SPACING_MARK) |
            (1 << Character.COMBINING_SPACING_MARK) |
            (1 << Character.DECIMAL_DIGIT_NUMBER) |
            (1 << Character.CONNECTOR_PUNCTUATION);
        if (((mask >> Character.getType(character)) & 1) != 0) {
            characters.skip();
            return true;
        } else if (character == 0x200c || character == 0x200d) {
            characters.skip();
            return true;
        } else {
            return false;
        }
    }

    private static boolean isUnicodeLetter(int character) {
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
        return ((mask >> Character.getType(character)) & 1) != 0;
    }

    private static Optional<Json5Token> tokenizeJson5Punctuator(CharacterIterator characters) {
        // JSON5Punctuator :: one of
        //     `{` `}` `[` `]` `:` `,`

        Json5TokenType tokenType;
        switch (characters.peek()) {
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

        characters.skip();

        var token = createToken(characters, tokenType);
        return Optional.of(token);
    }

    private static Optional<Json5Token> tokenizeJson5String(CharacterIterator characters) {
        // JSON5String ::
        //     `"` JSON5DoubleStringCharacters? `"`
        //     `'` JSON5SingleStringCharacters? `'`
        //
        // JSON5DoubleStringCharacters ::
        //     JSON5DoubleStringCharacter JSON5DoubleStringCharacters?
        //
        // JSON5SingleStringCharacters ::
        //     JSON5SingleStringCharacter JSON5SingleStringCharacters?

        if (characters.trySkip('"')) {
            while (trySkipJson5DoubleStringCharacter(characters)) {
            }
            if (characters.trySkip('"')) {
                return Optional.of(createToken(characters, Json5TokenType.STRING));
            } else {
                throw Json5ParseError.unexpectedTextError(
                    "string character or '\"'",
                    describeCharacter(characters.peek()),
                    characters.characterSourceRange()
                );
            }
        } else if (characters.trySkip('\'')) {
            while (trySkipJson5SingleStringCharacter(characters)) {
            }
            if (characters.trySkip('\'')) {
                return Optional.of(createToken(characters, Json5TokenType.STRING));
            } else {
                throw Json5ParseError.unexpectedTextError(
                    "string character or '\\''",
                    describeCharacter(characters.peek()),
                    characters.characterSourceRange()
                );
            }
        } else {
            return Optional.empty();
        }
    }

    private static boolean trySkipJson5DoubleStringCharacter(CharacterIterator characters) {
        return trySkipJson5StringCharacter(characters, StringType.DOUBLE_STRING);
    }

    private static boolean trySkipJson5SingleStringCharacter(CharacterIterator characters) {
        return trySkipJson5StringCharacter(characters, StringType.SINGLE_STRING);
    }

    private enum StringType {
        DOUBLE_STRING,
        SINGLE_STRING,
    }

    private static boolean trySkipJson5StringCharacter(
        CharacterIterator characters,
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

        var character = characters.peek();
        switch (character) {
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
                characters.skip();
                if (trySkipEscapeSequenceOrLineTerminatorSequence(characters)) {
                    return true;
                } else {
                    throw new Json5ParseError(
                        "Expected escape sequence or line terminator, but was " + describeCharacter(characters.peek()),
                        characters.characterSourceRange()
                    );
                }

            case '\n':
            case '\r':
            case -1:
                return false;
        }

        characters.skip();
        return true;
    }

    private static boolean trySkipEscapeSequenceOrLineTerminatorSequence(CharacterIterator characters) {
        // EscapeSequence ::
        //     CharacterEscapeSequence
        //     `0` [lookahead ∉ DecimalDigit]
        //     HexEscapeSequence
        //     UnicodeEscapeSequence
        //
        // SingleEscapeCharacter :: one of
        //     `'` `"` `\` `b` `f` `n` `r` `t` `v`
        //
        // NonEscapeCharacter ::
        //     SourceCharacter but not one of EscapeCharacter or LineTerminator
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

        switch (characters.peek()) {
            // LineTerminatorSequence

            case '\n':
            case '\u2028':
            case '\u2029':
                characters.skip();
                return true;

            case '\r':
                characters.skip();
                characters.trySkip('\n');
                return true;

            // EscapeSequence

            case '0':
                characters.skip();
                if (isDecimalDigit(characters.peek())) {
                    throw new Json5ParseError(
                        "'\\0' cannot be followed by decimal digit",
                        characters.characterSourceRange()
                    );
                }
                return true;

            case 'x':
                characters.skip();
                skipHexDigit(characters);
                skipHexDigit(characters);
                return true;

            case 'u':
                characters.skip();
                skipHexDigit(characters);
                skipHexDigit(characters);
                skipHexDigit(characters);
                skipHexDigit(characters);
                return true;

            case -1:
                return false;

            default:
                characters.skip();
                return true;
        }
    }

    private static void skipUnicodeEscapeSequence(CharacterIterator characters) {
        // UnicodeEscapeSequence ::
        //     `u` HexDigit HexDigit HexDigit HexDigit

        characters.skip('u');
        skipHexDigit(characters);
        skipHexDigit(characters);
        skipHexDigit(characters);
        skipHexDigit(characters);
    }

    private static Optional<Json5Token> tokenizeJson5Number(CharacterIterator characters) {
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

        var hasPlusSign = characters.trySkip('+');
        var isNegative = false;

        if (!hasPlusSign) {
            isNegative = characters.trySkip('-');
        }

        if (characters.trySkip(BUFFER_INFINITY)) {
            var tokenType = isNegative
                ? Json5TokenType.NUMBER_NEGATIVE_INFINITY
                : Json5TokenType.NUMBER_POSITIVE_INFINITY;

            var token = createToken(characters, tokenType);
            return Optional.of(token);
        }

        if (characters.trySkip(BUFFER_NAN)) {
            var token = createToken(characters, Json5TokenType.NUMBER_NAN);
            return Optional.of(token);
        }

        Json5Token token;
        if (trySkipHexIntegerLiteral(characters)) {
            token = createToken(characters, Json5TokenType.NUMBER_HEX);
        } else if (trySkipDecimalLiteral(characters)) {
            token = createToken(characters, Json5TokenType.NUMBER_DECIMAL);
        } else if (hasPlusSign || isNegative) {
            throw Json5ParseError.unexpectedTextError(
                "numeric literal",
                describeCharacter(characters.peek()),
                characters.characterSourceRange()
            );
        } else {
            return Optional.empty();
        }

        if (isIdentifierStart(characters)) {
            throw new Json5ParseError(
                "The source character immediately following a numeric " +
                    "literal must not be the start of an identifier",
                characters.characterSourceRange()
            );
        }

        return Optional.of(token);
    }

    private static final CharBuffer BUFFER_INFINITY = CharBuffer.wrap("Infinity");
    private static final CharBuffer BUFFER_NAN = CharBuffer.wrap("NaN");

    private static boolean trySkipDecimalLiteral(CharacterIterator characters) {
        // DecimalLiteral ::
        //     DecimalIntegerLiteral `.` DecimalDigits? ExponentPart?
        //     `.` DecimalDigits ExponentPart?
        //     DecimalIntegerLiteral ExponentPart?

        if (trySkipDecimalIntegerLiteral(characters)) {
            if (characters.trySkip('.')) {
                trySkipDecimalDigits(characters);
            }
        } else if (characters.trySkip('.')) {
            if (!trySkipDecimalDigits(characters)) {
                throw Json5ParseError.unexpectedTextError(
                    "decimal digit",
                    describeCharacter(characters.peek()),
                    characters.characterSourceRange()
                );
            }
        } else {
            return false;
        }

        trySkipExponentPart(characters);

        return true;
    }

    private static boolean trySkipDecimalIntegerLiteral(CharacterIterator characters) {
        // DecimalIntegerLiteral ::
        //     `0`
        //     NonZeroDigit DecimalDigits?
        //
        // NonZeroDigit :: one of
        //     `1` `2` `3 `4` `5` `6` `7` `8` `9`

        var firstCharacter = characters.peek();
        if (firstCharacter == '0') {
            characters.skip();
            if (trySkipDecimalDigits(characters)) {
                var sourceRange = characters.tokenSourceRange();
                throw new Json5ParseError(
                    "Integer part of number cannot have leading zeroes",
                    sourceRange
                );
            } else {
                return true;
            }
        } else if (firstCharacter >= '1' && firstCharacter <= '9') {
            characters.skip();
            trySkipDecimalDigits(characters);
            return true;
        } else {
            return false;
        }
    }

    private static void skipDecimalDigits(CharacterIterator characters) {
        if (!trySkipDecimalDigits(characters)) {
            throw Json5ParseError.unexpectedTextError(
                "decimal digit",
                describeCharacter(characters.peek()),
                characters.characterSourceRange()
            );
        }
    }

    private static boolean trySkipDecimalDigits(CharacterIterator characters) {
        // DecimalDigits ::
        //     DecimalDigit
        //     DecimalDigits DecimalDigit

        var skipped = false;

        while (true) {
            var character = characters.peek();
            if (isDecimalDigit(character)) {
                characters.skip();
                skipped = true;
            } else {
                return skipped;
            }
        }
    }

    private static boolean isDecimalDigit(int character) {
        // DecimalDigit :: one of
        //     `0` `1` `2` `3 `4` `5` `6` `7` `8` `9`

        return character >= '0' && character <= '9';
    }

    private static void trySkipExponentPart(CharacterIterator characters) {
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

        var character = characters.peek();
        if (!(character == 'e' || character == 'E')) {
            return;
        }
        characters.skip();

        if (!characters.trySkip('+')) {
            characters.trySkip('-');
        }

        skipDecimalDigits(characters);
    }

    private static boolean trySkipHexIntegerLiteral(CharacterIterator characters) {
        // HexIntegerLiteral ::
        //     `0x` HexDigit
        //     `0X` HexDigit
        //     HexIntegerLiteral HexDigit

        if (
            characters.trySkip(BUFFER_HEX_INTEGER_LITERAL_PREFIX_LOWERCASE) ||
                characters.trySkip(BUFFER_HEX_INTEGER_LITERAL_PREFIX_UPPERCASE)
        ) {
            skipHexDigit(characters);
            while (trySkipHexDigit(characters)) {
            }
            return true;
        } else {
            return false;
        }
    }

    private static final CharBuffer BUFFER_HEX_INTEGER_LITERAL_PREFIX_LOWERCASE = CharBuffer.wrap("0x");
    private static final CharBuffer BUFFER_HEX_INTEGER_LITERAL_PREFIX_UPPERCASE = CharBuffer.wrap("0X");

    private static void skipHexDigit(CharacterIterator characters) {
        if (!trySkipHexDigit(characters)) {
            var sourceRange = characters.characterSourceRange();
            throw Json5ParseError.unexpectedTextError(
                "hex digit",
                describeCharacter(characters.peek()),
                sourceRange
            );
        }
    }

    private static boolean trySkipHexDigit(CharacterIterator characters) {
        // HexDigit :: one of
        //     `0` `1` `2` `3` `4` `5` `6` `7` `8` `9` `a` `b` `c` `d` `e` `f` `A` `B` `C` `D` `E` `F`

        var character = characters.peek();
        if (
            (character >= '0' && character <= '9') ||
                (character >= 'a' && character <= 'f') ||
                (character >= 'A' && character <= 'F')
        ) {
            characters.skip();
            return true;
        } else {
            return false;
        }
    }

    private static String describeCharacter(int character) {
        // TODO: handle characters that should be escaped
        if (character == -1) {
            return "end of document";
        } else {
            return String.format("'%s'", new String(new int[]{character}, 0, 1));
        }
    }

    private static Json5Token createToken(
        CharacterIterator characters,
        Json5TokenType tokenType
    ) {
        var sourceRange = characters.tokenSourceRange();
        return new Json5Token(tokenType, sourceRange);
    }

    private static class CharacterIterator {
        private final SourceCharacterIterator iterator;
        private SourcePosition tokenStartPosition;

        private CharacterIterator(SourceText sourceText) {
            this.iterator = sourceText.characterIterator();
            this.tokenStartPosition = this.iterator.position();
        }

        private boolean isEnd() {
            return this.iterator.isEnd();
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
            if (this.iterator.remaining() < skip.length()) {
                return false;
            }

            if (this.iterator.peekSequence(skip.length()).equals(skip)) {
                this.iterator.skip(skip.length());
                return true;
            } else {
                return false;
            }
        }

        int peek() {
            return this.iterator.peek();
        }

        void skip() {
            this.iterator.skip();
        }

        void skip(int expectedCharacter) {
            var actualCharacter = peek();
            if (actualCharacter == expectedCharacter) {
                skip();
            } else {
                throw Json5ParseError.unexpectedTextError(
                    describeCharacter(expectedCharacter),
                    describeCharacter(actualCharacter),
                    characterSourceRange()
                );
            }
        }

        SourceRange characterSourceRange() {
            return this.iterator.characterSourceRange();
        }

        void startToken() {
            this.tokenStartPosition = this.iterator.position();
        }

        SourceRange tokenSourceRange() {
            var start = this.tokenStartPosition;
            var end = this.iterator.position();
            return iterator.sourceRange(start, end);
        }

        SourcePosition position() {
            return this.iterator.position();
        }

        public void position(SourcePosition position) {
            this.iterator.position(position);
        }
    }
}
