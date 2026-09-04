package com.g4vrk.text.serializer.impl.string;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.g4vrk.text.serializer.impl.String2ComponentSerializer;
import com.g4vrk.text.util.Legacy2MiniMessage;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.AMPERSAND_CHAR;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.SECTION_CHAR;

public final class UniversalTextSerializer implements String2ComponentSerializer {

    private static final char MINI_MESSAGE_TAG_START = '<';
    private static final char MINI_MESSAGE_TAG_END = '>';

    private static final int CACHE_SIZE = 16_384;

    private final Cache<String, Component> cache =
            Caffeine.newBuilder()
                    .initialCapacity(4_096)
                    .maximumSize(CACHE_SIZE)
                    .build();

    private final String2ComponentSerializer miniMessageSerializer;
    private final String2ComponentSerializer legacySerializer;

    public UniversalTextSerializer(
            final @NotNull String2ComponentSerializer miniMessageSerializer,
            final @NotNull String2ComponentSerializer legacySerializer
    ) {

        this.miniMessageSerializer = Objects.requireNonNull(
                miniMessageSerializer
        );

        this.legacySerializer = Objects.requireNonNull(
                legacySerializer
        );

    }

    @Override
    public @NotNull Component serialize(
            final @NotNull String source
    ) {

        if (source.isEmpty()) {
            return Component.empty();
        }

        return cache.get(
                source,
                this::serializeUncached
        );

    }

    private @NotNull Component serializeUncached(
            final @NotNull String source
    ) {

        final boolean miniMessage =
                source.indexOf(MINI_MESSAGE_TAG_START) != -1 &&
                        source.indexOf(MINI_MESSAGE_TAG_END) != -1;

        final boolean legacy =
                source.indexOf(AMPERSAND_CHAR) != -1 ||
                        source.indexOf(SECTION_CHAR) != -1;

        if (!miniMessage && !legacy) {
            return Component.text(source);
        }

        if (miniMessage) {

            if (legacy) {
                return miniMessageSerializer.serialize(
                        Legacy2MiniMessage.convert(source)
                );
            }

            return miniMessageSerializer.serialize(
                    source
            );

        }

        return legacySerializer.serialize(
                source
        );

    }

}
