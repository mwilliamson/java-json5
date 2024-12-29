package org.zwobble.json5.parser;

import org.junit.jupiter.api.Test;

import static org.zwobble.json5.parser.values.Json5ValueMatchers.*;
import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.isSequence;

public class Json5ParserTests {
    // == Nulls ==

    @Test
    public void canParseNullLiteral() {
        var result = Json5Parser.parseText("null");

        assertThat(result, isJson5Null());
    }

    // == Booleans ==

    @Test
    public void canParseTrue() {
        var result = Json5Parser.parseText("true");

        assertThat(result, isJson5Boolean(true));
    }

    @Test
    public void canParseFalse() {
        var result = Json5Parser.parseText("false");

        assertThat(result, isJson5Boolean(false));
    }

    // == Strings ==

    @Test
    public void canParseEmptyStringUsingDoubleQuotes() {
        var result = Json5Parser.parseText("\"\"");

        assertThat(result, isJson5String(""));
    }

    // == Objects ==

    @Test
    public void emptyObject() {
        var result = Json5Parser.parseText("{}");

        assertThat(result, isJson5Object(isSequence()));
    }
}
