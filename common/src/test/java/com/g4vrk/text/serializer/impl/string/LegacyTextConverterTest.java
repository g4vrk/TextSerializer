package com.g4vrk.text.serializer.impl.string;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyTextConverterTest {

    private static final LegacyTextConverter CONVERTER = new LegacyTextConverter();

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "&aGreen|§aGreen",
            "&AGreen|§aGreen",
            "§CBold case|§cBold case",
            "&#12abEFhex|§x§1§2§A§B§E§Fhex",
            "#12abEFhex|§x§1§2§A§B§E§Fhex",
            "&X&1&2&A&B&E&Frepeated|§x§1§2§A§B§E§Frepeated"
    })
    void normalizesEverySupportedLegacyForm(final String input, final String expected) {
        assertEquals(expected, CONVERTER.serialize(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "R&D", "A & B", "&&", "&zunknown", "trailing&", "&#12zzzz",
            "&c&l", "§C§L", "&#12abEF&l", "&X&1&2&A&B&E&F&r"
    })
    void preservesNonFormattingAmpersands(final String input) {
        assertEquals(input, CONVERTER.serialize(input));
    }

    @Test
    void parsesMiniMessageUsingAdventureSemantics() {
        assertEquals(
                MiniMessage2LegacyTestSupport.reference("<bold><red>x</red>y</bold>"),
                CONVERTER.serialize("<bold><red>x</red>y</bold>")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plain", "R&D", "&Cupper", "&#12abEFhex",
            "<bold><red>x</red>y</bold>", "<gradient:red:blue>😀</gradient>"
    })
    void normalizationIsIdempotent(final String input) {
        final String once = CONVERTER.serialize(input);
        assertEquals(once, CONVERTER.serialize(once));
    }
}
