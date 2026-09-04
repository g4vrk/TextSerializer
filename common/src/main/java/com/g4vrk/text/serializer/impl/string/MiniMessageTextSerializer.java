package com.g4vrk.text.serializer.impl.string;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.g4vrk.text.serializer.impl.String2ComponentSerializer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public final class MiniMessageTextSerializer implements String2ComponentSerializer {

    private static final char MINI_MESSAGE_TAG_START = '<';
    private static final char MINI_MESSAGE_TAG_END = '>';

    private static final int CACHE_SIZE = 16_384;

    private final Cache<String, Component> cache =
            Caffeine.newBuilder()
                    .initialCapacity(4_096)
                    .maximumSize(CACHE_SIZE)
                    .build();

    private final Function<String, Component> anonymousMiniMessage;

    public MiniMessageTextSerializer(
            final @NotNull Function<String, Component> anonymousMiniMessage
    ) {
        this.anonymousMiniMessage = Objects.requireNonNull(
                anonymousMiniMessage
        );
    }

    @Override
    public @NotNull Component serialize(
            final @NotNull String source
    ) {

        return this.cache.get(
                source,
                this::serializeUncached
        );

    }

    private @NotNull Component serializeUncached(
            final @NotNull String source
    ) {

        if (
                source.indexOf(MINI_MESSAGE_TAG_START) == -1 ||
                        source.indexOf(MINI_MESSAGE_TAG_END) == -1
        ) {
            return Component.text(source);
        }

        try {
            return anonymousMiniMessage.apply(source);
        } catch (final RuntimeException ignored) {
            return Component.text(source);
        }

    }

}
