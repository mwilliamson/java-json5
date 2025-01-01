package org.zwobble.json5.parser;

import org.junit.jupiter.api.Test;
import org.zwobble.json5.sources.Json5SourceRange;

import java.nio.CharBuffer;

import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.equalTo;

public class Json5TokenTests {
    public static final Json5SourceRange SOURCE_RANGE = new Json5SourceRange(0, 0);

    @Test
    public void identifierTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.IDENTIFIER,
            CharBuffer.wrap("foo"),
            SOURCE_RANGE
        );

        var result = token.describe();

        // TODO: handle identifiers that require escaping
        assertThat(result, equalTo("identifier 'foo'"));
    }

    @Test
    public void braceOpenTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_BRACE_OPEN,
            CharBuffer.wrap("{"),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("'{'"));
    }

    @Test
    public void braceCloseTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_BRACE_CLOSE,
            CharBuffer.wrap("}"),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("'}'"));
    }

    @Test
    public void squareOpenTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_SQUARE_OPEN,
            CharBuffer.wrap("["),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("'['"));
    }

    @Test
    public void squareCloseTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_SQUARE_CLOSE,
            CharBuffer.wrap("]"),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("']'"));
    }

    @Test
    public void colonTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_COLON,
            CharBuffer.wrap(":"),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("':'"));
    }

    @Test
    public void commaTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_COMMA,
            CharBuffer.wrap(","),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("','"));
    }

    @Test
    public void stringTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.STRING,
            CharBuffer.wrap("\"foo\""),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("string \"foo\""));
    }

    @Test
    public void numberDecimalTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_DECIMAL,
            CharBuffer.wrap("42"),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("number '42'"));
    }

    @Test
    public void numberHexTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_HEX,
            CharBuffer.wrap("0x42"),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("number '0x42'"));
    }

    @Test
    public void numberPositiveInfinityTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_POSITIVE_INFINITY,
            CharBuffer.wrap("Infinity"),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("number 'Infinity'"));
    }

    @Test
    public void numberNegativeInfinityTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_NEGATIVE_INFINITY,
            CharBuffer.wrap("-Infinity"),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("number '-Infinity'"));
    }

    @Test
    public void numberNaNTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_NAN,
            CharBuffer.wrap("NaN"),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("number 'NaN'"));
    }

    @Test
    public void endTokenIsDescribedAsEndOfDocument() {
        var token = new Json5Token(
            Json5TokenType.END,
            CharBuffer.wrap(""),
            SOURCE_RANGE
        );

        var result = token.describe();

        assertThat(result, equalTo("end of document"));
    }
}
