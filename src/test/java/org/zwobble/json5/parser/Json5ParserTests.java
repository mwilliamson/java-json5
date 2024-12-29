package org.zwobble.json5.parser;

import org.junit.jupiter.api.Test;

import static org.zwobble.json5.parser.values.Json5ValueMatchers.isJson5Null;
import static org.zwobble.json5.parser.values.Json5ValueMatchers.isJson5Object;
import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.isSequence;

public class Json5ParserTests {
    @Test
    public void canParseNullLiteral() {
        var result = Json5Parser.parseText("null");

        assertThat(result, isJson5Null());
    }

    @Test
    public void emptyObject() {
        var result = Json5Parser.parseText("{}");

        assertThat(result, isJson5Object(isSequence()));
    }
}
