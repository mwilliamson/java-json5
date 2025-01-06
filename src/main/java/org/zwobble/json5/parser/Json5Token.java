package org.zwobble.json5.parser;

import org.zwobble.sourcetext.SourceRange;

record Json5Token(
    Json5TokenType tokenType,
    SourceRange sourceRange
) {
    public boolean is(Json5TokenType tokenType) {
        return this.tokenType == tokenType;
    }

    boolean is(Json5TokenType tokenToken, String contents) {
        return is(tokenToken) && is(contents);
    }

    boolean is(String contents) {
        return CharSequence.compare(charSequence(), contents) == 0;
    }

    String describe() {
        return switch (this.tokenType()) {
            case IDENTIFIER ->
                String.format("identifier '%s'", charSequence());

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
                String.format("string %s", charSequence());

            case NUMBER_DECIMAL, NUMBER_HEX, NUMBER_POSITIVE_INFINITY, NUMBER_NEGATIVE_INFINITY, NUMBER_NAN ->
                String.format("number '%s'", charSequence());

            case END ->
                "end of document";
        };
    }

    public CharSequence charSequence() {
        return this.sourceRange.charSequence();
    }
}
