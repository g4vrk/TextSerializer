package com.g4vrk.text.serializer.impl.string;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.g4vrk.text.serializer.impl.String2ComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class LegacyTextSerializer implements String2ComponentSerializer {

    public static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character(LegacyComponentSerializer.SECTION_CHAR)
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private static final int CACHE_SIZE = 16_384;

    private static final Cache<String, Component> CACHE =
            Caffeine.newBuilder()
                    .initialCapacity(4_096)
                    .maximumSize(CACHE_SIZE)
                    .build();

    @Override
    public @NotNull Component serialize(
            final @NotNull String source
    ) {

        return CACHE.get(
                source,
                LegacyTextSerializer::serializeUncached
        );

    }

    private static @NotNull Component serializeUncached(
            @NotNull String source
    ) {

        final String normalized = LegacyTextConverter.normalizeLegacy(source);

        if (normalized.indexOf(LegacyComponentSerializer.SECTION_CHAR) == -1) {
            return Component.text(source);
        }

        return LEGACY_SERIALIZER.deserialize(normalized);

    }

}
