package com.g4vrk.text.engine;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class LegacyMiniMessageEngine {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final GsonComponentSerializer JSON = GsonComponentSerializer.gson();

    private LegacyMiniMessageEngine() {
    }

    public static @NotNull String deserialize(final @NotNull String source) {
        return JSON.serialize(MINI_MESSAGE.deserialize(source));
    }

    public static @NotNull String deserialize(
            final @NotNull String source,
            final @NotNull BiFunction<String, String, String> mapper,
            final @NotNull Predicate<String> owns
    ) {
        return JSON.serialize(MINI_MESSAGE.deserialize(source, new TagResolver() {

            @Override
            public @Nullable Tag resolve(
                    final @NotNull String name,
                    final @NotNull ArgumentQueue arguments,
                    final @NotNull Context context
            ) {
                if (!owns.test(name)) {
                    return null;
                }

                final String value = mapper.apply(name, join(arguments));

                return value == null ? null : Tag.preProcessParsed(value);
            }

            @Override
            public boolean has(final @NotNull String name) {
                return owns.test(name);
            }

        }));
    }

    private static @NotNull String join(final @NotNull ArgumentQueue arguments) {
        if (!arguments.hasNext()) {
            return "";
        }

        final String first = arguments.pop().value();

        if (!arguments.hasNext()) {
            return first;
        }

        final StringBuilder joined = new StringBuilder(first);
        do {
            joined.append(':').append(arguments.pop().value());
        } while (arguments.hasNext());

        return joined.toString();
    }

}
