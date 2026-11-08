package com.g4vrk.text.serializer.impl.string;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResolverTest {
    private final UniversalTextSerializer serializer = new UniversalTextSerializer(
            new MiniMessageTextSerializer(MiniMessage.miniMessage()::deserialize), new LegacyTextSerializer());
    private String plain(Component component) { return PlainTextComponentSerializer.plainText().serialize(component); }

    @Test void isolatesResolversAndDoesNotCachePlayerOutput() {
        assertEquals("Alice", plain(serializer.serialize("&c<name>&r", Placeholder.unparsed("name", "Alice"))));
        assertEquals("Bob", plain(serializer.serialize("&c<name>&r", Placeholder.unparsed("name", "Bob"))));
        assertEquals("<name>", plain(serializer.serialize("&c<name>&r")));
    }

    @Test void usesExplicitCustomParserForResolverCalls() {
        var mini = MiniMessage.builder().tags(net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.empty()).build();
        var custom = new MiniMessageTextSerializer(mini::deserialize, (source, resolvers) -> mini.deserialize(source, resolvers));
        assertEquals("<red>Alice", plain(custom.serialize("<red><name>", Placeholder.unparsed("name", "Alice"))));
        assertEquals("<red><name>", plain(custom.serialize("<red><name>")));
    }

    @Test void resetDoesNotAppearAsText() {
        for (String input : new String[]{"&r", "§r", "&R"}) assertEquals("", plain(serializer.serialize(input)));
        assertEquals("text", plain(serializer.serialize("&ctext&r")));
        assertEquals("text", plain(serializer.serialize("<red>text&r")));
    }

    @Test void preservesEscapesAndUnparsedResolverContent() {
        assertEquals("<red> <click:run_command:/op>", plain(serializer.serialize(
                "\\<red> <name>", Placeholder.unparsed("name", "<click:run_command:/op>"))));
        assertEquals("&R", plain(serializer.serialize("<name>", Placeholder.unparsed("name", "&R"))));
    }
}
