package org.zwobble.json5.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zwobble.json5.sources.Json5SourceRangeMatchers.isJson5SourceRange;
import static org.zwobble.json5.parser.values.Json5ValueMatchers.*;
import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.equalTo;
import static org.zwobble.precisely.Matchers.isSequence;

public class Json5ParserTests {
    // == Nulls ==

    @Test
    public void canParseNullLiteral() {
        var result = Json5Parser.parseText("null");

        assertThat(result, isJson5Null(isJson5SourceRange(0, 4)));
    }

    // == Booleans ==

    @Test
    public void canParseTrue() {
        var result = Json5Parser.parseText("true");

        assertThat(result, isJson5Boolean(true, isJson5SourceRange(0, 4)));
    }

    @Test
    public void canParseFalse() {
        var result = Json5Parser.parseText("false");

        assertThat(result, isJson5Boolean(false, isJson5SourceRange(0, 5)));
    }

    // == Strings ==

    @Test
    public void canParseEmptyStringUsingDoubleQuotes() {
        var result = Json5Parser.parseText("\"\"");

        assertThat(result, isJson5String("", isJson5SourceRange(0, 2)));
    }

    @Test
    public void canParseEmptyStringUsingSingleQuotes() {
        var result = Json5Parser.parseText("''");

        assertThat(result, isJson5String("", isJson5SourceRange(0, 2)));
    }

    // == Numbers ==

    @Test
    public void canParseIntegerZero() {
        var result = Json5Parser.parseText("0");

        assertThat(result, isJson5Number("0", isJson5SourceRange(0, 1)));
    }

    // == Objects ==

    @Test
    public void emptyObject() {
        var result = Json5Parser.parseText("{}");

        assertThat(result, isJson5Object(isSequence(), isJson5SourceRange(0, 2)));
    }

    @Test
    public void whenObjectIsMissingClosingBraceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("{")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value or '}', but was end of document")
        );
    }

    // == Arrays ==

    @Test
    public void emptyArray() {
        var result = Json5Parser.parseText("[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(0, 2)));
    }

    @Test
    public void whenArrayIsMissingClosingSquareBracketThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("[")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value or ']', but was end of document")
        );
    }

    // == Documents ==

    @Test
    public void whenDocumentHasTokensAfterValueThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("[][")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected end of document, but was '['")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(2, 3));
    }
}
