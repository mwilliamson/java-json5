package org.zwobble.json5.values;

import org.junit.jupiter.api.Test;
import org.zwobble.json5.paths.Json5Path;
import org.zwobble.sourcetext.SourcePosition;
import org.zwobble.sourcetext.SourceRange;
import org.zwobble.sourcetext.SourceText;

import static org.zwobble.json5.sources.SourceRangeMatchers.isSourceRange;
import static org.zwobble.json5.values.Json5ValueMatchers.isJson5Boolean;
import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.isOptionalEmpty;
import static org.zwobble.precisely.Matchers.isOptionalOf;

public class Json5ObjectTests {
    public static final SourceRange SOURCE_RANGE = new SourceRange(
        SourceText.fromString(""),
        new SourcePosition(0),
        new SourcePosition(0)
    );

    @Test
    public void whenObjectHasMemberThenMemberCanBeRetrievedByName() {
        var object = Json5Object.builder()
            .addMember(new Json5Member(
                new Json5MemberName("foo", SOURCE_RANGE),
                new Json5Boolean(true, Json5Path.ROOT, SOURCE_RANGE),
                SOURCE_RANGE
            ))
            .addMember(new Json5Member(
                new Json5MemberName("bar", SOURCE_RANGE),
                new Json5Boolean(false, Json5Path.ROOT, SOURCE_RANGE),
                SOURCE_RANGE
            ))
            .build(Json5Path.ROOT, SOURCE_RANGE);

        var result = object.getValue("foo");

        assertThat(result, isOptionalOf(
            isJson5Boolean(true, isSourceRange(0, 0))
        ));
    }

    @Test
    public void whenObjectHasNoMemberWithNameThenGetValueReturnsEmptyOptional() {
        var object = Json5Object.builder()
            .addMember(new Json5Member(
                new Json5MemberName("foo", SOURCE_RANGE),
                new Json5Boolean(true, Json5Path.ROOT, SOURCE_RANGE),
                SOURCE_RANGE
            ))
            .addMember(new Json5Member(
                new Json5MemberName("bar", SOURCE_RANGE),
                new Json5Boolean(false, Json5Path.ROOT, SOURCE_RANGE),
                SOURCE_RANGE
            ))
            .build(Json5Path.ROOT, SOURCE_RANGE);

        var result = object.getValue("baz");

        assertThat(result, isOptionalEmpty());
    }
}
