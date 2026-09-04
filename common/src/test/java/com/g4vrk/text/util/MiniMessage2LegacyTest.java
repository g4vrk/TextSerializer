package com.g4vrk.text.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiniMessage2LegacyTest {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    @ParameterizedTest
    @ValueSource(strings = {
            "plain text",
            "<bold><red>x</red>y</bold>",
            "<italic><#12abef>hex</#12abef></italic>",
            "\\<red>literal\\</red>",
            "<gradient:red:blue>😀</gradient>",
            "<gradient:red:green:blue:0.25>abcdef</gradient>",
            "<gradient:#ff0000:#0000ff><bold>nested</bold></gradient>",
            "<reset>reset"
    })
    void matchesAdventureReferenceImplementation(final String input) {
        final String expected = LEGACY.serialize(MINI_MESSAGE.deserialize(input));
        assertEquals(expected, MiniMessage2Legacy.convert(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<gradient:red:blue>😀</gradient>",
            "<gradient:#123456:#abcdef>𝄞</gradient>",
            "<gradient:yellow:aqua>👨‍👩‍👧‍👦</gradient>"
    })
    void neverSplitsUtf16SurrogatePairs(final String input) {
        final String output = MiniMessage2Legacy.convert(input);

        for (int index = 0; index < output.length(); index++) {
            final char current = output.charAt(index);
            if (Character.isHighSurrogate(current)) {
                assertTrue(index + 1 < output.length());
                assertTrue(Character.isLowSurrogate(output.charAt(index + 1)));
            }
            if (Character.isLowSurrogate(current)) {
                assertTrue(index > 0);
                assertTrue(Character.isHighSurrogate(output.charAt(index - 1)));
            }
        }
    }
}
