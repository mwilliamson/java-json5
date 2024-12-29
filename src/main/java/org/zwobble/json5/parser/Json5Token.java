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
}
