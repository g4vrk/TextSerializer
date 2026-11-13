package com.g4vrk.text.serializer.impl.component;

import com.g4vrk.text.serializer.impl.Component2StringSerializer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Flattens a component to its plain text.
 *
 * <p>Not cached on purpose: a component key has to be hashed and compared in full on every lookup,
 * which costs as much as walking the component once to flatten it.</p>
 */
public final class PlainTextSerializer implements Component2StringSerializer {

    private final Function<Component, String> plainText;

    public PlainTextSerializer(final @NotNull Function<Component, String> plainText) {
        this.plainText = Objects.requireNonNull(plainText);
    }

    @Override
    public @NotNull String serialize(final @NotNull Component source) {
        return this.plainText.apply(source);
    }

}
