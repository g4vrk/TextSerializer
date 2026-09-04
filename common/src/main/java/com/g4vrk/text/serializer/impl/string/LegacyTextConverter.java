package com.g4vrk.text.serializer.impl.string;

import com.g4vrk.text.serializer.impl.String2StringSerializer;
import com.g4vrk.text.util.MiniMessage2Legacy;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.AMPERSAND_CHAR;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.SECTION_CHAR;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.HEX_CHAR;

public final class LegacyTextConverter implements String2StringSerializer {

    private static final char MINI_MESSAGE_TAG_START = '<';

    private static final int CACHE_SIZE = 16_384;

    private static final Cache<String, String> CACHE =
            Caffeine.newBuilder()
                    .initialCapacity(4_096)
                    .maximumSize(CACHE_SIZE)
                    .build();

    @Override
    public @NotNull String serialize(
            final @NotNull String source
    ) {

        final int length = source.length();

        if (length < 2) {
            return source;
        }

        if (
                source.indexOf(AMPERSAND_CHAR) == -1 &&
                        source.indexOf(SECTION_CHAR) == -1 &&
                        source.indexOf(HEX_CHAR) == -1 &&
                        source.indexOf(MINI_MESSAGE_TAG_START) == -1
        ) {
            return source;
        }

        return CACHE.get(
                source,
                LegacyTextConverter::serializeUncached
        );

    }

    private static @NotNull String serializeUncached(
            @NotNull String source
    ) {

        if (
                source.indexOf(MINI_MESSAGE_TAG_START) != -1
        ) {
            source = MiniMessage2Legacy.convert(
                    source
            );
        }

        return normalizeLegacy(source);

    }

    /**
     * Normalizes recognized legacy codes without changing unrelated text.
     * Formatting sequences without any following text are preserved literally.
     *
     * @param source legacy or plain input
     * @return normalized legacy input
     */
    public static @NotNull String normalizeLegacy(
            @NotNull String source
    ) {

        final int length = source.length();

        if (length < 2) {
            return source;
        }

        int firstIndex = findFirstFormat(
                source,
                length
        );

        if (firstIndex == -1) {
            return source;
        }

        StringBuilder builder = null;

        int lastIndex = 0;

        for (
                int i = firstIndex;
                i < length;
                i++
        ) {

            final char current =
                    source.charAt(i);

            if (
                    current == HEX_CHAR &&
                    isHexColor(source, i + 1) &&
                    hasFormattingTarget(source, i + 7)
            ) {

                if (builder == null) {
                    builder = new StringBuilder(
                            length + 16
                    );
                }

                builder.append(
                        source,
                        lastIndex,
                        i
                );

                appendHex(
                        builder,
                        source,
                        i + 1
                );

                i += 6;
                lastIndex = i + 1;

                continue;
            }

            if (
                    current != AMPERSAND_CHAR &&
                    current != SECTION_CHAR
            ) {
                continue;
            }

            if (i + 1 >= length) {
                break;
            }

            final char code =
                    source.charAt(i + 1);

            if (
                    (
                            code == 'x' ||
                            code == 'X'
                    ) &&
                    isRepeatedHex(source, i) &&
                    !hasFormattingTarget(source, i + 14)
            ) {
                i += 13;
                continue;
            }

            if (
                    (
                            code == 'x' ||
                            code == 'X'
                    ) &&
                    !isRepeatedHex(source, i)
            ) {
                i = endOfPartialRepeatedHex(source, i, length) - 1;
                continue;
            }

            if (
                    code == HEX_CHAR &&
                    isHexColor(source, i + 2) &&
                    hasFormattingTarget(source, i + 8)
            ) {

                if (builder == null) {
                    builder = new StringBuilder(
                            length + 16
                    );
                }

                builder.append(
                        source,
                        lastIndex,
                        i
                );

                appendHex(
                        builder,
                        source,
                        i + 2
                );

                i += 7;
                lastIndex = i + 1;

                continue;
            }

            if (
                    (
                            code == 'x' ||
                            code == 'X'
                    ) &&
                    isRepeatedHex(source, i) &&
                    hasFormattingTarget(source, i + 14)
            ) {

                if (
                        current == SECTION_CHAR &&
                        code == 'x' &&
                        isNormalizedRepeatedHex(
                                source,
                                i
                        )
                ) {
                    i += 13;
                    continue;
                }

                if (builder == null) {
                    builder = new StringBuilder(
                            length + 16
                    );
                }

                builder.append(
                        source,
                        lastIndex,
                        i
                );

                appendRepeatedHex(
                        builder,
                        source,
                        i
                );

                i += 13;
                lastIndex = i + 1;

                continue;
            }

            if (
                    isLegacyCode(code) &&
                    hasFormattingTarget(source, i + 2)
            ) {

                final char normalizedCode =
                        toLowerCase(code);

                if (
                        current == SECTION_CHAR &&
                        code == normalizedCode
                ) {
                    continue;
                }

                if (builder == null) {
                    builder = new StringBuilder(
                            length + 8
                    );
                }

                builder.append(
                        source,
                        lastIndex,
                        i
                );

                builder.append(
                        SECTION_CHAR
                );

                builder.append(
                        normalizedCode
                );

                i++;

                lastIndex = i + 1;
            }

        }

        if (builder == null) {
            return source;
        }

        if (lastIndex < length) {
            builder.append(
                    source,
                    lastIndex,
                    length
            );
        }

        return builder.toString();

    }

    private static int findFirstFormat(
            @NotNull String source,
            int length
    ) {

        for (int i = 0; i < length; i++) {

            final char c =
                    source.charAt(i);

            if (
                    c == AMPERSAND_CHAR ||
                    c == SECTION_CHAR
            ) {
                if (
                        i + 2 < length &&
                        isLegacyCode(source.charAt(i + 1))
                ) {
                    return i;
                }

                if (
                        i + 2 < length &&
                        (
                                source.charAt(i + 1) == 'x' ||
                                source.charAt(i + 1) == 'X' ||
                                source.charAt(i + 1) == HEX_CHAR
                        )
                ) {
                    return i;
                }
            }

            if (
                    c == HEX_CHAR &&
                    i + 7 < length &&
                    isHexColor(
                            source,
                            i + 1
                    )
            ) {
                return i;
            }

        }

        return -1;

    }

    private static int endOfPartialRepeatedHex(
            @NotNull String source,
            int offset,
            int length
    ) {
        int index = offset + 2;
        int pairs = 0;

        while (
                pairs < 6 &&
                index + 1 < length &&
                (
                        source.charAt(index) == AMPERSAND_CHAR ||
                        source.charAt(index) == SECTION_CHAR
                ) &&
                isHex(source.charAt(index + 1))
        ) {
            index += 2;
            pairs++;
        }

        return index;
    }

    private static boolean hasFormattingTarget(
            @NotNull String source,
            int offset
    ) {
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

    private static int legacyTokenEnd(
            @NotNull String source,
            int offset
    ) {
        if (offset >= source.length()) {
            return -1;
        }

        if (source.charAt(offset) == HEX_CHAR && isHexColor(source, offset + 1)) {
            return offset + 7;
        }

        final char marker = source.charAt(offset);
        if (
                (marker != AMPERSAND_CHAR && marker != SECTION_CHAR) ||
                offset + 1 >= source.length()
        ) {
            return -1;
        }

        final char code = source.charAt(offset + 1);
        if (isLegacyCode(code)) {
            return offset + 2;
        }
        if (code == HEX_CHAR && isHexColor(source, offset + 2)) {
            return offset + 8;
        }
        if ((code == 'x' || code == 'X') && isRepeatedHex(source, offset)) {
            return offset + 14;
        }

        return -1;
    }

    private static void appendHex(
            @NotNull StringBuilder builder,
            @NotNull String source,
            int offset
    ) {

        builder.append(
                SECTION_CHAR
        );

        builder.append('x');

        for (int i = 0; i < 6; i++) {

            builder.append(
                    SECTION_CHAR
            );

            builder.append(
                    toUpperCase(
                            source.charAt(
                                    offset + i
                            )
                    )
            );

        }

    }

    private static void appendRepeatedHex(
            @NotNull StringBuilder builder,
            @NotNull String source,
            int offset
    ) {

        builder.append(
                SECTION_CHAR
        );

        builder.append('x');

        int index = offset + 2;

        for (int i = 0; i < 6; i++) {

            builder.append(
                    SECTION_CHAR
            );

            builder.append(
                    toUpperCase(
                            source.charAt(
                                    index + 1
                            )
                    )
            );

            index += 2;

        }

    }

    private static boolean isHexColor(
            @NotNull String source,
            int offset
    ) {

        if (
                offset + 6 >
                source.length()
        ) {
            return false;
        }

        for (int i = 0; i < 6; i++) {

            if (
                    !isHex(
                            source.charAt(
                                    offset + i
                            )
                    )
            ) {
                return false;
            }

        }

        return true;

    }

    private static boolean isRepeatedHex(
            @NotNull String source,
            int offset
    ) {

        if (
                offset + 14 >
                source.length()
        ) {
            return false;
        }

        int index = offset + 2;

        for (int i = 0; i < 6; i++) {

            final char prefix =
                    source.charAt(index);

            if (
                    prefix != AMPERSAND_CHAR &&
                    prefix != SECTION_CHAR
            ) {
                return false;
            }

            if (
                    !isHex(
                            source.charAt(
                                    index + 1
                            )
                    )
            ) {
                return false;
            }

            index += 2;

        }

        return true;

    }

    private static boolean isNormalizedRepeatedHex(
            @NotNull String source,
            int offset
    ) {

        int index = offset + 2;

        for (int i = 0; i < 6; i++) {

            if (
                    source.charAt(index) !=
                    SECTION_CHAR
            ) {
                return false;
            }

            final char value =
                    source.charAt(
                            index + 1
                    );

            if (
                    value !=
                    toUpperCase(value)
            ) {
                return false;
            }

            index += 2;

        }

        return true;

    }

    private static boolean isLegacyCode(
            char c
    ) {

        c = toLowerCase(c);

        return (
                c >= '0' &&
                c <= '9'
        ) || (
                c >= 'a' &&
                c <= 'f'
        ) || c == 'k'
                || c == 'l'
                || c == 'm'
                || c == 'n'
                || c == 'o'
                || c == 'r';

    }

    private static boolean isHex(
            char c
    ) {

        return (
                c >= '0' &&
                c <= '9'
        ) || (
                c >= 'a' &&
                c <= 'f'
        ) || (
                c >= 'A' &&
                c <= 'F'
        );

    }

    private static char toLowerCase(
            char c
    ) {

        if (
                c >= 'A' &&
                c <= 'Z'
        ) {
            return (char) (
                    c + ('a' - 'A')
            );
        }

        return c;

    }

    private static char toUpperCase(
            char c
    ) {

        if (
                c >= 'a' &&
                c <= 'f'
        ) {
            return (char) (
                    c - ('a' - 'A')
            );
        }

        return c;

    }

}
