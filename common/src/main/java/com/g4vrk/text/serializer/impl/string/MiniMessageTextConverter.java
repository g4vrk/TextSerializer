package com.g4vrk.text.serializer.impl.string;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.g4vrk.text.serializer.impl.String2StringSerializer;
import com.g4vrk.text.util.Legacy2MiniMessage;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.AMPERSAND_CHAR;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.SECTION_CHAR;

public final class MiniMessageTextConverter implements String2StringSerializer {

    private static final int CACHE_SIZE = 16_384;

    private static final Cache<String, String> CACHE =
            Caffeine.newBuilder()
                    .initialCapacity(4_096)
                    .maximumSize(CACHE_SIZE)
                    .build();

    @Override
    public @NotNull String serialize(
            final @NotNull String source
    ) {

        final int length = source.length();

        if (length < 2) {
            return source;
        }

        if (
                source.indexOf(AMPERSAND_CHAR) == -1 &&
                        source.indexOf(SECTION_CHAR) == -1
        ) {
            return source;
        }

        return CACHE.get(
                source,
                MiniMessageTextConverter::serializeUncached
        );

    }

    private static @NotNull String serializeUncached(
            final @NotNull String source
    ) {

        return Legacy2MiniMessage.convert(source);

    }

}
