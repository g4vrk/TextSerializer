package com.g4vrk.text.serializer.impl.string;

import com.github.benmanes.caffeine.cache.Cache;
import com.g4vrk.text.util.TextCache;
import com.g4vrk.text.TagMapper;
import com.g4vrk.text.serializer.impl.String2ComponentSerializer;
import com.g4vrk.text.util.Legacy2MiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.AMPERSAND_CHAR;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.SECTION_CHAR;

/**
 * Parses MiniMessage, legacy formatting, or a mixture of both.
 *
 * <p>Legacy codes are turned into MiniMessage tags before parsing, which is also what makes tag
 * resolvers work with legacy input: {@code "&cHello <react_vl:aim>"} takes the same path as pure
 * MiniMessage. Text that only carries legacy codes is answered as a plain component and cached.</p>
 */
public final class UniversalTextSerializer implements String2ComponentSerializer {

    private static final char TAG_START = '<';
    private static final char TAG_END = '>';


    private final Cache<String, Component> cache = TextCache.create();

    private final MiniMessageTextSerializer miniMessage;

    public UniversalTextSerializer(final @NotNull MiniMessageTextSerializer miniMessage) {
        this.miniMessage = Objects.requireNonNull(miniMessage);
    }

    @Override
    public @NotNull Component serialize(final @NotNull String source) {
        if (source.isEmpty()) {
            return Component.empty();
        }

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

        return this.miniMessage.parse(Legacy2MiniMessage.toMarkup(source), resolvers);
    }

    @Override
    public @NotNull Component serialize(final @NotNull String source, final @NotNull TagMapper mapper) {
        return this.miniMessage.parse(
                Legacy2MiniMessage.toMarkup(source),
                mapper
        );
    }

    private @NotNull Component serializeUncached(final @NotNull String source) {
        final boolean miniMessage = source.indexOf(TAG_START) != -1 && source.indexOf(TAG_END) != -1;

        final boolean legacy = source.indexOf(AMPERSAND_CHAR) != -1 || source.indexOf(SECTION_CHAR) != -1;

        if (!miniMessage && !legacy) {
            return Component.text(source);
        }

        if (!legacy) {
            return this.miniMessage.parse(source);
        }

        return this.miniMessage.parse(Legacy2MiniMessage.toMarkup(source));
    }

}
