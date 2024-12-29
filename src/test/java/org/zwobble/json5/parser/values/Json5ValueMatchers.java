package org.zwobble.json5.parser.values;

import org.zwobble.json5.values.Json5Member;
import org.zwobble.json5.values.Json5Null;
import org.zwobble.json5.values.Json5Object;
import org.zwobble.json5.values.Json5Value;
import org.zwobble.precisely.Matcher;

import static org.zwobble.precisely.Matchers.has;
import static org.zwobble.precisely.Matchers.instanceOf;

public class Json5ValueMatchers {
    public static Matcher<Json5Value> isJson5Null() {
        return instanceOf(
            Json5Null.class
        );
    }

    public static Matcher<Json5Value> isJson5Object(Matcher<Iterable<? extends Json5Member>> members) {
        return instanceOf(
            Json5Object.class,
            has("members", x -> x.members(), members)
        );
    }
}
