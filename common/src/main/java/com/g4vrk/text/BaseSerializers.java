package com.g4vrk.text;

import com.g4vrk.text.serializer.impl.Component2StringSerializer;
import com.g4vrk.text.serializer.impl.String2ComponentSerializer;
import com.g4vrk.text.serializer.impl.String2StringSerializer;
import com.g4vrk.text.serializer.impl.component.PlainTextSerializer;
import com.g4vrk.text.serializer.impl.string.LegacyTextConverter;
import com.g4vrk.text.serializer.impl.string.LegacyTextSerializer;
import com.g4vrk.text.serializer.impl.string.MiniMessageTextConverter;
import com.g4vrk.text.serializer.impl.string.MiniMessageTextSerializer;
import com.g4vrk.text.serializer.impl.string.UniversalTextSerializer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/** Shared, immutable serializer setup used by both distribution variants. */
public abstract class BaseSerializers implements Serializers {

    private final LegacyTextConverter legacyConverter = new LegacyTextConverter();
    private final LegacyTextSerializer legacySerializer = new LegacyTextSerializer();
    private final MiniMessageTextConverter miniMessageConverter = new MiniMessageTextConverter();
    private final MiniMessageTextSerializer miniMessageSerializer;
    private final PlainTextSerializer plainSerializer;
    private final UniversalTextSerializer universalSerializer;

    protected BaseSerializers(
            final @NotNull Function<String, Component> miniMessage,
            final @NotNull Function<Component, String> plainText
    ) {
        this.miniMessageSerializer = new MiniMessageTextSerializer(
                Objects.requireNonNull(miniMessage, "miniMessage")
        );
        this.plainSerializer = new PlainTextSerializer(
                Objects.requireNonNull(plainText, "plainText")
        );
        this.universalSerializer = new UniversalTextSerializer(
                this.miniMessageSerializer,
                this.legacySerializer
        );
    }

    @Override
    public final @NotNull String2ComponentSerializer universalSerializer() {
        return this.universalSerializer;
    }

    @Override
    public final @NotNull String2ComponentSerializer miniMessageSerializer() {
        return this.miniMessageSerializer;
    }

    @Override
    public final @NotNull String2StringSerializer miniMessageConverter() {
        return this.miniMessageConverter;
    }

    @Override
    public final @NotNull String2ComponentSerializer legacySerializer() {
        return this.legacySerializer;
    }

    @Override
    public final @NotNull String2StringSerializer legacyConverter() {
        return this.legacyConverter;
    }

    @Override
    public final @NotNull Component2StringSerializer plainSerializer() {
        return this.plainSerializer;
    }
}
