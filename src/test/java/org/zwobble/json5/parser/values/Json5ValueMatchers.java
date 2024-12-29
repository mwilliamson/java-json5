package org.zwobble.json5.parser.values;

import org.zwobble.json5.values.*;
import org.zwobble.precisely.Matcher;

import static org.zwobble.precisely.Matchers.*;

public class Json5ValueMatchers {
    public static Matcher<Json5Value> isJson5Null() {
        return instanceOf(
            Json5Null.class
        );
    }

    public static Matcher<Json5Value> isJson5Boolean(boolean value) {
        return instanceOf(
            Json5Boolean.class,
            has("value", x -> x.value(), equalTo(value))
        );
    }

    public static Matcher<Json5Value> isJson5String(String value) {
        return instanceOf(
            Json5String.class,
            has("value", x -> x.value(), equalTo(value))
        );
    }

    public static Matcher<Json5Value> isJson5Object(Matcher<Iterable<? extends Json5Member>> members) {
        return instanceOf(
            Json5Object.class,
            has("members", x -> x.members(), members)
        );
    }

    public static Matcher<Json5Value> isJson5Array(Matcher<Iterable<? extends Json5Value>> elements) {
        return instanceOf(
            Json5Array.class,
            has("elements", x -> x.elements(), elements)
        );
    }
}
