package org.zwobble.json5.paths;

import org.junit.jupiter.api.Test;

import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.equalTo;

public class Json5PathTests {
    @Test
    public void rootPathIsRepresentedByDollar() {
        var path = Json5Path.ROOT;

        var result = path.toString();

        assertThat(result, equalTo("$"));
    }

    @Test
    public void pathToObjectMembersIsRepresentedByDotAndMemberName() {
        var path = Json5Path.ROOT.member("foo");

        var result = path.toString();

        assertThat(result, equalTo("$.foo"));
    }

    @Test
    public void pathToArrayElementIsRepresentedByIndexNotation() {
        var path = Json5Path.ROOT.index(2);

        var result = path.toString();

        assertThat(result, equalTo("$[2]"));
    }
}
