package net.aelysium.aelysiummod.team;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FastColor;

public class CustomTimeCor {
    private final int rgb;
    private final String name;
    private final TextColor textColor;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underlined = false;
    private boolean strikethrough = false;
    private boolean obfuscated = false; // Zalgo/ofuscado

    public CustomTimeCor(String name, int red, int green, int blue) {
        this.name = name;
        this.rgb = FastColor.ARGB32.color(255, red, green, blue);
        this.textColor = TextColor.fromRgb(this.rgb);
    }

    public CustomTimeCor(String name, int rgb) {
        this.name = name;
        this.rgb = rgb | 0xFF000000;
        this.textColor = TextColor.fromRgb(this.rgb);
    }

    public int getRgb() {
        return rgb;
    }

    public int getRed() {
        return FastColor.ARGB32.red(rgb);
    }

    public int getGreen() {
        return FastColor.ARGB32.green(rgb);
    }

    public int getBlue() {
        return FastColor.ARGB32.blue(rgb);
    }

    public String getName() {
        return name;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public int getColorValue() {
        return rgb & 0xFFFFFF;
    }

    public String toHexString() {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    public static CustomTimeCor fromHex(String name, String hex) {
        hex = hex.replace("#", "");
        int rgb = Integer.parseInt(hex, 16);
        return new CustomTimeCor(name, rgb);
    }

    // ===== MÉTODOS DE FORMATAÇÃO =====

    public CustomTimeCor setBold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public CustomTimeCor setItalic(boolean italic) {
        this.italic = italic;
        return this;
    }

    public CustomTimeCor setUnderlined(boolean underlined) {
        this.underlined = underlined;
        return this;
    }

    public CustomTimeCor setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public CustomTimeCor setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
        return this;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isUnderlined() {
        return underlined;
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }

    public boolean isObfuscated() {
        return obfuscated;
    }

    /**
     * Retorna um Style com todas as formatações aplicadas
     */
    public Style getStyle() {
        return Style.EMPTY
                .withColor(textColor)
                .withBold(bold)
                .withItalic(italic)
                .withUnderlined(underlined)
                .withStrikethrough(strikethrough)
                .withObfuscated(obfuscated);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name + " (" + toHexString());
        if (bold) sb.append(" Negrito");
        if (italic) sb.append(" Itálico");
        if (underlined) sb.append(" Sublinhado");
        if (strikethrough) sb.append(" Riscado");
        if (obfuscated) sb.append(" Ofuscado");
        sb.append(")");
        return sb.toString();
    }
}