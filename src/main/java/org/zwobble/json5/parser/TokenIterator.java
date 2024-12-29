package org.zwobble.json5.parser;

import java.nio.CharBuffer;
import java.util.List;

class TokenIterator {
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
        return this.tokens.get(this.tokenIndex);
    }
}
