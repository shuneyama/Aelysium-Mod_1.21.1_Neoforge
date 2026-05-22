package net.aelysium.aelysiummod.nickname;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class NicknameUtil {

    public static final int BOLD         = 1;
    public static final int ITALIC       = 2;
    public static final int UNDERLINED   = 4;
    public static final int STRIKETHROUGH = 8;
    public static final int OBFUSCATED   = 16;

    public static int parseHex(String hex) {
        hex = hex.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() != 6) throw new IllegalArgumentException("Cor hex inválida: " + hex);
        return Integer.parseInt(hex, 16);
    }

    public static String toHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    public static Style applyFormat(Style style, int formatFlags) {
        if ((formatFlags & BOLD) != 0)          style = style.withBold(true);
        if ((formatFlags & ITALIC) != 0)        style = style.withItalic(true);
        if ((formatFlags & UNDERLINED) != 0)    style = style.withUnderlined(true);
        if ((formatFlags & STRIKETHROUGH) != 0) style = style.withStrikethrough(true);
        if ((formatFlags & OBFUSCATED) != 0)    style = style.withObfuscated(true);
        return style;
    }

    public static MutableComponent renderDualColor(String text, int cor1, int cor2, int formatFlags) {
        if (text == null || text.isEmpty()) return Component.empty();

        Style baseStyle = applyFormat(Style.EMPTY, formatFlags);

        if (text.length() == 1 || cor2 == -1) {
            return Component.literal(text).withStyle(baseStyle.withColor(TextColor.fromRgb(cor1)));
        }

        MutableComponent result = Component.empty();

        result.append(Component.literal(String.valueOf(text.charAt(0)))
                .withStyle(baseStyle.withColor(TextColor.fromRgb(cor1))));

        if (text.length() > 2) {
            result.append(Component.literal(text.substring(1, text.length() - 1))
                    .withStyle(baseStyle.withColor(TextColor.fromRgb(cor2))));
        }

        result.append(Component.literal(String.valueOf(text.charAt(text.length() - 1)))
                .withStyle(baseStyle.withColor(TextColor.fromRgb(cor1))));

        return result;
    }

    public static MutableComponent renderSingleColor(String text, int cor, int formatFlags) {
        if (text == null || text.isEmpty()) return Component.empty();
        Style style = applyFormat(Style.EMPTY.withColor(TextColor.fromRgb(cor)), formatFlags);
        return Component.literal(text).withStyle(style);
    }
}