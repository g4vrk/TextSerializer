package com.g4vrk.text;

import com.g4vrk.text.engine.LegacyMiniMessageEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.function.Function;

/**
 * Serializer entry point for Paper servers without native MiniMessage.
 *
 * <p>Adventure before 4.10 uses an isolated parser and converts its result through component JSON.
 * All public components remain server types. Newer Adventure uses the direct parser.</p>
 *
 * <p>Plain text goes through {@link PlainTextComponentSerializer} when the server has it
 * (Adventure 4.8+, and the only one left in Adventure 5) and through the old
 * {@link PlainComponentSerializer} on 1.16.5. Each branch sits in its own method, so the
 * class of the other one is never loaded.</p>
 */
public final class LegacySerializerRegistry extends BaseSerializerRegistry {

    public static final SerializerRegistry INSTANCE = new LegacySerializerRegistry();

    private LegacySerializerRegistry() {
        super(parser(), plainText());
    }

    private static MiniMessageAdapter parser() {
        try {
            Component.class.getMethod("compact");
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            return ModernParser.adapter();
        } catch (final NoSuchMethodException | ClassNotFoundException absent) {
            return new LegacyParser();
        }
    }

    private static final class ModernParser {

        private static MiniMessageAdapter adapter() {
            return MiniMessage.miniMessage()::deserialize;
        }

    }

    private static final class LegacyParser implements MiniMessageAdapter {

        @Override
        public Component deserialize(
                final String source,
                final TagResolver... resolvers
        ) {
            if (resolvers.length != 0) {
                throw new IllegalArgumentException("Use TagMapper on servers with Adventure below 4.10");
            }
            return GsonComponentSerializer.gson().deserialize(
                    LegacyMiniMessageEngine.deserialize(source)
            );
        }

        @Override
        public Component deserialize(final String source, final TagMapper mapper) {
            return GsonComponentSerializer.gson().deserialize(
                    LegacyMiniMessageEngine.deserialize(source, mapper::map, mapper::has)
            );
        }

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
