package com.g4vrk.text.serializer.impl.component;

import com.g4vrk.text.serializer.impl.Component2StringSerializer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public final class PlainTextSerializer implements Component2StringSerializer {

    private static final int CACHE_SIZE = 16_384;

    private final Cache<Component, String> cache =
            Caffeine.newBuilder()
                    .initialCapacity(4_096)
                    .maximumSize(CACHE_SIZE)
                    .build();

    private final Function<Component, String> anonymousPlainText;

    public PlainTextSerializer(
            final @NotNull Function<Component, String> anonymousPlainText
    ) {
        this.anonymousPlainText = Objects.requireNonNull(
                anonymousPlainText
        );
    }

    @Override
    public @NotNull String serialize(
            final @NotNull Component source
    ) {

        return this.cache.get(
                source,
                this::serializeUncached
        );

    }

    private @NotNull String serializeUncached(
            final @NotNull Component source
    ) {

        return anonymousPlainText.apply(source);

    }

}
