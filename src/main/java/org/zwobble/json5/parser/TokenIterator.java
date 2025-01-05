package org.zwobble.json5.parser;

import java.util.List;

class TokenIterator {
    private final List<Json5Token> tokens;
    private int tokenIndex;
    private final Json5Token tokenEnd;

    TokenIterator(List<Json5Token> tokens, Json5Token tokenEnd) {
        this.tokens = tokens;
        this.tokenIndex = 0;
        this.tokenEnd = tokenEnd;
    }

    void skip() {
        this.tokenIndex += 1;
    }

    boolean trySkip(Json5TokenType tokenType) {
        if (isNext(tokenType)) {
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

    boolean isNext(Json5TokenType tokenType) {
        return peek().tokenType() == tokenType;
    }
}
