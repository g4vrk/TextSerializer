package com.g4vrk.text.serializer.impl.string;

import com.g4vrk.text.serializer.impl.component.PlainTextSerializer;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerializerCacheIsolationTest {

    @Test
    void miniMessageCacheBelongsToItsSerializerInstance() {
        final MiniMessageTextSerializer first =
                new MiniMessageTextSerializer(ignored -> Component.text("first"));
        final MiniMessageTextSerializer second =
                new MiniMessageTextSerializer(ignored -> Component.text("second"));

        first.serialize("<same>");

        assertEquals(Component.text("second"), second.serialize("<same>"));
    }

    @Test
    void plainTextCacheBelongsToItsSerializerInstance() {
        final Component key = Component.text("same");
        final PlainTextSerializer first = new PlainTextSerializer(ignored -> "first");
        final PlainTextSerializer second = new PlainTextSerializer(ignored -> "second");

        first.serialize(key);

        assertEquals("second", second.serialize(key));
    }

    @Test
    void cachesAreSafeUnderConcurrentUse() {
        final MiniMessageTextSerializer serializer =
                new MiniMessageTextSerializer(Component::text);

        IntStream.range(0, 2_000)
                .parallel()
                .forEach(index -> {
                    final String input = "<value-" + (index % 64) + ">";
                    assertEquals(Component.text(input), serializer.serialize(input));
                });
    }
}
