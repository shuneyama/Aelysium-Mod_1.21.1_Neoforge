package net.aelysium.aelysiummod.team;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FastColor;

public class CustomTimeCor {
    private final int rgb;
    private final String name;
    private final TextColor textColor;

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

    @Override
    public String toString() {
        return name + " (" + toHexString() + ")";
    }
}