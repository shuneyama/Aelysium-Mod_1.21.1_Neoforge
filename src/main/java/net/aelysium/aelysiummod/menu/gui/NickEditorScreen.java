package net.aelysium.aelysiummod.menu.gui;

import net.aelysium.aelysiummod.network.AelysiumNetwork;
import net.aelysium.aelysiummod.nickname.NicknameUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class NickEditorScreen extends Screen {

    private final UUID targetUUID;
    private final String realName;

    private EditBox prefixField;
    private EditBox prefixCor1Field;
    private EditBox prefixCor2Field;
    private int prefixFormat;

    private EditBox nickField;
    private EditBox nickCorField;
    private int nickFormat;

    private EditBox suffixField;
    private EditBox suffixCor1Field;
    private EditBox suffixCor2Field;
    private int suffixFormat;

    private final String initPrefix, initNick, initSuffix;
    private final int initPrefixCor1, initPrefixCor2, initPrefixFormat;
    private final int initNickCor, initNickFormat;
    private final int initSuffixCor1, initSuffixCor2, initSuffixFormat;

    public NickEditorScreen(UUID targetUUID, String realName,
                            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
                            String nick, int nickCor, int nickFormat,
                            String suffix, int suffixCor1, int suffixCor2, int suffixFormat) {
        super(Component.literal("Editor de Nickname"));
        this.targetUUID = targetUUID;
        this.realName = realName;
        this.initPrefix = prefix;
        this.initPrefixCor1 = prefixCor1;
        this.initPrefixCor2 = prefixCor2;
        this.initPrefixFormat = prefixFormat;
        this.initNick = nick;
        this.initNickCor = nickCor;
        this.initNickFormat = nickFormat;
        this.initSuffix = suffix;
        this.initSuffixCor1 = suffixCor1;
        this.initSuffixCor2 = suffixCor2;
        this.initSuffixFormat = suffixFormat;
        this.prefixFormat = prefixFormat;
        this.nickFormat = nickFormat;
        this.suffixFormat = suffixFormat;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;
        int fieldW = 100;
        int colorW = 60;

        int y = startY + 12;

        prefixField = new EditBox(this.font, centerX - 140, y, fieldW, 18, Component.literal("Prefixo"));
        prefixField.setMaxLength(16);
        prefixField.setValue(initPrefix);
        addRenderableWidget(prefixField);

        prefixCor1Field = new EditBox(this.font, centerX - 30, y, colorW, 18, Component.literal("Cor1"));
        prefixCor1Field.setMaxLength(7);
        prefixCor1Field.setValue(initPrefixCor1 >= 0 ? NicknameUtil.toHex(initPrefixCor1) : "#FFFFFF");
        addRenderableWidget(prefixCor1Field);

        prefixCor2Field = new EditBox(this.font, centerX + 40, y, colorW, 18, Component.literal("Cor2"));
        prefixCor2Field.setMaxLength(7);
        prefixCor2Field.setValue(initPrefixCor2 >= 0 ? NicknameUtil.toHex(initPrefixCor2) : "");
        addRenderableWidget(prefixCor2Field);

        addFormatButtons(centerX + 110, y, "prefix");

        y += 40;

        nickField = new EditBox(this.font, centerX - 140, y, fieldW, 18, Component.literal("Nick"));
        nickField.setMaxLength(24);
        nickField.setValue(initNick);
        addRenderableWidget(nickField);

        nickCorField = new EditBox(this.font, centerX - 30, y, colorW, 18, Component.literal("Cor"));
        nickCorField.setMaxLength(7);
        nickCorField.setValue(NicknameUtil.toHex(initNickCor));
        addRenderableWidget(nickCorField);

        addFormatButtons(centerX + 110, y, "nick");

        y += 40;

        suffixField = new EditBox(this.font, centerX - 140, y, fieldW, 18, Component.literal("Sufixo"));
        suffixField.setMaxLength(16);
        suffixField.setValue(initSuffix);
        addRenderableWidget(suffixField);

        suffixCor1Field = new EditBox(this.font, centerX + -30, y, colorW, 18, Component.literal("Cor1"));
        suffixCor1Field.setMaxLength(7);
        suffixCor1Field.setValue(initSuffixCor1 >= 0 ? NicknameUtil.toHex(initSuffixCor1) : "#FFFFFF");
        addRenderableWidget(suffixCor1Field);

        suffixCor2Field = new EditBox(this.font, centerX + 40, y, colorW, 18, Component.literal("Cor2"));
        suffixCor2Field.setMaxLength(7);
        suffixCor2Field.setValue(initSuffixCor2 >= 0 ? NicknameUtil.toHex(initSuffixCor2) : "");
        addRenderableWidget(suffixCor2Field);

        addFormatButtons(centerX + 110, y, "suffix");

        y += 60;

        addRenderableWidget(Button.builder(Component.literal("§aSalvar"), btn -> save())
                .bounds(centerX - 105, y, 100, 20).build());

        addRenderableWidget(Button.builder(Component.literal("§cCancelar"), btn -> onClose())
                .bounds(centerX + 5, y, 100, 20).build());
    }

    private void addFormatButtons(int x, int y, String target) {
        int btnSize = 18;
        int gap = 2;

        String[] labels = {"B", "I", "U", "S", "Z"};
        int[] flags = {NicknameUtil.BOLD, NicknameUtil.ITALIC, NicknameUtil.UNDERLINED, NicknameUtil.STRIKETHROUGH, NicknameUtil.OBFUSCATED};

        for (int i = 0; i < labels.length; i++) {
            int flag = flags[i];
            int bx = x + (btnSize + gap) * i;
            String label = labels[i];

            addRenderableWidget(Button.builder(Component.literal(getFormatLabel(target, flag, label)), btn -> {
                toggleFormat(target, flag);
                btn.setMessage(Component.literal(getFormatLabel(target, flag, label)));
            }).bounds(bx, y, btnSize, btnSize).build());
        }
    }

    private String getFormatLabel(String target, int flag, String label) {
        int currentFormat = switch (target) {
            case "prefix" -> prefixFormat;
            case "nick" -> nickFormat;
            case "suffix" -> suffixFormat;
            default -> 0;
        };
        boolean active = (currentFormat & flag) != 0;
        return active ? "§a" + label : "§7" + label;
    }

    private void toggleFormat(String target, int flag) {
        switch (target) {
            case "prefix" -> prefixFormat ^= flag;
            case "nick" -> nickFormat ^= flag;
            case "suffix" -> suffixFormat ^= flag;
        }
    }

    private int parseColorSafe(String hex, int fallback) {
        if (hex == null || hex.trim().isEmpty()) return -1;
        try {
            return NicknameUtil.parseHex(hex);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void save() {
        String prefix = prefixField.getValue().trim();
        int pCor1 = parseColorSafe(prefixCor1Field.getValue(), 0xFFFFFF);
        int pCor2 = parseColorSafe(prefixCor2Field.getValue(), -1);

        String nick = nickField.getValue().trim();
        int nCor = parseColorSafe(nickCorField.getValue(), 0xFFFFFF);
        if (nCor == -1) nCor = 0xFFFFFF;

        String suffix = suffixField.getValue().trim();
        int sCor1 = parseColorSafe(suffixCor1Field.getValue(), 0xFFFFFF);
        int sCor2 = parseColorSafe(suffixCor2Field.getValue(), -1);

        PacketDistributor.sendToServer(new AelysiumNetwork.NickEditorSavePacket(
                targetUUID,
                prefix, pCor1, pCor2, prefixFormat,
                nick, nCor, nickFormat,
                suffix, sCor1, sCor2, suffixFormat
        ));

        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = 30;

        // Título
        graphics.drawCenteredString(this.font, "§lEditor de Nickname", centerX, 10, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Jogador: §f" + realName, centerX, 20, 0xAAAAAA);

        // Labels
        graphics.drawString(this.font, "§ePrefixo:", centerX - 140, startY, 0xFFFFFF);
        graphics.drawString(this.font, "§eCor1:", centerX - 30, startY, 0xFFFFFF);
        graphics.drawString(this.font, "§eCor2:", centerX + 40, startY, 0xFFFFFF);
        graphics.drawString(this.font, "§eFormato:", centerX + 110, startY, 0xFFFFFF);

        int y2 = startY + 40;
        graphics.drawString(this.font, "§eNick:", centerX - 140, y2, 0xFFFFFF);
        graphics.drawString(this.font, "§eCor:", centerX - 30, y2, 0xFFFFFF);
        graphics.drawString(this.font, "§eFormato:", centerX + 110, y2, 0xFFFFFF);

        int y3 = startY + 80;
        graphics.drawString(this.font, "§eSufixo:", centerX - 140, y3, 0xFFFFFF);
        graphics.drawString(this.font, "§eCor1:", centerX - 30, y3, 0xFFFFFF);
        graphics.drawString(this.font, "§eCor2:", centerX + 40, y3, 0xFFFFFF);
        graphics.drawString(this.font, "§eFormato:", centerX + 110, y3, 0xFFFFFF);

        // Preview
        int previewY = startY + 130;
        graphics.drawCenteredString(this.font, "§7Preview:", centerX, previewY, 0xAAAAAA);

        MutableComponent preview = buildPreview();
        int previewWidth = this.font.width(preview);
        graphics.drawString(this.font, preview, centerX - previewWidth / 2, previewY + 12, 0xFFFFFF);
    }

    private MutableComponent buildPreview() {
        MutableComponent result = Component.empty();

        String prefix = prefixField.getValue().trim();
        String nick = nickField.getValue().trim();
        String suffix = suffixField.getValue().trim();

        if (!prefix.isEmpty()) {
            int c1 = parseColorSafe(prefixCor1Field.getValue(), 0xFFFFFF);
            if (c1 == -1) c1 = 0xFFFFFF;
            int c2 = parseColorSafe(prefixCor2Field.getValue(), -1);
            result.append(NicknameUtil.renderDualColor(prefix, c1, c2, prefixFormat));
            result.append(Component.literal(" "));
        }

        if (!nick.isEmpty()) {
            int c = parseColorSafe(nickCorField.getValue(), 0xFFFFFF);
            if (c == -1) c = 0xFFFFFF;
            result.append(NicknameUtil.renderSingleColor(nick, c, nickFormat));
        } else {
            result.append(Component.literal("???"));
        }

        if (!suffix.isEmpty()) {
            result.append(Component.literal(" "));
            int c1 = parseColorSafe(suffixCor1Field.getValue(), 0xFFFFFF);
            if (c1 == -1) c1 = 0xFFFFFF;
            int c2 = parseColorSafe(suffixCor2Field.getValue(), -1);
            result.append(NicknameUtil.renderDualColor(suffix, c1, c2, suffixFormat));
        }

        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}