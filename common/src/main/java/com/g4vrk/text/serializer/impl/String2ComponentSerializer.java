package com.g4vrk.text.serializer.impl;

import com.g4vrk.text.serializer.Serializer;
import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface String2ComponentSerializer extends Serializer<String, Component> {
    /**
     * Parses with per-call resolvers. Implementations without MiniMessage support reject
     * nonempty resolver sets. Resolved output must never enter a source-only cache.
     */
    default Component serialize(String source,
            net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers) {
        if (resolvers.length == 0) return serialize(source);
        throw new UnsupportedOperationException("This serializer does not support tag resolvers");
    }
}
