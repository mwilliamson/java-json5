package org.zwobble.json5.parser;

import org.zwobble.json5.sources.Json5SourcePosition;
import org.zwobble.json5.sources.Json5SourceRange;

import java.nio.CharBuffer;
import java.util.List;

class TokenIterator {
    private final List<Json5Token> tokens;
    private int tokenIndex;
    private final Json5Token tokenEnd;

    TokenIterator(List<Json5Token> tokens) {
        // TODO: create end token so we don't have to synthesise (incorrect) end tokens here.
        this.tokens = tokens;
        this.tokenIndex = 0;
        var lastTokenSourceRange = this.tokens.isEmpty()
            ? new Json5SourceRange(CharBuffer.wrap(""), new Json5SourcePosition(0), new Json5SourcePosition(0))
            : tokens.getLast().sourceRange();
        this.tokenEnd = new Json5Token(
            Json5TokenType.END,
            new Json5SourceRange(
                lastTokenSourceRange.charBuffer(),
                lastTokenSourceRange.end(),
                lastTokenSourceRange.end()
            )
        );
    }

    void skip() {
        this.tokenIndex += 1;
    }

    boolean trySkip(Json5TokenType tokenType) {
        if (peek().tokenType() == tokenType) {
            this.tokenIndex += 1;
            return true;
        } else {
            return false;
        }
    }

    boolean trySkip(Json5TokenType tokenType, CharBuffer buffer) {
        var token = peek();
        if (token.tokenType() == tokenType && token.charBuffer().equals(buffer)) {
            this.tokenIndex += 1;
            return true;
        } else {
            return false;
        }
    }

    Json5Token peek() {
        if (this.tokenIndex >= this.tokens.size()) {
            return tokenEnd;
        }

        return this.tokens.get(this.tokenIndex);
    }
}
