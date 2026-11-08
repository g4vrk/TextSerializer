package com.g4vrk.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Answers the value of one MiniMessage tag as markup.
 *
 * <p>This is the resolver path that works the same in both distribution flavours: the value
 * crosses the library boundary as a plain {@link String}, so it does not matter whether the
 * MiniMessage that parses it is the one of the server or the one the legacy flavour bundles under
 * its own package. A resolver handed to MiniMessage directly would not survive that relocation.</p>
 *
 * <p>A mapper only answers tags it owns: {@link #has(String)} keeps {@code <red>} and every other
 * standard tag out of the way.</p>
 */
@FunctionalInterface
public interface TagMapper {

    /**
     * Gives the markup of one tag.
     *
     * @param name the tag name, for example {@code react_vl}
     * @param args   what followed the name, joined with {@code :}, empty when nothing did
     * @return the markup the tag stands for, or {@code null} to leave the tag as it is
     */
    @Nullable String map(final @NotNull String name, final @NotNull String args);

    /**
     * Tells whether this mapper owns a tag.
     *
     * <p>A mapper that answers {@code true} is asked for the value of the tag. Answer
     * {@code false} for everything the mapper does not know, so the standard tags of MiniMessage
     * keep working.</p>
     *
     * @param name the tag name
     * @return {@code true} when {@link #map(String, String)} can answer this tag
     */
    default boolean has(final @NotNull String name) {
        return true;
    }

}
