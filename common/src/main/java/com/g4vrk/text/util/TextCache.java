package com.g4vrk.text.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;

/**
 * Builds the bounded caches of the serializers.
 *
 * <p>The bound is the total length of the cached sources, not their count, so a handful of huge
 * strings cannot pin megabytes of memory the way a plain entry limit would allow. Maintenance runs
 * on the calling thread: no task ever lands on the common fork-join pool of the server.</p>
 */
public final class TextCache {

    private static final long MAX_SOURCE_CHARS = 1L << 21;

    private TextCache() {
    }

    /**
     * Creates a cache keyed by the source text.
     *
     * @param <V> the cached value type
     * @return an empty, thread-safe cache
     */
    public static <V> @NotNull Cache<String, V> create() {
        return Caffeine.newBuilder()
                .maximumWeight(MAX_SOURCE_CHARS)
                .weigher((final String source, final V value) -> source.length())
                .executor(Runnable::run)
                .build();
    }

}
