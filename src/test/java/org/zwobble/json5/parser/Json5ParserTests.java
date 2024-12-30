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
    public void whenDocumentIsEmptyThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value, but was end of document")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(0, 0));
    }

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

    @Test
    public void whitespaceIsIgnored() {
        var result = Json5Parser.parseText("   [ ]  ");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }

    @Test
    public void tabIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\t[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void verticalTabIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\u000b[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void formFeedIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\f[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void noBreakSpaceIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\u00a0[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void spaceIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText(" []");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void byteOrderMarkIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\ufeff[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void anyOtherUnicodeSpaceSeparatorIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\u2000[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void lineTerminatorLineFeedIsIgnored() {
        var result = Json5Parser.parseText("\n\n\n[\n]\n\n");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }

    @Test
    public void lineTerminatorCarriageReturnIsIgnored() {
        var result = Json5Parser.parseText("\r\r\r[\r]\r\r");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }

    @Test
    public void lineTerminatorLineSeparatorIsIgnored() {
        var result = Json5Parser.parseText("\u2028\u2028\u2028[\u2028]\u2028\u2028");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }

    @Test
    public void lineTerminatorParagraphSeparatorIsIgnored() {
        var result = Json5Parser.parseText("\u2029\u2029\u2029[\u2029]\u2029\u2029");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }
}
