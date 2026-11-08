package com.g4vrk.text;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class NativeSerializerRegistryTest {

    private static final PlainTextComponentSerializer PLAIN =
            PlainTextComponentSerializer.plainText();

    @Test
    void exposesStableSingletonSerializers() {
        final SerializerRegistry serializers = NativeSerializerRegistry.INSTANCE;

        assertSame(serializers.universalSerializer(), serializers.universalSerializer());
        assertSame(serializers.miniMessageSerializer(), serializers.miniMessageSerializer());
        assertSame(serializers.legacySerializer(), serializers.legacySerializer());
        assertSame(serializers.legacyConverter(), serializers.legacyConverter());
        assertSame(serializers.miniMessageConverter(), serializers.miniMessageConverter());
        assertSame(serializers.plainSerializer(), serializers.plainSerializer());
    }

    @Test
    void parsesMiniMessageAndLegacyTogether() {
        final SerializerRegistry serializers = NativeSerializerRegistry.INSTANCE;
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
        final SerializerRegistry serializers = NativeSerializerRegistry.INSTANCE;
        assertEquals(
                "Привет 😀 & мир",
                PLAIN.serialize(serializers.universalSerializer()
                        .serialize("<gradient:red:blue>Привет 😀 & мир</gradient>"))
        );
        assertEquals("R", PLAIN.serialize(serializers.legacySerializer().serialize("R&D")));
    }

    @Test
    void convertsNestedMiniMessageAndEscapesToLegacy() {
        final SerializerRegistry serializers = NativeSerializerRegistry.INSTANCE;

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
