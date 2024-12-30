package org.zwobble.json5.parser.values;

import org.zwobble.json5.sources.Json5SourceRange;
import org.zwobble.json5.values.*;
import org.zwobble.precisely.Matcher;

import static org.zwobble.precisely.Matchers.*;

public class Json5ValueMatchers {
    public static Matcher<Json5Value> isJson5Null(
        Matcher<Json5SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5Null.class,
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5Boolean(
        boolean value,
        Matcher<Json5SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5Boolean.class,
            has("value", x -> x.value(), equalTo(value)),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5String(
        String value,
        Matcher<Json5SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5String.class,
            has("value", x -> x.value(), equalTo(value)),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5Number(
        String value,
        Matcher<Json5SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5Number.class,
            has("value", x -> x.value(), equalTo(value)),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5Object(
        Matcher<Iterable<? extends Json5Member>> members,
        Matcher<Json5SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5Object.class,
            has("members", x -> x.members(), members),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Member> isJson5Member(
        Matcher<Json5MemberName> name,
        Matcher<Json5Value> value,
        Matcher<Json5SourceRange> sourceRange
    ) {
        return allOf(
            has("name", x -> x.name(), name),
            has("value", x -> x.value(), value),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5MemberName> isJson5MemberName(
        String value,
        Matcher<Json5SourceRange> sourceRange
    ) {
        return allOf(
            has("value", x -> x.value(), equalTo(value)),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5Array(
        Matcher<Iterable<? extends Json5Value>> elements,
        Matcher<Json5SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5Array.class,
            has("elements", x -> x.elements(), elements),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }
}
