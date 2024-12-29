package org.zwobble.json5.parser;

import org.junit.jupiter.api.Test;
import org.zwobble.json5.values.Json5Member;
import org.zwobble.json5.values.Json5Object;
import org.zwobble.json5.values.Json5Value;
import org.zwobble.precisely.Matcher;

import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.*;

public class Json5ParserTests {
    @Test
    public void emptyObject() {
        var result = Json5Parser.parseText("{}");

        assertThat(result, isJson5Object(isSequence()));
    }

    private Matcher<Json5Value> isJson5Object(Matcher<Iterable<? extends Json5Member>> members) {
        return instanceOf(
            Json5Object.class,
            has("members", x -> x.members(), members)
        );
    }
}
