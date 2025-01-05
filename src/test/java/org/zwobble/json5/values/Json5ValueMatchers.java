package org.zwobble.json5.values;

import org.zwobble.sourcetext.SourceRange;
import org.zwobble.precisely.MatchResult;
import org.zwobble.precisely.Matcher;
import org.zwobble.precisely.TextTree;

import java.math.BigDecimal;

import static org.zwobble.precisely.Matchers.*;

public class Json5ValueMatchers {
    public static Matcher<Json5Value> isJson5Null(
        Matcher<SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5Null.class,
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5Boolean(
        boolean value,
        Matcher<SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5Boolean.class,
            has("value", x -> x.value(), equalTo(value)),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5String(
        String value,
        Matcher<SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5String.class,
            has("value", x -> x.value(), equalTo(value)),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5NumberFinite(
        BigDecimal value,
        Matcher<SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5NumberFinite.class,
            has("value", x -> x.value(), new BigDecimalMatcher(value)),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    static class BigDecimalMatcher implements Matcher<BigDecimal> {
        private final BigDecimal value;

        BigDecimalMatcher(BigDecimal value) {
            this.value = value;
        }

        @Override
        public MatchResult match(BigDecimal actual) {
            if (value.compareTo(actual) == 0) {
                return MatchResult.matched();
            } else {
                return MatchResult.unmatched(TextTree.object("was ", actual));
            }
        }

        @Override
        public TextTree describe() {
            return TextTree.object(value);
        }
    }

    public static Matcher<Json5Value> isJson5NumberPositiveInfinity(
        Matcher<SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5NumberPositiveInfinity.class,
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5NumberNegativeInfinity(
        Matcher<SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5NumberNegativeInfinity.class,
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5NumberNan(
        Matcher<SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5NumberNan.class,
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5Object(
        Matcher<Iterable<? extends Json5Member>> members,
        Matcher<SourceRange> sourceRange
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
        Matcher<SourceRange> sourceRange
    ) {
        return allOf(
            has("name", x -> x.name(), name),
            has("value", x -> x.value(), value),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5MemberName> isJson5MemberName(
        String value,
        Matcher<SourceRange> sourceRange
    ) {
        return allOf(
            has("value", x -> x.value(), equalTo(value)),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }

    public static Matcher<Json5Value> isJson5Array(
        Matcher<Iterable<? extends Json5Value>> elements,
        Matcher<SourceRange> sourceRange
    ) {
        return instanceOf(
            Json5Array.class,
            has("elements", x -> x.elements(), elements),
            has("sourceRange", x -> x.sourceRange(), sourceRange)
        );
    }
}
