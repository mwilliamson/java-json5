package org.zwobble.json5.parser;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

class Json5Tokenizer {
    private static final CharBuffer NULL = CharBuffer.wrap("null");

    private Json5Tokenizer() {
    }

    static List<Json5Token> tokenize(String text) {
        var iterator = new CharIterator(text);
        var tokens = new ArrayList<Json5Token>();

        while (!iterator.isEnd()) {
            if (iterator.trySkip('{')) {
                var token = new Json5Token(Json5TokenType.BRACE_OPEN);
                tokens.add(token);
            } else if (iterator.trySkip('}')) {
                var token = new Json5Token(Json5TokenType.BRACE_CLOSE);
                tokens.add(token);
            } else if (iterator.trySkip(NULL)) {
                var token = new Json5Token(Json5TokenType.NULL);
                tokens.add(token);
            }
        }

        return tokens;
    }

    private static class CharIterator {
        private final CharBuffer buffer;
        private int index;

        private CharIterator(String text) {
            this.buffer = CharBuffer.wrap(text);
            this.index = 0;
        }

        private boolean isEnd() {
            return this.index >= this.buffer.length();
        }

        private boolean trySkip(char skip) {
            if (this.buffer.get(this.index) == skip) {
                this.index += 1;
                return true;
            } else {
                return false;
            }
        }

        private boolean trySkip(CharBuffer skip) {
            if (skip.length() > this.remaining()) {
                return false;
            }

            if (this.buffer.subSequence(this.index, skip.length()).equals(skip)) {
                this.index += skip.length();
                return true;
            } else {
                return false;
            }
        }

        private int remaining() {
            return this.buffer.length() - this.index;
        }
    }
}
