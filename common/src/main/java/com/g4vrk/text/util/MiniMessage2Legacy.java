package com.g4vrk.text.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Converts MiniMessage markup to the normalized section-character legacy format.
 *
 * <p>Parsing is delegated to Adventure itself so escaping, nested styles,
 * gradients, and Unicode are handled exactly like regular MiniMessage input.</p>
 */
public final class MiniMessage2Legacy {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character(LegacyComponentSerializer.SECTION_CHAR)
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private MiniMessage2Legacy() {
    }

    /**
     * Converts the supplied MiniMessage string to legacy formatting.
     *
     * @param source MiniMessage input
     * @return normalized legacy string
     */
    public static @NotNull String convert(final @NotNull String source) {
        Objects.requireNonNull(source, "source");
        try {
            return LEGACY.serialize(MINI_MESSAGE.deserialize(source));
        } catch (final RuntimeException ignored) {
            return source;
        }
    }
}
