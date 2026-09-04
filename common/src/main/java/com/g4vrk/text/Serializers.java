package com.g4vrk.text;

import com.g4vrk.text.serializer.impl.Component2StringSerializer;
import com.g4vrk.text.serializer.impl.String2ComponentSerializer;
import com.g4vrk.text.serializer.impl.String2StringSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Collection of serializers for MiniMessage, Minecraft legacy formatting,
 * and plain text.
 */
public interface Serializers {

    /** Parses MiniMessage, legacy formatting, or a mixture of both. */
    @NotNull String2ComponentSerializer universalSerializer();

    /** Parses MiniMessage markup. */
    @NotNull String2ComponentSerializer miniMessageSerializer();

    /** Converts recognized legacy codes to MiniMessage tags and preserves other text. */
    @NotNull String2StringSerializer miniMessageConverter();

    /** Parses {@code &x&r&r&g&g&b&b}, {@code &#rrggbb}, and regular legacy codes. */
    @NotNull String2ComponentSerializer legacySerializer();

    /** Converts MiniMessage and legacy input to normalized {@code §} formatting. */
    @NotNull String2StringSerializer legacyConverter();

    /** Removes component styling and returns plain text. */
    @NotNull Component2StringSerializer plainSerializer();

}
