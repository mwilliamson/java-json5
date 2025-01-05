package org.zwobble.json5.parser;

import org.junit.jupiter.api.Test;
import org.zwobble.json5.paths.Json5Path;
import org.zwobble.json5.values.Json5Array;
import org.zwobble.json5.values.Json5Boolean;
import org.zwobble.json5.values.Json5Object;
import org.zwobble.json5.values.Json5Value;
import org.zwobble.sourcetext.SourceText;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.zwobble.json5.sources.SourceRangeMatchers.isSourceRange;
import static org.zwobble.json5.values.Json5ValueMatchers.*;
import static org.zwobble.precisely.AssertThat.assertThat;
import static org.zwobble.precisely.Matchers.*;

public class Json5ParserTests {
    // == Nulls ==

    @Test
    public void canParseNullLiteral() {
        var result = parseText("null");

        assertThat(result, isJson5Null(isSourceRange(0, 4)));
    }

    // == Booleans ==

    @Test
    public void canParseTrue() {
        var result = parseText("true");

        assertThat(result, isJson5Boolean(true, isSourceRange(0, 4)));
    }

    @Test
    public void canParseFalse() {
        var result = parseText("false");

        assertThat(result, isJson5Boolean(false, isSourceRange(0, 5)));
    }

    // == Strings ==

    @Test
    public void canParseEmptyStringUsingDoubleQuotes() {
        var result = parseText("\"\"");

        assertThat(result, isJson5String("", isSourceRange(0, 2)));
    }

    @Test
    public void canParseEmptyStringUsingSingleQuotes() {
        var result = parseText("''");

        assertThat(result, isJson5String("", isSourceRange(0, 2)));
    }

    @Test
    public void canParseDoubleQuotedStringContainingAsciiCharacters() {
        var result = parseText("\"abc123\"");

        assertThat(result, isJson5String("abc123", isSourceRange(0, 8)));
    }

    @Test
    public void canParseSingleQuotedStringContainingAsciiCharacters() {
        var result = parseText("'abc123'");

        assertThat(result, isJson5String("abc123", isSourceRange(0, 8)));
    }

    @Test
    public void canParseDoubleQuotedStringContainingSingleQuote() {
        var result = parseText("\"'\"");

        assertThat(result, isJson5String("'", isSourceRange(0, 3)));
    }

    @Test
    public void canParseSingleQuotedStringContainingDoubleQuote() {
        var result = parseText("'\"'");

        assertThat(result, isJson5String("\"", isSourceRange(0, 3)));
    }

    @Test
    public void canParseStringContainingEscapedSingleQuote() {
        var result = parseText("\"\\'\"");

        assertThat(result, isJson5String("'", isSourceRange(0, 4)));
    }

    @Test
    public void canParseStringContainingEscapedDoubleQuote() {
        var result = parseText("\"\\\"\"");

        assertThat(result, isJson5String("\"", isSourceRange(0, 4)));
    }

    @Test
    public void canParseStringContainingEscapedBackslash() {
        var result = parseText("\"\\\\\"");

        assertThat(result, isJson5String("\\", isSourceRange(0, 4)));
    }

    @Test
    public void canParseStringContainingEscapedBackspace() {
        var result = parseText("\"\\b\"");

        assertThat(result, isJson5String("\b", isSourceRange(0, 4)));
    }

    @Test
    public void canParseStringContainingEscapedFormFeed() {
        var result = parseText("\"\\f\"");

        assertThat(result, isJson5String("\f", isSourceRange(0, 4)));
    }

    @Test
    public void canParseStringContainingEscapedLineFeed() {
        var result = parseText("\"\\n\"");

        assertThat(result, isJson5String("\n", isSourceRange(0, 4)));
    }

    @Test
    public void canParseStringContainingEscapedCarriageReturn() {
        var result = parseText("\"\\r\"");

        assertThat(result, isJson5String("\r", isSourceRange(0, 4)));
    }

    @Test
    public void canParseStringContainingEscapedHorizontalTab() {
        var result = parseText("\"\\t\"");

        assertThat(result, isJson5String("\t", isSourceRange(0, 4)));
    }

    @Test
    public void canParseStringContainingEscapedVerticalTab() {
        var result = parseText("\"\\v\"");

        assertThat(result, isJson5String("\u000b", isSourceRange(0, 4)));
    }

    @Test
    public void canParseStringContainingEscapedNonEscapeCharacter() {
        var result = parseText("\"\\a\"");

        assertThat(result, isJson5String("a", isSourceRange(0, 4)));
    }

    @Test
    public void canParseDoubleQuotedStringContainingLineSeparator() {
        var result = parseText("\"\u2028\"");

        assertThat(result, isJson5String("\u2028", isSourceRange(0, 3)));
    }

    @Test
    public void canParseSingleQuotedStringContainingLineSeparator() {
        var result = parseText("'\u2028'");

        assertThat(result, isJson5String("\u2028", isSourceRange(0, 3)));
    }

    @Test
    public void canParseDoubleQuotedStringContainingParagraphSeparator() {
        var result = parseText("\"\u2029\"");

        assertThat(result, isJson5String("\u2029", isSourceRange(0, 3)));
    }

    @Test
    public void canParseSingleQuotedStringContainingParagraphSeparator() {
        var result = parseText("'\u2029'");

        assertThat(result, isJson5String("\u2029", isSourceRange(0, 3)));
    }

    @Test
    public void canParseStringContainingNullEscapeSequenceNotFollowedByDecimalDigit() {
        var result = parseText("\"\\0\"");

        assertThat(result, isJson5String("\0", isSourceRange(0, 4)));
    }

    @Test
    public void whenStringContainsNullEscapeSequenceThenDecimalDigitThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("\"\\01\"")
        );

        assertThat(
            error.getMessage(),
            equalTo("'\\0' cannot be followed by decimal digit")
        );
        assertThat(error.sourceRange(), isSourceRange(3, 4));
    }

    @Test
    public void canParseStringContainingHexEscapeSequence() {
        var result = parseText("\"\\x5b\"");

        assertThat(result, isJson5String("[", isSourceRange(0, 6)));
    }

    @Test
    public void canParseStringContainingUnicodeEscapeSequence() {
        var result = parseText("\"\\u03c0\"");

        assertThat(result, isJson5String("\u03c0", isSourceRange(0, 8)));
    }

    @Test
    public void canParseStringContainingLineContinuationWithLineFeed() {
        var result = parseText("\"abc\\\ndef\"");

        assertThat(result, isJson5String("abcdef", isSourceRange(0, 10)));
    }

    @Test
    public void canParseStringContainingLineContinuationWithCarriageReturnNoLineFeed() {
        var result = parseText("\"abc\\\rdef\"");

        assertThat(result, isJson5String("abcdef", isSourceRange(0, 10)));
    }

    @Test
    public void canParseStringContainingLineContinuationWithCarriageReturnThenLineFeed() {
        var result = parseText("\"abc\\\r\ndef\"");

        assertThat(result, isJson5String("abcdef", isSourceRange(0, 11)));
    }

    @Test
    public void canParseStringContainingLineContinuationWithLineSeparator() {
        var result = parseText("\"abc\\\u2028def\"");

        assertThat(result, isJson5String("abcdef", isSourceRange(0, 10)));
    }

    @Test
    public void canParseStringContainingLineContinuationWithParagraphSeparator() {
        var result = parseText("\"abc\\\u2029def\"");

        assertThat(result, isJson5String("abcdef", isSourceRange(0, 10)));
    }

    @Test
    public void whenStringContainsLineFeedAfterLineContinuationThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("\"\\\n\n\"")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected string character or '\"', but was '\n'")
        );
        assertThat(error.sourceRange(), isSourceRange(3, 4));
    }

    @Test
    public void whenUnclosedStringEndsWithBackslashThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("\"\\")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected escape sequence or line terminator, but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(2, 2));
    }

    @Test
    public void whenDoubleQuotedStringIsUnclosedThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("\"")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected string character or '\"', but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(1, 1));
    }

    @Test
    public void whenSingleQuotedStringIsUnclosedThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("'")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected string character or '\\'', but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(1, 1));
    }

    // == Numbers ==

    @Test
    public void canParseIntegerZero() {
        var result = parseText("0");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.ZERO,
            isSourceRange(0, 1)
        ));
    }

    @Test
    public void canParseIntegerZeroWithPositiveSign() {
        var result = parseText("+0");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.ZERO,
            isSourceRange(0, 2)
        ));
    }

    @Test
    public void canParseIntegerZeroWithNegativeSign() {
        var result = parseText("-0");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.ZERO,
            isSourceRange(0, 2)
        ));
    }

    @Test
    public void canParseDecimalIntegerWithoutSign() {
        var result = parseText("123");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.valueOf(123),
            isSourceRange(0, 3)
        ));
    }

    @Test
    public void canParseDecimalIntegerWithPositiveSign() {
        var result = parseText("+123");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.valueOf(123),
            isSourceRange(0, 4)
        ));
    }

    @Test
    public void canParseDecimalIntegerWithNegativeSign() {
        var result = parseText("-123");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.valueOf(-123),
            isSourceRange(0, 4)
        ));
    }

    @Test
    public void whenPlusSignIsNotFollowedByNumericLiteralThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("+")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected numeric literal, but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(1, 1));
    }

    @Test
    public void whenNegativeSignIsNotFollowedByNumericLiteralThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("-")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected numeric literal, but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(1, 1));
    }

    @Test
    public void canParseIntegerWithTrailingDot() {
        var result = parseText("123.");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.valueOf(123),
            isSourceRange(0, 4)
        ));
    }

    @Test
    public void integerCannotHaveLeadingZeroes() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("01")
        );

        assertThat(
            error.getMessage(),
            equalTo("Integer part of number cannot have leading zeroes")
        );
        assertThat(error.sourceRange(), isSourceRange(0, 2));
    }

    @Test
    public void canParseNumberWithIntegerPartAndFractionalPart() {
        var result = parseText("123.456");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal("123.456"),
            isSourceRange(0, 7)
        ));
    }

    @Test
    public void canParseNumberWithoutIntegerPartAndWithFractionalPart() {
        var result = parseText(".456");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal(".456"),
            isSourceRange(0, 4)
        ));
    }

    @Test
    public void whenThereIsNoIntegerPartThenDigitsAreExpectedAfterDot() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText(".")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected decimal digit, but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(1, 1));
    }

    @Test
    public void canParseNumberWithIntegerPartAndExponent() {
        var result = parseText("1e3");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal("1000"),
            isSourceRange(0, 3)
        ));
    }

    @Test
    public void canParseNumberWithIntegerPartAndFractionalPartAndExponent() {
        var result = parseText("1.4e3");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal("1400"),
            isSourceRange(0, 5)
        ));
    }

    @Test
    public void canParseNumberWithFractionalPartAndExponent() {
        var result = parseText(".4e3");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal("400"),
            isSourceRange(0, 4)
        ));
    }

    @Test
    public void exponentIndicatorCanBeLowercase() {
        var result = parseText("1e3");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal("1000"),
            isSourceRange(0, 3)
        ));
    }

    @Test
    public void exponentIndicatorCanBeUppercase() {
        var result = parseText("1E3");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal("1000"),
            isSourceRange(0, 3)
        ));
    }

    @Test
    public void whenExponentPartHasNoSignThenExponentIsPositive() {
        var result = parseText("1e3");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal("1000"),
            isSourceRange(0, 3)
        ));
    }

    @Test
    public void whenExponentPartHasPlusSignThenExponentIsPositive() {
        var result = parseText("1e+3");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal("1000"),
            isSourceRange(0, 4)
        ));
    }

    @Test
    public void whenExponentPartHasMinusSignThenExponentIsNegative() {
        var result = parseText("1e-3");

        assertThat(result, isJson5NumberFinite(
            new BigDecimal("0.001"),
            isSourceRange(0, 4)
        ));
    }

    @Test
    public void whenExponentIndicatorIsNotFollowedByDecimalDigitsThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("1e")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected decimal digit, but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(2, 2));
    }

    @Test
    public void whenLowercaseHexLiteralPrefixIsNotFollowedByHexDigitThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("0x")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected hex digit, but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(2, 2));
    }

    @Test
    public void whenUppercaseHexLiteralPrefixIsNotFollowedByHexDigitThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("0X")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected hex digit, but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(2, 2));
    }

    @Test
    public void hexLiteralPrefixCanBeLowercase() {
        var result = parseText("0x0");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.ZERO,
            isSourceRange(0, 3)
        ));
    }

    @Test
    public void hexLiteralPrefixCanBeUppercase() {
        var result = parseText("0X0");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.ZERO,
            isSourceRange(0, 3)
        ));
    }

    @Test
    public void canParseHexIntegerWithoutSign() {
        var result = parseText("0x19afAFcD");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.valueOf(0x19afAFcD),
            isSourceRange(0, 10)
        ));
    }

    @Test
    public void canParseHexIntegerWithPositiveSign() {
        var result = parseText("+0x19afAFcD");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.valueOf(0x19afAFcD),
            isSourceRange(0, 11)
        ));
    }

    @Test
    public void canParseHexIntegerWithNegativeSign() {
        var result = parseText("-0x19afAFcD");

        assertThat(result, isJson5NumberFinite(
            BigDecimal.valueOf(-0x19afAFcD),
            isSourceRange(0, 11)
        ));
    }

    @Test
    public void canParseInfinityWithoutSign() {
        var result = parseText("Infinity");

        assertThat(result, isJson5NumberPositiveInfinity(isSourceRange(0, 8)));
    }

    @Test
    public void canParseInfinityWithPositiveSign() {
        var result = parseText("+Infinity");

        assertThat(result, isJson5NumberPositiveInfinity(isSourceRange(0, 9)));
    }

    @Test
    public void canParseInfinityWithNegativeSign() {
        var result = parseText("-Infinity");

        assertThat(result, isJson5NumberNegativeInfinity(isSourceRange(0, 9)));
    }

    @Test
    public void canParseNanWithoutSign() {
        var result = parseText("NaN");

        assertThat(result, isJson5NumberNan(isSourceRange(0, 3)));
    }

    @Test
    public void canParseNanWithPositiveSign() {
        var result = parseText("+NaN");

        assertThat(result, isJson5NumberNan(isSourceRange(0, 4)));
    }

    @Test
    public void canParseNanWithNegativeSign() {
        var result = parseText("-NaN");

        assertThat(result, isJson5NumberNan(isSourceRange(0, 4)));
    }

    @Test
    public void sourceCharacterAfterNumericLiteralMustNotBeIdentifierStart() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("0x0g")
        );

        assertThat(
            error.getMessage(),
            equalTo(
                "The source character immediately following a numeric " +
                    "literal must not be the start of an identifier"
            )
        );
        assertThat(error.sourceRange(), isSourceRange(3, 4));
    }

    // == Objects ==

    @Test
    public void emptyObject() {
        var result = parseText("{}");

        assertThat(result, isJson5Object(isSequence(), isSourceRange(0, 2)));
    }

    @Test
    public void canParseObjectWithOneMemberAndNoTrailingComma() {
        var result = parseText("{foo: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isSourceRange(1, 4)),
                    isJson5Boolean(true, isSourceRange(6, 10)),
                    isSourceRange(1, 10)
                )
            ),
            isSourceRange(0, 11)
        ));
    }

    @Test
    public void canParseObjectWithOneMemberAndTrailingComma() {
        var result = parseText("{foo: true,}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isSourceRange(1, 4)),
                    isJson5Boolean(true, isSourceRange(6, 10)),
                    isSourceRange(1, 10)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void canParseObjectWithMultipleMembersAndNoTrailingComma() {
        var result = parseText("{foo:true,bar:false,baz:null}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isSourceRange(1, 4)),
                    isJson5Boolean(true, isSourceRange(5, 9)),
                    isSourceRange(1, 9)
                ),
                isJson5Member(
                    isJson5MemberName("bar", isSourceRange(10, 13)),
                    isJson5Boolean(false, isSourceRange(14, 19)),
                    isSourceRange(10, 19)
                ),
                isJson5Member(
                    isJson5MemberName("baz", isSourceRange(20, 23)),
                    isJson5Null(isSourceRange(24, 28)),
                    isSourceRange(20, 28)
                )
            ),
            isSourceRange(0, 29)
        ));
    }

    @Test
    public void canParseObjectWithMultipleMembersAndTrailingComma() {
        var result = parseText("{foo:true,bar:false,baz:null,}");


        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isSourceRange(1, 4)),
                    isJson5Boolean(true, isSourceRange(5, 9)),
                    isSourceRange(1, 9)
                ),
                isJson5Member(
                    isJson5MemberName("bar", isSourceRange(10, 13)),
                    isJson5Boolean(false, isSourceRange(14, 19)),
                    isSourceRange(10, 19)
                ),
                isJson5Member(
                    isJson5MemberName("baz", isSourceRange(20, 23)),
                    isJson5Null(isSourceRange(24, 28)),
                    isSourceRange(20, 28)
                )
            ),
            isSourceRange(0, 30)
        ));
    }

    @Test
    public void nestedObject() {
        var result = parseText("{a: {}}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("a", isSourceRange(1, 2)),
                    isJson5Object(
                        isSequence(),
                        isSourceRange(4, 6)
                    ),
                    isSourceRange(1, 6)
                )
            ),
            isSourceRange(0, 7)
        ));
    }

    @Test
    public void whenObjectHasTokenThatIsNeitherMemberNameNorClosingBraceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("{]}")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON member or '}', but was ']'")
        );
        assertThat(error.sourceRange(), isSourceRange(1, 2));
    }

    @Test
    public void whenObjectMemberIsMissingColonThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("{foo,}")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected ':', but was ','")
        );
        assertThat(error.sourceRange(), isSourceRange(4, 5));
    }

    @Test
    public void whenObjectHasMemberValueThatIsNeitherValueNorClosingBraceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("{foo:]}")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value, but was ']'")
        );
        assertThat(error.sourceRange(), isSourceRange(5, 6));
    }

    @Test
    public void whenObjectHasPostMemberTokenThatIsNeitherCommaNorClosingBraceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("{foo:null]}")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected ',' or '}', but was ']'")
        );
        assertThat(error.sourceRange(), isSourceRange(9, 10));
    }

    @Test
    public void whenObjectIsMissingClosingBraceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("{")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON member or '}', but was end of document")
        );
    }

    // === Member names ===

    @Test
    public void memberNameCanBeIdentifier() {
        var result = parseText("{foo: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isSourceRange(1, 4)),
                    isJson5Boolean(true, isSourceRange(6, 10)),
                    isSourceRange(1, 10)
                )
            ),
            isSourceRange(0, 11)
        ));
    }

    @Test
    public void memberNameCanBeReservedWord() {
        var result = parseText("{null: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("null", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanStartWithDollar() {
        var result = parseText("{$foo: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("$foo", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanContainDollar() {
        var result = parseText("{foo$: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo$", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanStartWithUnderscore() {
        var result = parseText("{_foo: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("_foo", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanContainUnderscore() {
        var result = parseText("{foo_: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo_", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanStartWithUnicodeEscapeSequence() {
        var result = parseText("{\\u03c0foo: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("\u03c0foo", isSourceRange(1, 10)),
                    isJson5Boolean(true, isSourceRange(12, 16)),
                    isSourceRange(1, 16)
                )
            ),
            isSourceRange(0, 17)
        ));
    }

    @Test
    public void memberNameCanContainUnicodeEscapeSequence() {
        var result = parseText("{foo\\u03c0: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo\u03c0", isSourceRange(1, 10)),
                    isJson5Boolean(true, isSourceRange(12, 16)),
                    isSourceRange(1, 16)
                )
            ),
            isSourceRange(0, 17)
        ));
    }

    @Test
    public void whenMemberNameContainsSyntacticallyInvalidUnicodeEscapeSequenceThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("{foo\\u20: true}")
        );

        assertThat(error.getMessage(), equalTo("Expected hex digit, but was ':'"));
        assertThat(error.sourceRange(), isSourceRange(8, 9));
    }

    @Test
    public void memberNameCanContainUnicodeNonSpacingMark() {
        var result = parseText("{foo\u0300: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo\u0300", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanContainUnicodeCombiningSpacingMark() {
        var result = parseText("{foo\u0903: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo\u0903", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanContainUnicodeDecimalNumber() {
        var result = parseText("{foo0: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo0", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanContainUnicodeConnectorPunctation() {
        var result = parseText("{foo\u203f: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo\u203f", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanContainUnicodeZeroWidthNonJoiner() {
        var result = parseText("{foo\u200c: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo\u200c", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanContainUnicodeZeroWidthJoiner() {
        var result = parseText("{foo\u200d: true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo\u200d", isSourceRange(1, 5)),
                    isJson5Boolean(true, isSourceRange(7, 11)),
                    isSourceRange(1, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    @Test
    public void memberNameCanBeDoubleQuotedString() {
        var result = parseText("{\"foo\": true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isSourceRange(1, 6)),
                    isJson5Boolean(true, isSourceRange(8, 12)),
                    isSourceRange(1, 12)
                )
            ),
            isSourceRange(0, 13)
        ));
    }

    @Test
    public void memberNameCanBeSingleQuotedString() {
        var result = parseText("{'foo': true}");

        assertThat(result, isJson5Object(
            isSequence(
                isJson5Member(
                    isJson5MemberName("foo", isSourceRange(1, 6)),
                    isJson5Boolean(true, isSourceRange(8, 12)),
                    isSourceRange(1, 12)
                )
            ),
            isSourceRange(0, 13)
        ));
    }

    // == Arrays ==

    @Test
    public void emptyArray() {
        var result = parseText("[]");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(0, 2)));
    }

    @Test
    public void canParseArrayWithOneElementAndNoTrailingComma() {
        var result = parseText("[true]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5Boolean(true, isSourceRange(1, 5))
            ),
            isSourceRange(0, 6)
        ));
    }

    @Test
    public void canParseArrayWithOneElementAndTrailingComma() {
        var result = parseText("[true,]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5Boolean(true, isSourceRange(1, 5))
            ),
            isSourceRange(0, 7)
        ));
    }

    @Test
    public void canParseArrayWithMultipleElementsAndNoTrailingComma() {
        var result = parseText("[true, false, null]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5Boolean(true, isSourceRange(1, 5)),
                isJson5Boolean(false, isSourceRange(7, 12)),
                isJson5Null(isSourceRange(14, 18))
            ),
            isSourceRange(0, 19)
        ));
    }

    @Test
    public void canParseArrayWithMultipleElementsAndTrailingComma() {
        var result = parseText("[true, false, null,]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5Boolean(true, isSourceRange(1, 5)),
                isJson5Boolean(false, isSourceRange(7, 12)),
                isJson5Null(isSourceRange(14, 18))
            ),
            isSourceRange(0, 20)
        ));
    }

    @Test
    public void nestedArray() {
        var result = parseText("[[]]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5Array(
                    isSequence(),
                    isSourceRange(1, 3)
                )
            ),
            isSourceRange(0, 4)
        ));
    }

    @Test
    public void whenArrayHasElementThatIsNeitherValueNorClosingSquareBracketThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("[}]")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value or ']', but was '}'")
        );
        assertThat(error.sourceRange(), isSourceRange(1, 2));
    }

    @Test
    public void whenArrayHasPostElementTokenThatIsNeitherCommaNorClosingSquareBracketThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("[null}]")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected ',' or ']', but was '}'")
        );
        assertThat(error.sourceRange(), isSourceRange(5, 6));
    }

    @Test
    public void whenArrayIsMissingClosingSquareBracketThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("[")
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
            () -> parseText("")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected JSON value, but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(0, 0));
    }

    @Test
    public void whenDocumentHasTokensAfterValueThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("[][")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected end of document, but was '['")
        );
        assertThat(error.sourceRange(), isSourceRange(2, 3));
    }

    @Test
    public void whitespaceIsIgnored() {
        var result = parseText("   [ ]  ");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(3, 6)));
    }

    @Test
    public void tabIsTreatedAsWhiteSpace() {
        var result = parseText("\t[]");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(1, 3)));
    }

    @Test
    public void verticalTabIsTreatedAsWhiteSpace() {
        var result = parseText("\u000b[]");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(1, 3)));
    }

    @Test
    public void formFeedIsTreatedAsWhiteSpace() {
        var result = parseText("\f[]");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(1, 3)));
    }

    @Test
    public void noBreakSpaceIsTreatedAsWhiteSpace() {
        var result = parseText("\u00a0[]");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(1, 3)));
    }

    @Test
    public void spaceIsTreatedAsWhiteSpace() {
        var result = parseText(" []");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(1, 3)));
    }

    @Test
    public void byteOrderMarkIsTreatedAsWhiteSpace() {
        var result = parseText("\ufeff[]");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(1, 3)));
    }

    @Test
    public void anyOtherUnicodeSpaceSeparatorIsTreatedAsWhiteSpace() {
        var result = parseText("\u2000[]");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(1, 3)));
    }

    @Test
    public void lineTerminatorLineFeedIsIgnored() {
        var result = parseText("\n\n\n[\n]\n\n");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(3, 6)));
    }

    @Test
    public void lineTerminatorCarriageReturnIsIgnored() {
        var result = parseText("\r\r\r[\r]\r\r");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(3, 6)));
    }

    @Test
    public void lineTerminatorLineSeparatorIsIgnored() {
        var result = parseText("\u2028\u2028\u2028[\u2028]\u2028\u2028");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(3, 6)));
    }

    @Test
    public void lineTerminatorParagraphSeparatorIsIgnored() {
        var result = parseText("\u2029\u2029\u2029[\u2029]\u2029\u2029");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(3, 6)));
    }

    @Test
    public void multiLineCommentIsIgnored() {
        var result = parseText("/* a */[/* b\n\nc */] /** d*  **/");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(7, 19)));
    }

    @Test
    public void whenMultiLineCommentIsNotClosedThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("[]/*")
        );

        assertThat(
            error.getMessage(),
            equalTo("Expected '*/', but was end of document")
        );
        assertThat(error.sourceRange(), isSourceRange(4, 4));
    }

    @Test
    public void singleLineCommentIsIgnored() {
        var result = parseText("// a\n[// b\n] // c\n\n");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(5, 12)));
    }

    @Test
    public void singleLineCommentCanBeClosedByEndOfDocument() {
        var result = parseText("[]//");

        assertThat(result, isJson5Array(isSequence(), isSourceRange(0, 2)));
    }

    @Test
    public void whenCharacterCannotBeTokenizedThenErrorIsThrown() {
        var error = assertThrows(
            Json5ParseError.class,
            () -> parseText("=")
        );


        assertThat(
            error.getMessage(),
            equalTo("Expected JSON5 token, but was '='")
        );
        assertThat(error.sourceRange(), isSourceRange(0, 1));
    }

    @Test
    public void whenSurrogatePairsAreUsedThenCharacterIndexCountsEachSurrogateAsOneCharacter() {
        var result = parseText("[\"\uD83E\uDD67\", true]");

        assertThat(result, isJson5Array(
            isSequence(
                isJson5String(
                    "\uD83E\uDD67",
                    isSourceRange(1, 5)
                ),
                isJson5Boolean(
                    true,
                    isSourceRange(7, 11)
                )
            ),
            isSourceRange(0, 12)
        ));
    }

    // == Paths ==

    @Test
    public void pathsAreGeneratedForObjectMembers() {
        var result = parseText("{a: {b: true}}");

        assertThat(result, instanceOf(
            Json5Object.class,
            has("path", x -> x.path(), equalTo(Json5Path.ROOT)),
            has("members", x -> x.members(), isSequence(
                allOf(
                    has("name", x -> x.name().value(), equalTo("a")),
                    has("value", x -> x.value(), instanceOf(
                        Json5Object.class,
                        has("path", x -> x.path(), equalTo(Json5Path.ROOT.member("a"))),
                        has("members", x -> x.members(), isSequence(
                            allOf(
                                has("name", x -> x.name().value(), equalTo("b")),
                                has("value", x -> x.value(), instanceOf(
                                    Json5Boolean.class,
                                    has("path", x -> x.path(), equalTo(Json5Path.ROOT.member("a").member("b")))
                                ))
                            )
                        ))
                    ))
                )
            ))
        ));
    }

    @Test
    public void pathsAreGeneratedForArrayElements() {
        var result = parseText("[[true], false]");

        assertThat(result, instanceOf(
            Json5Array.class,
            has("path", x -> x.path(), equalTo(Json5Path.ROOT)),
            has("elements", x -> x.elements(), isSequence(
                instanceOf(
                    Json5Array.class,
                    has("path", x -> x.path(), equalTo(Json5Path.ROOT.index(0))),
                    has("elements", x -> x.elements(), isSequence(
                        instanceOf(
                            Json5Boolean.class,
                            has("path", x -> x.path(), equalTo(Json5Path.ROOT.index(0).index(0)))
                        )
                    ))
                ),
                instanceOf(
                    Json5Boolean.class,
                    has("path", x -> x.path(), equalTo(Json5Path.ROOT.index(1)))
                )
            ))
        ));
    }

    // == Test Helpers ==

    private Json5Value parseText(String text) {
        return Json5Parser.parse(SourceText.fromString("<string>", text));
    }
}
