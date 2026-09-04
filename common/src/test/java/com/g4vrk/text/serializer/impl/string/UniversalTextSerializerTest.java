package com.g4vrk.text.serializer.impl.string;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UniversalTextSerializerTest {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final UniversalTextSerializer SERIALIZER = new UniversalTextSerializer(
            new MiniMessageTextSerializer(MINI_MESSAGE::deserialize),
            new LegacyTextSerializer()
    );

    @Test
    void preservesLiteralAmpersandsInMiniMessage() {
        assertEquals(
                "A & B",
                PLAIN.serialize(SERIALIZER.serialize("<red>A & B</red>"))
        );
    }

    @Test
    void preservesStylesWhenLegacyAndMiniMessageAreMixed() {
        final Component result = SERIALIZER.serialize("&l<red>x</red>y");
        final String legacy = LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build()
                .serialize(result);

        assertEquals("§c§lx§r§ly", legacy);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plain text", "R&D", "1 < 2 & 3 > 0", "Привет, мир!", "emoji 😀"
    })
    void preservesPlainText(final String input) {
        assertEquals(input, PLAIN.serialize(SERIALIZER.serialize(input)));
    }

    @Test
    void usesCanonicalEmptyComponent() {
        assertEquals(Component.empty(), SERIALIZER.serialize(""));
    }
}
