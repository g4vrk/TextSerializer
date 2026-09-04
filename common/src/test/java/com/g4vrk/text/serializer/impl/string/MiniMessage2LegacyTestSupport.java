package com.g4vrk.text.serializer.impl.string;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

final class MiniMessage2LegacyTestSupport {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private MiniMessage2LegacyTestSupport() {
    }

    static String reference(final String input) {
        return LEGACY.serialize(MINI_MESSAGE.deserialize(input));
    }
}
