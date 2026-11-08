package com.g4vrk.text.serializer.impl;

import com.g4vrk.text.serializer.Serializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

/**
 * Turns a string into a component.
 *
 * <p>A serializer that parses MiniMessage also takes tag resolvers, which is how a placeholder
 * such as {@code <react_vl:aim>} is resolved. The default overload ignores them, so a serializer
 * that cannot resolve tags still satisfies the interface.</p>
 */
@FunctionalInterface
public interface String2ComponentSerializer extends Serializer<String, Component> {

    /**
     * Turns a string into a component, resolving its tags through the given resolvers.
     *
     * @param source    the text
     * @param resolvers the tag resolvers, may be empty
     * @return the component
     */
    default @NotNull Component serialize(
            final @NotNull String source,
            final @NotNull TagResolver @NotNull ... resolvers
    ) {
        return this.serialize(source);
    }

}
