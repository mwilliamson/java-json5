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
}
