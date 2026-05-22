package net.aelysium.aelysiummod.menu.gui;

import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.npc.entity.NpcTradeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class NpcSettingsTab extends NpcEditorScreen {

    private EditBox nameField;
    private EditBox lookRadiusField;

    private int configNameFieldX,        configNameFieldY,        configNameFieldW,        configNameFieldH;
    private int configNameVisibleBtnX,   configNameVisibleBtnY,   configNameVisibleBtnW,   configNameVisibleBtnH;
    private int configLookBtnX,          configLookBtnY,          configLookBtnW,          configLookBtnH;
    private int configLookRadiusFieldX,  configLookRadiusFieldY,  configLookRadiusFieldW,  configLookRadiusFieldH;
    private int configGravityBtnX,       configGravityBtnY,       configGravityBtnW,       configGravityBtnH;
    private int configSaveBtnX,          configSaveBtnY,          configSaveBtnW,          configSaveBtnH;
    private int configLoadBtnX,          configLoadBtnY,          configLoadBtnW,          configLoadBtnH;
    private int configRemoveBtnX,        configRemoveBtnY,        configRemoveBtnW,        configRemoveBtnH;

    public NpcSettingsTab(CustomNpcEntity npc, CompoundTag npcData, NpcEditorScreen origin) {
        super(npc, npcData);
        if (origin != null) origin.copyStateTo(this);
        this.currentTab = 2;
    }

    @Override
    protected void initCurrentTab() {
        int btnSaveW   = sw(64);
        int btnLoadW   = sw(80);
        int btnRemoveW = sw(74);
        int totalW     = btnSaveW + btnLoadW + btnRemoveW;
        int gap        = (sw(253) - totalW) / 4;

        int y = sy(35);
        int nomeStartX = sx(0) + gap;
        int nomeEndX   = sx(0) + gap + btnSaveW + gap + btnLoadW + gap + btnRemoveW;
        int nomeTotalW = nomeEndX - nomeStartX;

        configNameVisibleBtnW = sw(14); configNameVisibleBtnH = sw(14);
        configNameVisibleBtnX = nomeEndX - configNameVisibleBtnW;
        configNameVisibleBtnY = y;

        configNameFieldX = nomeStartX;
        configNameFieldY = y;
        configNameFieldW = nomeTotalW - configNameVisibleBtnW - sw(4);
        configNameFieldH = sw(14);

        nameField = addEditBox(configNameFieldX, configNameFieldY,
                configNameFieldW, configNameFieldH, editName, s -> editName = s);
        nameField.setMaxLength(128);

        addRenderableWidget(Button.builder(
                Component.literal(editNameVisible ? "V" : "X"),
                b -> {
                    editNameVisible = !editNameVisible;
                    b.setMessage(Component.literal(editNameVisible ? "V" : "X"));
                }
        ).bounds(configNameVisibleBtnX, configNameVisibleBtnY,
                configNameVisibleBtnW, configNameVisibleBtnH).build());

        y += sw(22);
        configLookBtnX = sx(10); configLookBtnY = y;
        configLookBtnW = sw(140); configLookBtnH = sw(14);
        configLookRadiusFieldX = sx(155); configLookRadiusFieldY = y;
        configLookRadiusFieldW = sw(40);  configLookRadiusFieldH = sw(14);

        addRenderableWidget(Button.builder(
                Component.literal(editLookAtPlayers ? "Olhar jogadores: ON" : "Olhar jogadores: OFF"),
                b -> {
                    editLookAtPlayers = !editLookAtPlayers;
                    b.setMessage(Component.literal(editLookAtPlayers ? "Olhar jogadores: ON" : "Olhar jogadores: OFF"));
                }
        ).bounds(configLookBtnX, configLookBtnY, configLookBtnW, configLookBtnH).build());

        lookRadiusField = addEditBox(configLookRadiusFieldX, configLookRadiusFieldY,
                configLookRadiusFieldW, configLookRadiusFieldH, fmtF(editLookRadius),
                s -> { try { editLookRadius = Math.min(Float.parseFloat(s), 64f); } catch (NumberFormatException ignored) {} });

        y += sw(22);
        configGravityBtnX = sx(10); configGravityBtnY = y;
        configGravityBtnW = sw(140); configGravityBtnH = sw(14);

        addRenderableWidget(Button.builder(
                Component.literal(editHasGravity ? "Gravidade: ON" : "Gravidade: OFF"),
                b -> {
                    editHasGravity = !editHasGravity;
                    b.setMessage(Component.literal(editHasGravity ? "Gravidade: ON" : "Gravidade: OFF"));
                }
        ).bounds(configGravityBtnX, configGravityBtnY, configGravityBtnW, configGravityBtnH).build());

        y += sw(22);
        configSaveBtnX   = sx(0) + gap;   configSaveBtnY   = y; configSaveBtnW   = btnSaveW;   configSaveBtnH   = sw(14);
        configLoadBtnX   = configSaveBtnX   + btnSaveW   + gap; configLoadBtnY   = y; configLoadBtnW   = btnLoadW;   configLoadBtnH   = sw(14);
        configRemoveBtnX = configLoadBtnX   + btnLoadW   + gap; configRemoveBtnY = y; configRemoveBtnW = btnRemoveW; configRemoveBtnH = sw(14);

        addRenderableWidget(Button.builder(
                Component.literal("Salvar NPC"),
                b -> savePreset()
        ).bounds(configSaveBtnX, configSaveBtnY, configSaveBtnW, configSaveBtnH).build());

        addRenderableWidget(Button.builder(
                Component.literal("Carregar NPC"),
                b -> loadPreset()
        ).bounds(configLoadBtnX, configLoadBtnY, configLoadBtnW, configLoadBtnH).build());

        addRenderableWidget(Button.builder(
                Component.literal("Remover NPC"),
                b -> Minecraft.getInstance().setScreen(new ConfirmRemoveScreen(this, npc))
        ).bounds(configRemoveBtnX, configRemoveBtnY, configRemoveBtnW, configRemoveBtnH).build());
    }

    @Override
    protected void renderTabContent(GuiGraphics g, int mx, int my) {
        g.drawString(font, "Nome:", configNameFieldX, configNameFieldY - sw(8), 0xFF404040, false);
    }

    private void savePreset() {
        new Thread(() -> {
            try {
                String path = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_saveFileDialog(
                        "Salvar NPC Preset", "npc_preset.nbt", null, "NPC Preset (*.nbt)");
                if (path != null) {
                    CompoundTag preset = buildSaveData();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    net.minecraft.nbt.NbtIo.writeCompressed(preset, baos);
                    Files.write(Path.of(path), baos.toByteArray());
                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().player.displayClientMessage(
                                    Component.literal("§aPreset salvo!"), true));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, "PresetSave").start();
    }

    private void loadPreset() {
        new Thread(() -> {
            try {
                String path = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(
                        "Carregar NPC Preset", "", null, "NPC Preset (*.nbt)", false);
                if (path != null) {
                    byte[] data = Files.readAllBytes(Path.of(path));
                    CompoundTag preset = net.minecraft.nbt.NbtIo.readCompressed(
                            new ByteArrayInputStream(data),
                            net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                    Minecraft.getInstance().execute(() -> {
                        applyPreset(preset);
                        clearWidgets();
                        initCurrentTab();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, "PresetLoad").start();
    }

    private CompoundTag buildSaveData() {
        CompoundTag tag = new CompoundTag();
        tag.putString("NpcName",       editName);
        tag.putString("ModelType",     editModelType);
        tag.putBoolean("Invulnerable", editInvulnerable);
        tag.putBoolean("LookAtPlayers",editLookAtPlayers);
        tag.putFloat("LookRadius",     editLookRadius);
        tag.putFloat("ScaleX",         editScaleX);
        tag.putFloat("ScaleY",         editScaleY);
        tag.putFloat("ScaleZ",         editScaleZ);
        tag.putFloat("RotationX",      editRotX);
        tag.putFloat("RotationY",      editRotY);
        tag.putFloat("RotationZ",      editRotZ);
        tag.putBoolean("HasGravity",   editHasGravity);
        tag.putBoolean("NameVisible",  editNameVisible);

        ListTag tradesTag = new ListTag();
        for (NpcTradeData t : editTrades)
            if (t.isValid()) tradesTag.add(t.save(Minecraft.getInstance().level.registryAccess()));
        tag.put("Trades", tradesTag);

        ListTag equipList = new ListTag();
        for (int i = 0; i < editEquipment.length; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("Slot", i);
            if (!editEquipment[i].isEmpty())
                slotTag.put("Item", editEquipment[i].save(Minecraft.getInstance().level.registryAccess()));
            equipList.add(slotTag);
        }
        tag.put("Equipment", equipList);
        return tag;
    }

    private void applyPreset(CompoundTag tag) {
        if (tag.contains("NpcName"))       editName          = tag.getString("NpcName");
        if (tag.contains("ModelType"))     editModelType     = tag.getString("ModelType");
        if (tag.contains("Invulnerable"))  editInvulnerable  = tag.getBoolean("Invulnerable");
        if (tag.contains("LookAtPlayers")) editLookAtPlayers = tag.getBoolean("LookAtPlayers");
        if (tag.contains("LookRadius"))    editLookRadius    = tag.getFloat("LookRadius");
        if (tag.contains("ScaleX"))        editScaleX        = tag.getFloat("ScaleX");
        if (tag.contains("ScaleY"))        editScaleY        = tag.getFloat("ScaleY");
        if (tag.contains("ScaleZ"))        editScaleZ        = tag.getFloat("ScaleZ");
        if (tag.contains("RotationX"))     editRotX          = tag.getFloat("RotationX");
        if (tag.contains("RotationY"))     editRotY          = tag.getFloat("RotationY");
        if (tag.contains("RotationZ"))     editRotZ          = tag.getFloat("RotationZ");
        if (tag.contains("HasGravity"))    editHasGravity    = tag.getBoolean("HasGravity");
        if (tag.contains("NameVisible"))   editNameVisible   = tag.getBoolean("NameVisible");

        if (tag.contains("Trades")) {
            editTrades.clear();
            ListTag tradesTag = tag.getList("Trades", 10);
            for (int i = 0; i < tradesTag.size(); i++)
                editTrades.add(NpcTradeData.load(tradesTag.getCompound(i),
                        Minecraft.getInstance().level.registryAccess()));
            while (editTrades.size() < TRADES_PER_PAGE) editTrades.add(new NpcTradeData());
        }
        if (tag.contains("Equipment")) {
            ListTag equipTag = tag.getList("Equipment", 10);
            for (int i = 0; i < equipTag.size(); i++) {
                CompoundTag slotTag = equipTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                if (slot >= 0 && slot < editEquipment.length && slotTag.contains("Item"))
                    editEquipment[slot] = ItemStack.parse(
                            Minecraft.getInstance().level.registryAccess(),
                            slotTag.getCompound("Item")).orElse(ItemStack.EMPTY);
            }
        }
        Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§aPreset carregado!"), true);
    }

    @Override
    protected boolean anyFieldFocused() {
        if (nameField       != null && nameField.isFocused())       return true;
        if (lookRadiusField != null && lookRadiusField.isFocused()) return true;
        return false;
    }
}