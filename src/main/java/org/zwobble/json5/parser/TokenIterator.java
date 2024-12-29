package org.zwobble.json5.parser;

import java.nio.CharBuffer;
import java.util.List;

class TokenIterator {
    public static final Json5Token TOKEN_END = new Json5Token(
        Json5TokenType.END,
        CharBuffer.wrap("")
    );

    private final List<Json5Token> tokens;
    private int tokenIndex;

    TokenIterator(List<Json5Token> tokens) {
        this.tokens = tokens;
        this.tokenIndex = 0;
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
        if (token.tokenType() == tokenType && token.buffer().equals(buffer)) {
            this.tokenIndex += 1;
            return true;
        } else {
            return false;
        }
    }

    Json5Token peek() {
        if (this.tokenIndex >= this.tokens.size()) {
            return TOKEN_END;
        }

        return this.tokens.get(this.tokenIndex);
    }
}
