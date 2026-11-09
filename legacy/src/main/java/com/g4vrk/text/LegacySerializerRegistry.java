package com.g4vrk.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.function.Function;

/**
 * Serializer entry point for Paper servers without native MiniMessage.
 *
 * <p>Plain text goes through {@link PlainTextComponentSerializer} when the server has it
 * (Adventure 4.8+, and the only one left in Adventure 5) and through the old
 * {@link PlainComponentSerializer} on 1.16.5. Each branch sits in its own method, so the
 * class of the other one is never loaded.</p>
 */
public final class LegacySerializerRegistry extends BaseSerializerRegistry {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    public static final SerializerRegistry INSTANCE = new LegacySerializerRegistry();

    private LegacySerializerRegistry() {
        super(MINI_MESSAGE::deserialize, plainText());
    }

    private static Function<Component, String> plainText() {
        try {
            Class.forName("net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer");
            return modernPlainText();
        } catch (final ClassNotFoundException absent) {
            return legacyPlainText();
        }
    }

    private static Function<Component, String> modernPlainText() {
        return PlainTextComponentSerializer.plainText()::serialize;
    }

    @SuppressWarnings("deprecation")
    private static Function<Component, String> legacyPlainText() {
        return PlainComponentSerializer.plain()::serialize;
    }
}
