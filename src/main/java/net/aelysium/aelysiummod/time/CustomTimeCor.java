package net.aelysium.aelysiummod.time;

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
    private boolean obfuscated = false;

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
        return this.rgb;
    }

    public int getRed() {
        return FastColor.ARGB32.red(this.rgb);
    }

    public int getGreen() {
        return FastColor.ARGB32.green(this.rgb);
    }

    public int getBlue() {
        return FastColor.ARGB32.blue(this.rgb);
    }

    public String getName() {
        return this.name;
    }

    public TextColor getTextColor() {
        return this.textColor;
    }

    public int getColorValue() {
        return this.rgb & 0xFFFFFF;
    }

    public String toHexString() {
        return String.format("#%06X", this.rgb & 0xFFFFFF);
    }

    public static CustomTimeCor fromHex(String name, String hex) {
        hex = hex.replace("#", "");
        int rgb = Integer.parseInt(hex, 16);
        return new CustomTimeCor(name, rgb);
    }

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
        return this.bold;
    }

    public boolean isItalic() {
        return this.italic;
    }

    public boolean isUnderlined() {
        return this.underlined;
    }

    public boolean isStrikethrough() {
        return this.strikethrough;
    }

    public boolean isObfuscated() {
        return this.obfuscated;
    }

    public Style getStyle() {
        return Style.EMPTY
                .withColor(this.textColor)
                .withBold(this.bold)
                .withItalic(this.italic)
                .withUnderlined(this.underlined)
                .withStrikethrough(this.strikethrough)
                .withObfuscated(this.obfuscated);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.name + " (" + toHexString());
        if (this.bold) sb.append(" Negrito");
        if (this.italic) sb.append(" Itálico");
        if (this.underlined) sb.append(" Sublinhado");
        if (this.strikethrough) sb.append(" Riscado");
        if (this.obfuscated) sb.append(" Ofuscado");
        sb.append(")");
        return sb.toString();
    }
}