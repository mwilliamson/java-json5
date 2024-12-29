package org.zwobble.json5.parser;

import java.nio.CharBuffer;

record Json5Token(Json5TokenType tokenType, CharBuffer buffer) {
}
