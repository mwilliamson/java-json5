package org.zwobble.json5.parser;

import org.zwobble.sourcetext.SourcePosition;
import org.zwobble.sourcetext.SourceRange;
import org.zwobble.sourcetext.SourceText;

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
            ? new SourceRange(SourceText.fromString(""), new SourcePosition(0), new SourcePosition(0))
            : tokens.getLast().sourceRange();
        this.tokenEnd = new Json5Token(
            Json5TokenType.END,
            new SourceRange(
                lastTokenSourceRange.sourceText(),
                lastTokenSourceRange.end(),
                lastTokenSourceRange.end()
            )
        );
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

    boolean isNext(Json5TokenType tokenType) {
        return peek().tokenType() == tokenType;
    }
}
