package org.zwobble.json5.values;

import org.junit.jupiter.api.Test;
import org.zwobble.json5.sources.Json5SourceRange;

import static org.zwobble.json5.sources.Json5SourceRangeMatchers.isJson5SourceRange;
import static org.zwobble.json5.values.Json5ValueMatchers.isJson5Boolean;
import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.isOptionalEmpty;
import static org.zwobble.precisely.Matchers.isOptionalOf;

public class Json5ObjectTests {
    @Test
    public void whenObjectHasMemberThenMemberCanBeRetrievedByName() {
        var object = Json5Object.builder()
            .addMember(new Json5Member(
                new Json5MemberName("foo", new Json5SourceRange(0, 0)),
                new Json5Boolean(true, new Json5SourceRange(0, 0)),
                new Json5SourceRange(0, 0)
            ))
            .addMember(new Json5Member(
                new Json5MemberName("bar", new Json5SourceRange(0, 0)),
                new Json5Boolean(false, new Json5SourceRange(0, 0)),
                new Json5SourceRange(0, 0)
            ))
            .build(new Json5SourceRange(0, 0));

        var result = object.getValue("foo");

        assertThat(result, isOptionalOf(
            isJson5Boolean(true, isJson5SourceRange(0, 0))
        ));
    }

    @Test
    public void whenObjectHasNoMemberWithNameThenGetValueReturnsEmptyOptional() {
        var object = Json5Object.builder()
            .addMember(new Json5Member(
                new Json5MemberName("foo", new Json5SourceRange(0, 0)),
                new Json5Boolean(true, new Json5SourceRange(0, 0)),
                new Json5SourceRange(0, 0)
            ))
            .addMember(new Json5Member(
                new Json5MemberName("bar", new Json5SourceRange(0, 0)),
                new Json5Boolean(false, new Json5SourceRange(0, 0)),
                new Json5SourceRange(0, 0)
            ))
            .build(new Json5SourceRange(0, 0));

        var result = object.getValue("baz");

        assertThat(result, isOptionalEmpty());
    }
}