package com.g4vrk.text.engine;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/** An isolated parser whose public boundary contains only JDK types. */
public final class LegacyMiniMessageEngine {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final GsonComponentSerializer JSON = GsonComponentSerializer.gson();

    private LegacyMiniMessageEngine() {
    }

    public static String deserialize(final String source) {
        return JSON.serialize(MINI_MESSAGE.deserialize(source));
    }

    public static String deserialize(
            final String source,
            final BiFunction<String, String, String> mapper,
            final Predicate<String> owns
    ) {
        return JSON.serialize(MINI_MESSAGE.deserialize(source, new TagResolver() {

            @Override
            public Tag resolve(final String name, final ArgumentQueue arguments, final Context context) {
                if (!owns.test(name)) {
                    return null;
                }
                final String value = mapper.apply(name, join(arguments));
                return value == null ? null : Tag.preProcessParsed(value);
            }

            @Override
            public boolean has(final String name) {
                return owns.test(name);
            }

        }));
    }

    private static String join(final ArgumentQueue arguments) {
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
