package com.g4vrk.text.serializer.impl.string;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.g4vrk.text.MiniMessageAdapter;
import com.g4vrk.text.serializer.impl.String2ComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Parses MiniMessage markup through the MiniMessage of the running server.
 *
 * <p>Text without a tag is answered as a plain component, and a text that fails to parse falls
 * back to plain text instead of breaking the caller. Results are cached, because configs hold
 * the same strings for the whole life of the server.</p>
 */
public final class MiniMessageTextSerializer implements String2ComponentSerializer {

    private static final char TAG_START = '<';
    private static final char TAG_END = '>';

    private static final int CACHE_SIZE = 16_384;

    private final Cache<String, Component> cache = Caffeine.newBuilder()
            .initialCapacity(4_096)
            .maximumSize(CACHE_SIZE)
            .build();

    private final MiniMessageAdapter miniMessage;

    public MiniMessageTextSerializer(final @NotNull MiniMessageAdapter miniMessage) {
        this.miniMessage = Objects.requireNonNull(miniMessage);
    }

    @Override
    public @NotNull Component serialize(final @NotNull String source) {
        return this.cache.get(source, this::serializeUncached);
    }

    @Override
    public @NotNull Component serialize(
            final @NotNull String source,
            final @NotNull TagResolver @NotNull ... resolvers
    ) {
        if (resolvers.length == 0) {
            return this.serialize(source);
        }

        return this.parse(source, resolvers);
    }

    /**
     * Parses markup without asking the cache, for callers that bring their own resolvers.
     *
     * @param source    the markup
     * @param resolvers the tag resolvers, may be empty
     * @return the component
     */
    public @NotNull Component parse(
            final @NotNull String source,
            final @NotNull TagResolver @NotNull ... resolvers
    ) {
        if (source.indexOf(TAG_START) == -1 || source.indexOf(TAG_END) == -1) {
            return Component.text(source);
        }

        try {
            return this.miniMessage.deserialize(source, resolvers);
        } catch (final RuntimeException ignored) {
            return Component.text(source);
        }
    }

    private @NotNull Component serializeUncached(final @NotNull String source) {
        return this.parse(source);
    }

}
