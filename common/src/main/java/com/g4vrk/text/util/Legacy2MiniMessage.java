package com.g4vrk.text.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Converts recognized Minecraft legacy codes to canonical MiniMessage markup. */
public final class Legacy2MiniMessage {

    private static final char AMPERSAND = '&';
    private static final char SECTION = '§';
    private static final boolean MODERN_ADVENTURE = modernAdventure();

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

        if (!conversion.markup() || !MODERN_ADVENTURE) {
            return conversion.markupText();
        }

        try {
            return Canonicalizer.MINI_MESSAGE.serialize(Canonicalizer.MINI_MESSAGE.deserialize(conversion.markupText()));
        } catch (final RuntimeException ignored) {
            return conversion.markupText();
        }
    }

    /**
     * Converts the legacy codes of a text and leaves everything else, including MiniMessage
     * markup the caller still has to resolve, exactly as it is.
     *
     * @param source legacy, MiniMessage or mixed text
     * @return markup with the legacy codes replaced by tags
     */
    public static @NotNull String toMarkup(final @NotNull String source) {
        Objects.requireNonNull(source, "source");

        final Conversion conversion = convertCodes(source);

        return conversion.changed() ? conversion.markupText() : source;
    }

    private static boolean modernAdventure() {
        try {
            Component.class.getMethod("compact");
            return true;
        } catch (final NoSuchMethodException absent) {
            return false;
        }
    }

    private static final class Canonicalizer {

        private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    }

    private static Conversion convertCodes(final String source) {
        final StringBuilder output = new StringBuilder(source.length() + 32);
        final LegacyState state = new LegacyState();
        boolean changed = false;
        boolean markup = false;
        int index = 0;
        int rawDepth = 0;

        while (index < source.length()) {
            final char current = source.charAt(index);

            if (current == '<' && startsTag(source, index) && !isEscaped(source, index)) {
                final int tagEnd = findTagEnd(source, index + 1);
                if (tagEnd != -1) {
                    markup = true;
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

            final String color = colorName(code);

            if (color != null) {
                state.replaceColor(output, color, rawDepth);
                changed = true;
                index += 2;
                continue;
            }

            final String decoration = decorationName(code);

            if (decoration != null) {
                state.addDecoration(output, code, decoration, rawDepth);
                changed = true;
                index += 2;
                continue;
            }

            if (code == 'r') {
                state.close(output, true);
                output.append("<reset>");
                changed = true;
                index += 2;
                continue;
            }

            if (code == '#' && isHex(source, index + 2)) {
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
                if (repeated.complete()) {
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
        return new Conversion(output.toString(), changed, markup);
    }

    private static String colorName(final char code) {
        return switch (code) {
            case '0' -> "black";
            case '1' -> "dark_blue";
            case '2' -> "dark_green";
            case '3' -> "dark_aqua";
            case '4' -> "dark_red";
            case '5' -> "dark_purple";
            case '6' -> "gold";
            case '7' -> "gray";
            case '8' -> "dark_gray";
            case '9' -> "blue";
            case 'a' -> "green";
            case 'b' -> "aqua";
            case 'c' -> "red";
            case 'd' -> "light_purple";
            case 'e' -> "yellow";
            case 'f' -> "white";
            default -> null;
        };
    }

    private static String decorationName(final char code) {
        return switch (code) {
            case 'k' -> "obfuscated";
            case 'l' -> "bold";
            case 'm' -> "strikethrough";
            case 'n' -> "underlined";
            case 'o' -> "italic";
            default -> null;
        };
    }

    private static boolean isHexDigit(final char value) {
        return (value >= '0' && value <= '9')
                || (value >= 'a' && value <= 'f')
                || (value >= 'A' && value <= 'F');
    }

    private static boolean isMarker(final char value) {
        return value == AMPERSAND || value == SECTION;
    }

    private static boolean isHex(final String source, final int offset) {
        if (offset + 6 > source.length()) {
            return false;
        }
        for (int index = offset; index < offset + 6; index++) {
            if (!isHexDigit(source.charAt(index))) {
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
                isHexDigit(source.charAt(index + 1))
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

    private static boolean startsTag(final String source, final int index) {
        return index + 1 < source.length() && !Character.isWhitespace(source.charAt(index + 1));
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

    private record Conversion(String markupText, boolean changed, boolean markup) {
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
