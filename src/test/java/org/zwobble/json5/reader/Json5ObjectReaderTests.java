package org.zwobble.json5.reader;

import org.junit.jupiter.api.Test;
import org.zwobble.sourcetext.SourceText;

import java.util.Optional;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.equalTo;

public class Json5ObjectReaderTests {
    @Test
    public void whenDocumentIsNotObjectThenParseThrowsError() {
        var source = "null";

        var error = assertThrows(
            Json5ObjectReadError.class,
            () -> parseJson5Object(source)
        );

        assertThat(error.getMessage(), equalTo("$ expected to be object, but was null"));
        assertThat(error.sourceRange().describe(), equalTo("""
            <string>:1:1
            null
            ^^^^"""
        ));
    }

    @Test
    public void whenMemberIsMissingThenGetLongThrowsError() {
        var source = """
            {
                a: {
                    b: [
                        {x: 1},
                        {},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var error = assertThrows(
            Json5ObjectReadError.class,
            () -> object
                .getObjectOrNone("a")
                .orElseThrow()
                .getArrayOfObjectsOrNone("b")
                .orElseThrow()
                .get(1)
                .getLong("x")
        );

        assertThat(error.getMessage(), equalTo("$.a.b[1] missing member x"));
        assertThat(error.sourceRange().describe(), equalTo("""
            <string>:5:13
                        {},
                        ^^"""
        ));
    }

    @Test
    public void whenMemberIsNotNumberFiniteThenGetLongThrowsError() {
        var source = """
            {
                a: {
                    b: [
                        {x: 1},
                        {x: true},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var error = assertThrows(
            Json5ObjectReadError.class,
            () -> object
                .getObjectOrNone("a")
                .orElseThrow()
                .getArrayOfObjectsOrNone("b")
                .orElseThrow()
                .get(1)
                .getLong("x")
        );

        assertThat(error.getMessage(), equalTo("$.a.b[1].x expected to be finite number, but was boolean"));
        assertThat(error.sourceRange().describe(), equalTo("""
            <string>:5:17
                        {x: true},
                            ^^^^"""
        ));
    }

    @Test
    public void whenMemberIsFiniteNumberButIsNotLongThenGetLongThrowsError() {
        var source = """
            {
                a: {
                    b: [
                        {x: 1},
                        {x: 1.2},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var error = assertThrows(
            Json5ObjectReadError.class,
            () -> object
                .getObjectOrNone("a")
                .orElseThrow()
                .getArrayOfObjectsOrNone("b")
                .orElseThrow()
                .get(1)
                .getLong("x")
        );

        assertThat(error.getMessage(), equalTo("$.a.b[1].x must be a 64-bit integer"));
        assertThat(error.sourceRange().describe(), equalTo("""
            <string>:5:17
                        {x: 1.2},
                            ^^^"""
        ));
    }

    @Test
    public void whenMemberIsLongThenGetLongReturnsValue() {
        var source = """
            {
                a: {
                    b: [
                        {x: 1},
                        {x : 2},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var result = object
            .getObjectOrNone("a")
            .orElseThrow()
            .getArrayOfObjectsOrNone("b")
            .orElseThrow()
            .get(1)
            .getLong("x");

        assertThat(result, equalTo(2L));
    }

    @Test
    public void whenMemberIsMissingThenGetLongOrNoneReturnsEmptyOptional() {
        var source = """
            {
                a: {
                    b: [
                        {x: 1},
                        {},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var result = object
            .getObjectOrNone("a")
            .orElseThrow()
            .getArrayOfObjectsOrNone("b")
            .orElseThrow()
            .get(1)
            .getLongOrNone("x");

        assertThat(result, equalTo(OptionalLong.empty()));
    }

    @Test
    public void whenMemberIsNotLongThenGetLongOrNoneThrowsError() {
        var source = """
            {
                a: {
                    b: [
                        {x: 1},
                        {x: true},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var error = assertThrows(
            Json5ObjectReadError.class,
            () -> object
                .getObjectOrNone("a")
                .orElseThrow()
                .getArrayOfObjectsOrNone("b")
                .orElseThrow()
                .get(1)
                .getLongOrNone("x")
        );

        assertThat(error.getMessage(), equalTo("$.a.b[1].x expected to be finite number, but was boolean"));
        assertThat(error.sourceRange().describe(), equalTo("""
            <string>:5:17
                        {x: true},
                            ^^^^"""
        ));
    }

    @Test
    public void whenMemberIsLongOrNoneThenGetLongReturnsValue() {
        var source = """
            {
                a: {
                    b: [
                        {x: 1},
                        {x: 2},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var result = object
            .getObjectOrNone("a")
            .orElseThrow()
            .getArrayOfObjectsOrNone("b")
            .orElseThrow()
            .get(1)
            .getLongOrNone("x");

        assertThat(result, equalTo(OptionalLong.of(2L)));
    }

    @Test
    public void whenMemberIsMissingThenGetStringThrowsError() {
        var source = """
            {
                shed: {
                    bins: [
                        {name: "one"},
                        {},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var error = assertThrows(
            Json5ObjectReadError.class,
            () -> object
                .getObjectOrNone("shed")
                .orElseThrow()
                .getArrayOfObjectsOrNone("bins")
                .orElseThrow()
                .get(1)
                .getString("name")
        );

        assertThat(error.getMessage(), equalTo("$.shed.bins[1] missing member name"));
        assertThat(error.sourceRange().describe(), equalTo("""
            <string>:5:13
                        {},
                        ^^"""
        ));
    }

    @Test
    public void whenMemberIsNotStringThenGetStringThrowsError() {
        var source = """
            {
                shed: {
                    bins: [
                        {name: "one"},
                        {name: true},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var error = assertThrows(
            Json5ObjectReadError.class,
            () -> object
                .getObjectOrNone("shed")
                .orElseThrow()
                .getArrayOfObjectsOrNone("bins")
                .orElseThrow()
                .get(1)
                .getString("name")
        );

        assertThat(error.getMessage(), equalTo("$.shed.bins[1].name expected to be string, but was boolean"));
        assertThat(error.sourceRange().describe(), equalTo("""
            <string>:5:20
                        {name: true},
                               ^^^^"""
        ));
    }

    @Test
    public void whenMemberIsStringThenGetStringReturnsValue() {
        var source = """
            {
                shed: {
                    bins: [
                        {name: "one"},
                        {name: "two"},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var result = object
            .getObjectOrNone("shed")
            .orElseThrow()
            .getArrayOfObjectsOrNone("bins")
            .orElseThrow()
            .get(1)
            .getString("name");

        assertThat(result.value(), equalTo("two"));
        assertThat(result.sourceRange().describe(), equalTo(
            """
            <string>:5:20
                        {name: "two"},
                               ^^^^^"""
        ));
    }

    @Test
    public void whenMemberIsMissingThenGetStringOrNoneReturnsEmptyOptional() {
        var source = """
            {
                shed: {
                    bins: [
                        {name: "one"},
                        {},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var result = object
            .getObjectOrNone("shed")
            .orElseThrow()
            .getArrayOfObjectsOrNone("bins")
            .orElseThrow()
            .get(1)
            .getStringOrNone("name");

        assertThat(result, equalTo(Optional.empty()));
    }

    @Test
    public void whenMemberIsNotStringThenGetStringOrNoneThrowsError() {
        var source = """
            {
                shed: {
                    bins: [
                        {name: "one"},
                        {name: true},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var error = assertThrows(
            Json5ObjectReadError.class,
            () -> object
                .getObjectOrNone("shed")
                .orElseThrow()
                .getArrayOfObjectsOrNone("bins")
                .orElseThrow()
                .get(1)
                .getStringOrNone("name")
        );

        assertThat(error.getMessage(), equalTo("$.shed.bins[1].name expected to be string, but was boolean"));
        assertThat(error.sourceRange().describe(), equalTo("""
            <string>:5:20
                        {name: true},
                               ^^^^"""
        ));
    }

    @Test
    public void whenMemberIsStringThenGetStringOrNoneReturnsValue() {
        var source = """
            {
                shed: {
                    bins: [
                        {name: "one"},
                        {name: "two"},
                    ],
                },
            }
            """;
        var object = parseJson5Object(source);

        var result = object
            .getObjectOrNone("shed")
            .orElseThrow()
            .getArrayOfObjectsOrNone("bins")
            .orElseThrow()
            .get(1)
            .getStringOrNone("name");

        assertThat(result.orElseThrow().value(), equalTo("two"));
        assertThat(result.orElseThrow().sourceRange().describe(), equalTo(
            """
            <string>:5:20
                        {name: "two"},
                               ^^^^^"""
        ));
    }

    private Json5ObjectReader parseJson5Object(String text) {
        var sourceText = SourceText.fromString("<string>", text);
        return Json5ObjectReader.parse(sourceText);
    }
}
