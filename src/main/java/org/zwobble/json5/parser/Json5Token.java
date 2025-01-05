package org.zwobble.json5.parser;

import org.zwobble.sourcetext.SourceRange;

import java.nio.CharBuffer;

record Json5Token(
    Json5TokenType tokenType,
    SourceRange sourceRange
) {
    public boolean is(Json5TokenType tokenType) {
        return this.tokenType == tokenType;
    }

    boolean is(Json5TokenType tokenToken, CharBuffer buffer) {
        return this.tokenType == tokenToken && charBuffer().equals(buffer);
    }

    String describe() {
        return switch (this.tokenType()) {
            case IDENTIFIER ->
                String.format("identifier '%s'", charBuffer());

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
                String.format("string %s", charBuffer());

            case NUMBER_DECIMAL, NUMBER_HEX, NUMBER_POSITIVE_INFINITY, NUMBER_NEGATIVE_INFINITY, NUMBER_NAN ->
                String.format("number '%s'", charBuffer());

            case END ->
                "end of document";
        };
    }

    public CharBuffer charBuffer() {
        return this.sourceRange.charBuffer();
    }
}
