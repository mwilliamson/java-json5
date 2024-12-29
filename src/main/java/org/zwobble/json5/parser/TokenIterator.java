package org.zwobble.json5.parser;

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

    Json5Token peek() {
        return this.tokens.get(this.tokenIndex);
    }
}
