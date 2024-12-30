package org.zwobble.json5.parser;

enum Json5TokenType {
    // JSON5Token ::
    //     JSON5Identifier
    //     JSON5Punctuator
    //     JSON5String
    //     JSON5Number

    IDENTIFIER,
    PUNCTUATOR_BRACE_OPEN,
    PUNCTUATOR_BRACE_CLOSE,
    PUNCTUATOR_SQUARE_OPEN,
    PUNCTUATOR_SQUARE_CLOSE,
    PUNCTUATOR_COMMA,
    STRING,
    NUMBER,

    // Add a token type for the end of the document to simplify the parser.
    END,
}
