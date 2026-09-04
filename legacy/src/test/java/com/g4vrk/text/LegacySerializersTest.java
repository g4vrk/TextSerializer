package com.g4vrk.text;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SuppressWarnings("deprecation")
class LegacySerializersTest {

    private static final PlainComponentSerializer PLAIN = PlainComponentSerializer.plain();

    @Test
    void exposesStableSingletonSerializers() {
        final Serializers serializers = LegacySerializers.INSTANCE;

        assertSame(serializers.universalSerializer(), serializers.universalSerializer());
        assertSame(serializers.miniMessageSerializer(), serializers.miniMessageSerializer());
        assertSame(serializers.legacySerializer(), serializers.legacySerializer());
        assertSame(serializers.legacyConverter(), serializers.legacyConverter());
        assertSame(serializers.miniMessageConverter(), serializers.miniMessageConverter());
        assertSame(serializers.plainSerializer(), serializers.plainSerializer());
    }

    @Test
    void parsesMiniMessageAndLegacyTogether() {
        final Serializers serializers = LegacySerializers.INSTANCE;
        final String legacy = LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build()
                .serialize(serializers.universalSerializer().serialize("&l<red>x</red>y"));

        assertEquals("§c§lx§r§ly", legacy);
    }

    @Test
    void preservesUnicodeAndLiteralAmpersands() {
        final Serializers serializers = LegacySerializers.INSTANCE;
        assertEquals(
                "Привет 😀 & мир",
                PLAIN.serialize(serializers.universalSerializer()
                        .serialize("<gradient:red:blue>Привет 😀 & мир</gradient>"))
        );
        assertEquals("R&D", PLAIN.serialize(serializers.legacySerializer().serialize("R&D")));
    }

    @Test
    void convertsNestedMiniMessageAndEscapesToLegacy() {
        final Serializers serializers = LegacySerializers.INSTANCE;

        assertEquals(
                "§c§lx§r§ly",
                serializers.legacyConverter().serialize("<bold><red>x</red>y</bold>")
        );
        assertEquals(
                "<red>literal</red>",
                PLAIN.serialize(serializers.miniMessageSerializer()
                        .serialize("\\<red>literal\\</red>"))
        );
    }
}
