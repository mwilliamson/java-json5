package org.zwobble.json5.sources;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.equalTo;

public class Json5SourceRangeTests {
    private record TestCase(
        String name,
        String contents,
        int characterIndexStart,
        String expectedContext
    ) {
        public int characterIndexEnd() {
            return characterIndexStart;
        }
    }

    @TestFactory
    public List<DynamicTest> sourceContextIsSourceLineWithPointer() {
        return Stream.of(
            new TestCase(
                "one line, first character",
                "abcd",
                0,
                """
                    <filename>:1:1
                    abcd
                    ^"""
            ),
            new TestCase(
                "one line, last character",
                "abcd",
                3,
                """
                    <filename>:1:4
                    abcd
                       ^"""
            ),
            new TestCase(
                "one line, end",
                "abcd",
                4,
                """
                    <filename>:1:5
                    abcd
                        ^"""
            ),
            new TestCase(
                "many lines, end",
                """
                    abc
                    def""",
                7,
                """
                    <filename>:2:4
                    def
                       ^"""
            ),
            new TestCase(
                "first line, first character",
                """
                    abc
                    def""",
                0,
                """
                    <filename>:1:1
                    abc
                    ^"""
            ),
            new TestCase(
                "first line, last character",
                """
                    abc
                    def""",
                2,
                """
                    <filename>:1:3
                    abc
                      ^"""
            ),
            new TestCase(
                "last line, first character",
                """
                    abc
                    def""",
                4,
                """
                    <filename>:2:1
                    def
                    ^"""
            ),
            new TestCase(
                "new line",
                """
                    abc
                    def""",
                3,
                """
                    <filename>:1:4
                    abc
                       ^"""
            )
        ).map(testCase -> DynamicTest.dynamicTest(testCase.name, () -> {
            var sourceRange = new Json5SourceRange(
                Json5SourceText.fromString(testCase.contents),
                new Json5SourcePosition(testCase.characterIndexStart),
                new Json5SourcePosition(testCase.characterIndexEnd())
            );

            var result = sourceRange.describe("<filename>");

            assertThat(result, equalTo(testCase.expectedContext));
        })).collect(Collectors.toList());
    }

    @Test
    public void whenSourceIsZeroLengthThenPointerPointsToNextCharacter() {
        var sourceRange = new Json5SourceRange(
            Json5SourceText.fromString("""
                abcdef
                ghijkl
                mnopqr"""),
            new Json5SourcePosition(9),
            new Json5SourcePosition(9)
        );

        var result = sourceRange.describe("<filename>");

        assertThat(result, equalTo("""
            <filename>:2:3
            ghijkl
              ^"""));
    }

    @Test
    public void whenSourceIsOneCharacterThenPointerPointsToCharacter() {
        var sourceRange = new Json5SourceRange(
            Json5SourceText.fromString("""
                abcdef
                ghijkl
                mnopqr"""),
            new Json5SourcePosition(9),
            new Json5SourcePosition(9)
        );

        var result = sourceRange.describe("<filename>");

        assertThat(result, equalTo("""
            <filename>:2:3
            ghijkl
              ^"""));
    }

    @Test
    public void whenSourceIsMultipleCharactersOnSameLineThenPointerPointsToAllCharacters() {
        var sourceRange = new Json5SourceRange(
            Json5SourceText.fromString("""
                abcdef
                ghijkl
                mnopqr"""),
            new Json5SourcePosition(9),
            new Json5SourcePosition(12)
        );

        var result = sourceRange.describe("<filename>");

        assertThat(result, equalTo("""
            <filename>:2:3
            ghijkl
              ^^^"""));
    }

    @Test
    public void whenSourceIsMultipleCharactersOnMultipleLinesThenPointerPointsToFirstCharacter() {
        // TODO: show multiple lines
        var sourceRange = new Json5SourceRange(
            Json5SourceText.fromString("""
                abcdef
                ghijkl
                mnopqr"""),
            new Json5SourcePosition(9),
            new Json5SourcePosition(14)
        );

        assertThat(sourceRange.describe("<filename>"), equalTo("""
            <filename>:2:3
            ghijkl
              ^"""));
    }
}
