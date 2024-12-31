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

            case NUMBER_DECIMAL, NUMBER_HEX ->
                String.format("number '%s'", this.buffer);

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
}
