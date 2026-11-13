package com.g4vrk.text.serializer.impl.string;

import com.github.benmanes.caffeine.cache.Cache;
import com.g4vrk.text.util.TextCache;
import com.g4vrk.text.TagMapper;
import com.g4vrk.text.serializer.impl.String2ComponentSerializer;
import com.g4vrk.text.util.Legacy2MiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Parses legacy formatting: {@code §c}, {@code &c}, {@code &#rrggbb} and
 * {@code &x&r&r&g&g&b&b}.
 *
 * <p>Without resolvers the text goes through Adventure's legacy serializer, which is the fastest
 * path and resolves hex colours exactly like the server does. With resolvers the codes are turned
 * into MiniMessage tags first, so a tag resolver works alongside them.</p>
 */
public final class LegacyTextSerializer implements String2ComponentSerializer {


    public static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character(LegacyComponentSerializer.SECTION_CHAR)
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private final Cache<String, Component> cache = TextCache.create();

    private final MiniMessageTextSerializer miniMessage;

    public LegacyTextSerializer(final @NotNull MiniMessageTextSerializer miniMessage) {
        this.miniMessage = miniMessage;
    }

    @Override
    public @NotNull Component serialize(final @NotNull String source) {
        return this.cache.get(source, LegacyTextSerializer::serializeUncached);
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

    private static @NotNull Component serializeUncached(final @NotNull String source) {
        final String normalized = LegacyTextConverter.normalizeLegacy(source);

        if (normalized.indexOf(LegacyComponentSerializer.SECTION_CHAR) == -1) {
            return Component.text(source);
        }

        return LEGACY_SERIALIZER.deserialize(normalized);
    }

}
