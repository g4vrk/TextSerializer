package com.g4vrk.text;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/** Serializer entry point for Paper servers with native MiniMessage support. */
public final class NativeSerializers extends BaseSerializers {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_TEXT =
            PlainTextComponentSerializer.plainText();

    public static final Serializers INSTANCE = new NativeSerializers();

    private NativeSerializers() {
        super(MINI_MESSAGE::deserialize, PLAIN_TEXT::serialize);
    }
}
