package com.g4vrk.text.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Converts recognized Minecraft legacy codes to canonical MiniMessage markup. */
public final class Legacy2MiniMessage {

    private static final char AMPERSAND = '&';
    private static final char SECTION = '§';
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    private static final Map<Character, String> COLORS = Map.ofEntries(
            Map.entry('0', "black"),
            Map.entry('1', "dark_blue"),
            Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"),
            Map.entry('4', "dark_red"),
            Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"),
            Map.entry('7', "gray"),
            Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"),
            Map.entry('a', "green"),
            Map.entry('b', "aqua"),
            Map.entry('c', "red"),
            Map.entry('d', "light_purple"),
            Map.entry('e', "yellow"),
            Map.entry('f', "white")
    );

    private static final Map<Character, String> DECORATIONS = Map.of(
            'k', "obfuscated",
            'l', "bold",
            'm', "strikethrough",
            'n', "underlined",
            'o', "italic"
    );

    private Legacy2MiniMessage() {
    }

    /**
     * Converts recognized legacy formatting and preserves all other text.
     * Existing MiniMessage tags are kept intact, so mixed input is supported.
     *
     * @param source legacy, MiniMessage, or mixed text
     * @return canonical MiniMessage markup
     */
    public static @NotNull String convert(final @NotNull String source) {
        Objects.requireNonNull(source, "source");

        final Conversion conversion = convertCodes(source);
        if (!conversion.changed()) {
            return source;
        }

        try {
            return MINI_MESSAGE.serialize(MINI_MESSAGE.deserialize(conversion.markup()));
        } catch (final RuntimeException ignored) {
            return conversion.markup();
        }
    }

    private static Conversion convertCodes(final String source) {
        final StringBuilder output = new StringBuilder(source.length() + 32);
        final LegacyState state = new LegacyState();
        boolean changed = false;
        int index = 0;
        int rawDepth = 0;

        while (index < source.length()) {
            final char current = source.charAt(index);

            if (current == '<' && !isEscaped(source, index)) {
                final int tagEnd = findTagEnd(source, index + 1);
                if (tagEnd != -1) {
                    final String tag = source.substring(index, tagEnd + 1);
                    if (isResetTag(tag)) {
                        state.close(output, true);
                    } else if (isClosingTag(tag)) {
                        final List<ActiveTag> moved = state.closeInside(output, rawDepth);
                        output.append(tag);
                        rawDepth = Math.max(0, rawDepth - 1);
                        state.reopen(output, moved, rawDepth);
                        index = tagEnd + 1;
                        continue;
                    }

                    output.append(tag);
                    if (!isSelfClosingTag(tag)) {
                        rawDepth++;
                    }
                    index = tagEnd + 1;
                    continue;
                }
            }

            if (!isMarker(current) || index + 1 >= source.length()) {
                output.append(current);
                index++;
                continue;
            }

            final char rawCode = source.charAt(index + 1);
            final char code = Character.toLowerCase(rawCode);

            if (COLORS.containsKey(code) && hasFormattingTarget(source, index + 2)) {
                state.replaceColor(output, COLORS.get(code), rawDepth);
                changed = true;
                index += 2;
                continue;
            }

            if (DECORATIONS.containsKey(code) && hasFormattingTarget(source, index + 2)) {
                state.addDecoration(output, code, DECORATIONS.get(code), rawDepth);
                changed = true;
                index += 2;
                continue;
            }

            if (code == 'r' && hasFormattingTarget(source, index + 2)) {
                state.close(output, true);
                output.append("<reset>");
                changed = true;
                index += 2;
                continue;
            }

            if (code == '#' && isHex(source, index + 2)
                    && hasFormattingTarget(source, index + 8)) {
                state.replaceColor(
                        output,
                        "#" + source.substring(index + 2, index + 8),
                        rawDepth
                );
                changed = true;
                index += 8;
                continue;
            }

            if (code == 'x') {
                final RepeatedHex repeated = repeatedHex(source, index);
                if (repeated.complete()
                        && hasFormattingTarget(source, repeated.endIndex())) {
                    state.replaceColor(output, "#" + repeated.color(), rawDepth);
                    changed = true;
                    index = repeated.endIndex();
                    continue;
                }

                output.append(source, index, repeated.endIndex());
                index = repeated.endIndex();
                continue;
            }

            output.append(current);
            index++;
        }

        state.close(output, true);
        return new Conversion(output.toString(), changed);
    }

    private static boolean hasFormattingTarget(final String source, final int offset) {
        int index = offset;
        while (index < source.length()) {
            final int tokenEnd = legacyTokenEnd(source, index);
            if (tokenEnd == -1) {
                return true;
            }
            index = tokenEnd;
        }
        return false;
    }

    private static int legacyTokenEnd(final String source, final int offset) {
        if (offset + 1 >= source.length() || !isMarker(source.charAt(offset))) {
            return -1;
        }

        final char code = Character.toLowerCase(source.charAt(offset + 1));
        if (COLORS.containsKey(code) || DECORATIONS.containsKey(code) || code == 'r') {
            return offset + 2;
        }
        if (code == '#' && isHex(source, offset + 2)) {
            return offset + 8;
        }
        if (code == 'x') {
            final RepeatedHex repeated = repeatedHex(source, offset);
            if (repeated.complete()) {
                return repeated.endIndex();
            }
        }
        return -1;
    }

    private static boolean isMarker(final char value) {
        return value == AMPERSAND || value == SECTION;
    }

    private static boolean isHex(final String source, final int offset) {
        if (offset + 6 > source.length()) {
            return false;
        }
        for (int index = offset; index < offset + 6; index++) {
            if (Character.digit(source.charAt(index), 16) == -1) {
                return false;
            }
        }
        return true;
    }

    private static RepeatedHex repeatedHex(final String source, final int markerIndex) {
        final StringBuilder color = new StringBuilder(6);
        int index = markerIndex + 2;

        while (
                color.length() < 6 &&
                index + 1 < source.length() &&
                isMarker(source.charAt(index)) &&
                Character.digit(source.charAt(index + 1), 16) != -1
        ) {
            color.append(source.charAt(index + 1));
            index += 2;
        }

        return new RepeatedHex(color.toString(), index, color.length() == 6);
    }

    private static int findTagEnd(final String source, final int offset) {
        char quote = 0;
        boolean escaped = false;

        for (int index = offset; index < source.length(); index++) {
            final char current = source.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (quote != 0) {
                if (current == quote) {
                    quote = 0;
                }
                continue;
            }
            if (current == '\'' || current == '"') {
                quote = current;
                continue;
            }
            if (current == '>') {
                return index;
            }
        }

        return -1;
    }

    private static boolean isEscaped(final String source, final int index) {
        int backslashes = 0;
        for (int cursor = index - 1; cursor >= 0 && source.charAt(cursor) == '\\'; cursor--) {
            backslashes++;
        }
        return (backslashes & 1) == 1;
    }

    private static boolean isClosingTag(final String tag) {
        return tag.length() > 3 && tag.charAt(1) == '/';
    }

    private static boolean isResetTag(final String tag) {
        return "<reset>".equalsIgnoreCase(tag) || "<r>".equalsIgnoreCase(tag);
    }

    private static boolean isSelfClosingTag(final String tag) {
        final int separator = tag.indexOf(':');
        final int end = separator == -1 ? tag.length() - 1 : separator;
        final String name = tag.substring(1, end);
        return name.equalsIgnoreCase("newline")
                || name.equalsIgnoreCase("br")
                || name.equalsIgnoreCase("reset")
                || name.equalsIgnoreCase("r");
    }

    private record Conversion(String markup, boolean changed) {
    }

    private record RepeatedHex(String color, int endIndex, boolean complete) {
    }

    private record ActiveTag(char code, String name, int depth) {
    }

    private static final class LegacyState {

        private final List<ActiveTag> tags = new ArrayList<>();

        private void replaceColor(
                final StringBuilder output,
                final String nextColor,
                final int depth
        ) {
            close(output, true);
            output.append('<').append(nextColor).append('>');
            this.tags.add(new ActiveTag('c', nextColor, depth));
        }

        private void addDecoration(
                final StringBuilder output,
                final char code,
                final String decoration,
                final int depth
        ) {
            for (final ActiveTag tag : this.tags) {
                if (tag.code() == code) {
                    return;
                }
            }
            output.append('<').append(decoration).append('>');
            this.tags.add(new ActiveTag(code, decoration, depth));
        }

        private void close(final StringBuilder output, final boolean clear) {
            for (int index = this.tags.size() - 1; index >= 0; index--) {
                output.append("</").append(this.tags.get(index).name()).append('>');
            }
            if (clear) {
                this.tags.clear();
            }
        }

        private List<ActiveTag> closeInside(
                final StringBuilder output,
                final int depth
        ) {
            final List<ActiveTag> moved = new ArrayList<>();
            while (
                    !this.tags.isEmpty()
                            && this.tags.get(this.tags.size() - 1).depth() >= depth
            ) {
                final ActiveTag tag = this.tags.remove(this.tags.size() - 1);
                output.append("</").append(tag.name()).append('>');
                moved.add(0, tag);
            }

            return moved;
        }

        private void reopen(
                final StringBuilder output,
                final List<ActiveTag> moved,
                final int depth
        ) {
            for (final ActiveTag tag : moved) {
                output.append('<').append(tag.name()).append('>');
                this.tags.add(new ActiveTag(tag.code(), tag.name(), depth));
            }
        }
    }
}
