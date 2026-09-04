package com.g4vrk.text.serializer;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Serializer<S, R> {

    /**
     * Serializes a non-null source value.
     *
     * @param source value to serialize
     * @return serialized result
     */
    @NotNull R serialize(
            final @NotNull S source
    );

}
