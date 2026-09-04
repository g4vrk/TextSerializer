package com.g4vrk.text.serializer.impl.string;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiniMessageTextSerializerTest {

    @Test
    void bypassesDelegateWhenNoCompleteTagCanExist() {
        final AtomicInteger calls = new AtomicInteger();
        final MiniMessageTextSerializer serializer = new MiniMessageTextSerializer(input -> {
            calls.incrementAndGet();
            return Component.text("parsed");
        });

        assertEquals(Component.text("plain < text"), serializer.serialize("plain < text"));
        assertEquals(Component.text("plain > text"), serializer.serialize("plain > text"));
        assertEquals(0, calls.get());
    }

    @Test
    void cachesParsedInputWithinOneInstance() {
        final AtomicInteger calls = new AtomicInteger();
        final MiniMessageTextSerializer serializer = new MiniMessageTextSerializer(input -> {
            calls.incrementAndGet();
            return Component.text("parsed");
        });

        serializer.serialize("<tag>");
        serializer.serialize("<tag>");

        assertEquals(1, calls.get());
    }
}
