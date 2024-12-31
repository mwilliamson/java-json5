package org.zwobble.json5.parser;

import org.junit.jupiter.api.Test;
import org.zwobble.json5.sources.Json5SourceRange;

import java.nio.CharBuffer;

import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.equalTo;

public class Json5TokenTests {
    @Test
    public void identifierTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.IDENTIFIER,
            CharBuffer.wrap("foo"),
            new Json5SourceRange(0, 0)
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
            new Json5SourceRange(0, 0)
        );

        var result = token.describe();

        assertThat(result, equalTo("'{'"));
    }

    @Test
    public void braceCloseTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_BRACE_CLOSE,
            CharBuffer.wrap("}"),
            new Json5SourceRange(0, 0)
        );

        var result = token.describe();

        assertThat(result, equalTo("'}'"));
    }

    @Test
    public void squareOpenTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_SQUARE_OPEN,
            CharBuffer.wrap("["),
            new Json5SourceRange(0, 0)
        );

        var result = token.describe();

        assertThat(result, equalTo("'['"));
    }

    @Test
    public void squareCloseTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_SQUARE_CLOSE,
            CharBuffer.wrap("]"),
            new Json5SourceRange(0, 0)
        );

        var result = token.describe();

        assertThat(result, equalTo("']'"));
    }

    @Test
    public void colonTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_COLON,
            CharBuffer.wrap(":"),
            new Json5SourceRange(0, 0)
        );

        var result = token.describe();

        assertThat(result, equalTo("':'"));
    }

    @Test
    public void commaTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_COMMA,
            CharBuffer.wrap(","),
            new Json5SourceRange(0, 0)
        );

        var result = token.describe();

        assertThat(result, equalTo("','"));
    }

    @Test
    public void stringTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.STRING,
            CharBuffer.wrap("\"foo\""),
            new Json5SourceRange(0, 0)
        );

        var result = token.describe();

        assertThat(result, equalTo("string \"foo\""));
    }

    @Test
    public void numberDecimalTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_DECIMAL,
            CharBuffer.wrap("42"),
            new Json5SourceRange(0, 0)
        );

        var result = token.describe();

        assertThat(result, equalTo("number '42'"));
    }

    @Test
    public void endTokenIsDescribedAsEndOfDocument() {
        var token = new Json5Token(
            Json5TokenType.END,
            CharBuffer.wrap(""),
            new Json5SourceRange(0, 0)
        );

        var result = token.describe();

        assertThat(result, equalTo("end of document"));
    }
}
