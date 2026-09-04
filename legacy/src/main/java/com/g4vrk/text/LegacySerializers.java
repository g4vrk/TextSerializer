package com.g4vrk.text;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

/** Serializer entry point for legacy Paper servers. */
@SuppressWarnings("deprecation")
public final class LegacySerializers extends BaseSerializers {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static final PlainComponentSerializer PLAIN_TEXT = PlainComponentSerializer.plain();

    public static final Serializers INSTANCE = new LegacySerializers();

    private LegacySerializers() {
        super(MINI_MESSAGE::deserialize, PLAIN_TEXT::serialize);
    }
}
