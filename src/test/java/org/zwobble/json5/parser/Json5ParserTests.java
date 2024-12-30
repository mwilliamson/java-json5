package org.zwobble.json5.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zwobble.json5.sources.Json5SourceRangeMatchers.isJson5SourceRange;
import static org.zwobble.json5.parser.values.Json5ValueMatchers.*;
import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.equalTo;
import static org.zwobble.precisely.Matchers.isSequence;

public class Json5ParserTests {
    // == Nulls ==

    @Test
    public void canParseNullLiteral() {
        var result = Json5Parser.parseText("null");

        assertThat(result, isJson5Null(isJson5SourceRange(0, 4)));
    }

    // == Booleans ==

    @Test
    public void canParseTrue() {
        var result = Json5Parser.parseText("true");

        assertThat(result, isJson5Boolean(true, isJson5SourceRange(0, 4)));
    }

    @Test
    public void canParseFalse() {
        var result = Json5Parser.parseText("false");

        assertThat(result, isJson5Boolean(false, isJson5SourceRange(0, 5)));
    }

    // == Strings ==

    @Test
    public void canParseEmptyStringUsingDoubleQuotes() {
        var result = Json5Parser.parseText("\"\"");

        assertThat(result, isJson5String("", isJson5SourceRange(0, 2)));
    }

    @Test
    public void canParseEmptyStringUsingSingleQuotes() {
        var result = Json5Parser.parseText("''");

        assertThat(result, isJson5String("", isJson5SourceRange(0, 2)));
    }

    // == Numbers ==

    @Test
    public void canParseIntegerZero() {
        var result = Json5Parser.parseText("0");

        assertThat(result, isJson5Number("0", isJson5SourceRange(0, 1)));
    }

    // == Objects ==

    @Test
    public void emptyObject() {
        var result = Json5Parser.parseText("{}");

        assertThat(result, isJson5Object(isSequence(), isJson5SourceRange(0, 2)));
    }

    @Test
    public void canParseObjectWithOneMemberAndNoTrailingComma() {
        var result = Json5Parser.parseText("{foo: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isJson5SourceRange(1, 4)),
                    isJson5Boolean(true, isJson5SourceRange(6, 10)),
                    isJson5SourceRange(1, 10)
                )
            ),
            isJson5SourceRange(0, 11)
        ));
    }

    @Test
    public void canParseObjectWithOneMemberAndTrailingComma() {
        var result = Json5Parser.parseText("{foo: true,}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isJson5SourceRange(1, 4)),
                    isJson5Boolean(true, isJson5SourceRange(6, 10)),
                    isJson5SourceRange(1, 10)
                )
            ),
            isJson5SourceRange(0, 12)
        ));
    }

    @Test
    public void canParseObjectWithMultipleMembersAndNoTrailingComma() {
        var result = Json5Parser.parseText("{foo:true,bar:false,baz:null}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isJson5SourceRange(1, 4)),
                    isJson5Boolean(true, isJson5SourceRange(5, 9)),
                    isJson5SourceRange(1, 9)
                ),
                isJson5Member(
                    isJson5MemberName("bar", isJson5SourceRange(10, 13)),
                    isJson5Boolean(false, isJson5SourceRange(14, 19)),
                    isJson5SourceRange(10, 19)
                ),
                isJson5Member(
                    isJson5MemberName("baz", isJson5SourceRange(20, 23)),
                    isJson5Null(isJson5SourceRange(24, 28)),
                    isJson5SourceRange(20, 28)
                )
            ),
            isJson5SourceRange(0, 29)
        ));
    }

    @Test
    public void canParseObjectWithMultipleMembersAndTrailingComma() {
        var result = Json5Parser.parseText("{foo:true,bar:false,baz:null,}");


        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isJson5SourceRange(1, 4)),
                    isJson5Boolean(true, isJson5SourceRange(5, 9)),
                    isJson5SourceRange(1, 9)
                ),
                isJson5Member(
                    isJson5MemberName("bar", isJson5SourceRange(10, 13)),
                    isJson5Boolean(false, isJson5SourceRange(14, 19)),
                    isJson5SourceRange(10, 19)
                ),
                isJson5Member(
                    isJson5MemberName("baz", isJson5SourceRange(20, 23)),
                    isJson5Null(isJson5SourceRange(24, 28)),
                    isJson5SourceRange(20, 28)
                )
            ),
            isJson5SourceRange(0, 30)
        ));
    }

    @Test
    public void whenObjectHasTokenThatIsNeitherMemberNameNorClosingBraceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("{]}")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON member or '}', but was ']'")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(1, 2));
    }

    @Test
    public void whenObjectMemberIsMissingColonThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("{foo,}")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected ':', but was ','")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(4, 5));
    }

    @Test
    public void whenObjectHasMemberValueThatIsNeitherValueNorClosingBraceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("{foo:]}")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value, but was ']'")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(5, 6));
    }

    @Test
    public void whenObjectHasPostMemberTokenThatIsNeitherCommaNorClosingBraceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("{foo:null]}")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected ',' or '}', but was ']'")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(9, 10));
    }

    @Test
    public void whenObjectIsMissingClosingBraceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("{")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON member or '}', but was end of document")
        );
    }

    // == Arrays ==

    @Test
    public void emptyArray() {
        var result = Json5Parser.parseText("[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(0, 2)));
    }

    @Test
    public void canParseArrayWithOneElementAndNoTrailingComma() {
        var result = Json5Parser.parseText("[true]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5Boolean(true, isJson5SourceRange(1, 5))
            ),
            isJson5SourceRange(0, 6)
        ));
    }

    @Test
    public void canParseArrayWithOneElementAndTrailingComma() {
        var result = Json5Parser.parseText("[true,]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5Boolean(true, isJson5SourceRange(1, 5))
            ),
            isJson5SourceRange(0, 7)
        ));
    }

    @Test
    public void canParseArrayWithMultipleElementsAndNoTrailingComma() {
        var result = Json5Parser.parseText("[true, false, null]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5Boolean(true, isJson5SourceRange(1, 5)),
                isJson5Boolean(false, isJson5SourceRange(7, 12)),
                isJson5Null(isJson5SourceRange(14, 18))
            ),
            isJson5SourceRange(0, 19)
        ));
    }

    @Test
    public void canParseArrayWithMultipleElementsAndTrailingComma() {
        var result = Json5Parser.parseText("[true, false, null,]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5Boolean(true, isJson5SourceRange(1, 5)),
                isJson5Boolean(false, isJson5SourceRange(7, 12)),
                isJson5Null(isJson5SourceRange(14, 18))
            ),
            isJson5SourceRange(0, 20)
        ));
    }

    @Test
    public void whenArrayHasElementThatIsNeitherValueNorClosingSquareBracketThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("[}]")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value or ']', but was '}'")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(1, 2));
    }

    @Test
    public void whenArrayHasPostElementTokenThatIsNeitherCommaNorClosingSquareBracketThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("[null}]")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected ',' or ']', but was '}'")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(5, 6));
    }

    @Test
    public void whenArrayIsMissingClosingSquareBracketThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("[")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value or ']', but was end of document")
        );
    }

    // == Documents ==

    @Test
    public void whenDocumentIsEmptyThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value, but was end of document")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(0, 0));
    }

    @Test
    public void whenDocumentHasTokensAfterValueThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("[][")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected end of document, but was '['")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(2, 3));
    }

    @Test
    public void whitespaceIsIgnored() {
        var result = Json5Parser.parseText("   [ ]  ");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }

    @Test
    public void tabIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\t[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void verticalTabIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\u000b[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void formFeedIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\f[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void noBreakSpaceIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\u00a0[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void spaceIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText(" []");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void byteOrderMarkIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\ufeff[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void anyOtherUnicodeSpaceSeparatorIsTreatedAsWhiteSpace() {
        var result = Json5Parser.parseText("\u2000[]");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(1, 3)));
    }

    @Test
    public void lineTerminatorLineFeedIsIgnored() {
        var result = Json5Parser.parseText("\n\n\n[\n]\n\n");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }

    @Test
    public void lineTerminatorCarriageReturnIsIgnored() {
        var result = Json5Parser.parseText("\r\r\r[\r]\r\r");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }

    @Test
    public void lineTerminatorLineSeparatorIsIgnored() {
        var result = Json5Parser.parseText("\u2028\u2028\u2028[\u2028]\u2028\u2028");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }

    @Test
    public void lineTerminatorParagraphSeparatorIsIgnored() {
        var result = Json5Parser.parseText("\u2029\u2029\u2029[\u2029]\u2029\u2029");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(3, 6)));
    }

    @Test
    public void multiLineCommentIsIgnored() {
        var result = Json5Parser.parseText("/* a */[/* b\n\nc */] /** d*  **/");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(7, 19)));
    }

    @Test
    public void whenMultiLineCommentIsNotClosedThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> Json5Parser.parseText("[]/*")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected '*/', but was end of document")
        );
        assertThat(error.sourceRange(), isJson5SourceRange(4, 4));
    }

    @Test
    public void singleLineCommentIsIgnored() {
        var result = Json5Parser.parseText("// a\n[// b\n] // c\n\n");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(5, 12)));
    }

    @Test
    public void singleLineCommentCanBeClosedByEndOfDocument() {
        var result = Json5Parser.parseText("[]//");

        assertThat(result, isJson5Array(isSequence(), isJson5SourceRange(0, 2)));
    }
}
