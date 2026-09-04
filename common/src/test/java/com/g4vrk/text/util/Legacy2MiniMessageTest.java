package com.g4vrk.text.util;

import com.g4vrk.text.serializer.impl.string.LegacyTextConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Legacy2MiniMessageTest {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final LegacyTextConverter NORMALIZER = new LegacyTextConverter();

    @ParameterizedTest
    @ValueSource(strings = {
            "&0black", "&9blue", "&agreen", "&Cupper-red", "§Lbold",
            "&lbold &ounderlined", "&rreset", "&#12abEFhex",
            "&X&1&2&A&B&E&Frepeated", "&x§1&2§A&B§E&Fmixed",
            "prefix &cRed &lBold suffix"
    })
    void preservesLegacySemantics(final String input) {
        final Component expected = LEGACY.deserialize(NORMALIZER.serialize(input));
        final Component actual = MINI_MESSAGE.deserialize(Legacy2MiniMessage.convert(input));

        assertEquals(LEGACY.serialize(expected), LEGACY.serialize(actual));
    }

    @ParameterizedTest
    @MethodSource("allClassicLegacyCodes")
    void supportsEveryClassicCodeInEitherCase(final String input) {
        final Component expected = LEGACY.deserialize(NORMALIZER.serialize(input));
        final Component actual = MINI_MESSAGE.deserialize(Legacy2MiniMessage.convert(input));

        assertEquals(LEGACY.serialize(expected), LEGACY.serialize(actual));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "R&D", "A & B", "&&", "&zunknown", "trailing&",
            "&#12zzzz", "&x&1&2&3", "&c&l", "§C§L",
            "section §Z stays"
    })
    void preservesUnknownAndMalformedSequences(final String input) {
        assertEquals(input, Legacy2MiniMessage.convert(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "&l<red>x</red>y",
            "<bold>&cred</bold>after",
            "<italic>&aGreen <bold>&lBold</bold></italic>"
    })
    void keepsMixedMarkupWellNestedAndParseable(final String input) {
        final String converted = Legacy2MiniMessage.convert(input);
        MINI_MESSAGE.deserialize(converted);
    }

    @Test
    void doesNotInterpretAmpersandsInsideTagArguments() {
        final String converted = Legacy2MiniMessage.convert(
                "<click:open_url:'https://example.test?a=1&b=2'>link</click> &cred"
        );

        assertTrue(converted.contains("a=1&b=2"));
        assertEquals(
                "link red",
                net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                        .plainText()
                        .serialize(MINI_MESSAGE.deserialize(converted))
        );
    }

    private static Stream<String> allClassicLegacyCodes() {
        return "0123456789abcdefklmnor".chars()
                .mapToObj(value -> (char) value)
                .flatMap(code -> Stream.of(
                        "&" + code + "value",
                        "&" + Character.toUpperCase(code) + "value",
                        "§" + code + "value"
                ));
    }
}
