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
    private final java.util.function.BiFunction<String,
            net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[], Component> contextualMiniMessage;

    public MiniMessageTextSerializer(
            final @NotNull Function<String, Component> anonymousMiniMessage
    ) {
        this(anonymousMiniMessage, (source, resolvers) ->
                net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(source, resolvers));
    }

    /** Both functions must use the same parser configuration. */
    public MiniMessageTextSerializer(Function<String, Component> anonymousMiniMessage,
            java.util.function.BiFunction<String,
                    net.kyori.adventure.text.minimessage.tag.resolver.TagResolver[], Component> contextualMiniMessage) {
        this.anonymousMiniMessage = Objects.requireNonNull(anonymousMiniMessage);
        this.contextualMiniMessage = Objects.requireNonNull(contextualMiniMessage);
    }

    @Override
    public @NotNull Component serialize(final @NotNull String source,
            final net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers) {
        if (resolvers.length == 0) return serialize(source);
        // Resolver identity and mutable context cannot safely be represented by the source key.
        return contextualMiniMessage.apply(source, resolvers);
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
