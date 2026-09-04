package com.g4vrk.text.serializer.impl.string;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyTextSerializerTest {

    private static final LegacyTextSerializer SERIALIZER = new LegacyTextSerializer();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @ParameterizedTest
    @ValueSource(strings = {"R&D", "A & B", "&&", "&zunknown", "trailing&"})
    void preservesLiteralAmpersands(final String input) {
        assertEquals(input, PLAIN.serialize(SERIALIZER.serialize(input)));
    }

    @Test
    void acceptsUppercaseLegacyCodes() {
        final Component component = SERIALIZER.serialize("&Cupper");
        assertEquals("upper", PLAIN.serialize(component));
        assertEquals("§cupper", LegacyTextSerializer.LEGACY_SERIALIZER.serialize(component));
    }

    @Test
    void acceptsAllHexRepresentations() {
        final String expected = "§x§1§2§a§b§e§fhex";

        assertEquals(expected, canonical("&#12abEFhex"));
        assertEquals(expected, canonical("#12abEFhex"));
        assertEquals(expected, canonical("&X&1&2&A&B&E&Fhex"));
    }

    private static String canonical(final String input) {
        return LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build()
                .serialize(SERIALIZER.serialize(input));
    }
}
