package org.zwobble.json5.parser;

import org.junit.jupiter.api.Test;
import org.zwobble.sourcetext.SourceRange;
import org.zwobble.sourcetext.SourceText;

import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.equalTo;

public class Json5TokenTests {
    public static SourceRange sourceRange(String text) {
        var sourceText = SourceText.fromString("<string>", text);
        var start = sourceText.characterPosition(0);
        var end = sourceText.characterPosition(text.length());
        return start.to(end);
    }

    @Test
    public void identifierTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.IDENTIFIER,
            sourceRange("foo")
        );

        var result = token.describe();

        // TODO: handle identifiers that require escaping
        assertThat(result, equalTo("identifier 'foo'"));
    }

    @Test
    public void braceOpenTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_BRACE_OPEN,
            sourceRange("{")
        );

        var result = token.describe();

        assertThat(result, equalTo("'{'"));
    }

    @Test
    public void braceCloseTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_BRACE_CLOSE,
            sourceRange("}")
        );

        var result = token.describe();

        assertThat(result, equalTo("'}'"));
    }

    @Test
    public void squareOpenTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_SQUARE_OPEN,
            sourceRange("[")
        );

        var result = token.describe();

        assertThat(result, equalTo("'['"));
    }

    @Test
    public void squareCloseTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_SQUARE_CLOSE,
            sourceRange("]")
        );

        var result = token.describe();

        assertThat(result, equalTo("']'"));
    }

    @Test
    public void colonTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_COLON,
            sourceRange(":")
        );

        var result = token.describe();

        assertThat(result, equalTo("':'"));
    }

    @Test
    public void commaTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.PUNCTUATOR_COMMA,
            sourceRange(",")
        );

        var result = token.describe();

        assertThat(result, equalTo("','"));
    }

    @Test
    public void stringTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.STRING,
            sourceRange("\"foo\"")
        );

        var result = token.describe();

        assertThat(result, equalTo("string \"foo\""));
    }

    @Test
    public void numberDecimalTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_DECIMAL,
            sourceRange("42")
        );

        var result = token.describe();

        assertThat(result, equalTo("number '42'"));
    }

    @Test
    public void numberHexTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_HEX,
            sourceRange("0x42")
        );

        var result = token.describe();

        assertThat(result, equalTo("number '0x42'"));
    }

    @Test
    public void numberPositiveInfinityTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_POSITIVE_INFINITY,
            sourceRange("Infinity")
        );

        var result = token.describe();

        assertThat(result, equalTo("number 'Infinity'"));
    }

    @Test
    public void numberNegativeInfinityTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_NEGATIVE_INFINITY,
            sourceRange("-Infinity")
        );

        var result = token.describe();

        assertThat(result, equalTo("number '-Infinity'"));
    }

    @Test
    public void numberNaNTokenIsDescribedLiterally() {
        var token = new Json5Token(
            Json5TokenType.NUMBER_NAN,
            sourceRange("NaN")
        );

        var result = token.describe();

        assertThat(result, equalTo("number 'NaN'"));
    }

    @Test
    public void endTokenIsDescribedAsEndOfDocument() {
        var token = new Json5Token(
            Json5TokenType.END,
            sourceRange("")
        );

        var result = token.describe();

        assertThat(result, equalTo("end of document"));
    }
}
