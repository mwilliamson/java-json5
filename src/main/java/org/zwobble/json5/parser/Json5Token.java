package org.zwobble.json5.parser;

import org.zwobble.json5.sources.Json5SourceRange;

import java.nio.CharBuffer;

record Json5Token(
    Json5TokenType tokenType,
    CharBuffer buffer,
    Json5SourceRange sourceRange
) {
    public boolean is(Json5TokenType tokenType) {
        return this.tokenType == tokenType;
    }

    boolean is(Json5TokenType tokenToken, CharBuffer buffer) {
        return this.tokenType == tokenToken && this.buffer.equals(buffer);
    }

    String describe() {
        return switch (this.tokenType()) {
            case IDENTIFIER ->
                String.format("identifier '%s'", this.buffer);

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
                String.format("string %s", this.buffer);

            case NUMBER_DECIMAL, NUMBER_HEX, NUMBER_POSITIVE_INFINITY, NUMBER_NEGATIVE_INFINITY, NUMBER_NAN ->
                String.format("number '%s'", this.buffer);

            case END ->
                "end of document";
        };
    }
}
