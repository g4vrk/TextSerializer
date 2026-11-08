package com.g4vrk.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

/**
 * One MiniMessage parser, so the library can be built against both the Adventure bundled with
 * Paper and the one bundled with the legacy line.
 *
 * <p>{@code MiniMessage::deserialize} matches this interface directly.</p>
 */
@FunctionalInterface
public interface MiniMessageAdapter {

    /**
     * Parses MiniMessage markup.
     *
     * @param input     the markup
     * @param resolvers the tag resolvers, may be empty
     * @return the component
     */
    @NotNull Component deserialize(final @NotNull String input, final @NotNull TagResolver @NotNull ... resolvers);

}
