package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.DadosTrocaNpc;
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
public class AbaConfiguracoesNpc extends TelaEditorNpc {

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

    public AbaConfiguracoesNpc(EntidadeNpcPersonalizado npc, CompoundTag npcData, TelaEditorNpc origin) {
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
                Component.translatable(editLookAtPlayers ? "gui.aelysiummod.npc.olhar_jogadores_on" : "gui.aelysiummod.npc.olhar_jogadores_off"),
                b -> {
                    editLookAtPlayers = !editLookAtPlayers;
                    b.setMessage(Component.translatable(editLookAtPlayers ? "gui.aelysiummod.npc.olhar_jogadores_on" : "gui.aelysiummod.npc.olhar_jogadores_off"));
                }
        ).bounds(configLookBtnX, configLookBtnY, configLookBtnW, configLookBtnH).build());

        lookRadiusField = addEditBox(configLookRadiusFieldX, configLookRadiusFieldY,
                configLookRadiusFieldW, configLookRadiusFieldH, fmtF(editLookRadius),
                s -> { try { editLookRadius = Math.min(Float.parseFloat(s), 64f); } catch (NumberFormatException ignored) {} });

        y += sw(22);
        configGravityBtnX = sx(10); configGravityBtnY = y;
        configGravityBtnW = sw(140); configGravityBtnH = sw(14);

        addRenderableWidget(Button.builder(
                Component.translatable(editHasGravity ? "gui.aelysiummod.npc.gravidade_on" : "gui.aelysiummod.npc.gravidade_off"),
                b -> {
                    editHasGravity = !editHasGravity;
                    b.setMessage(Component.translatable(editHasGravity ? "gui.aelysiummod.npc.gravidade_on" : "gui.aelysiummod.npc.gravidade_off"));
                }
        ).bounds(configGravityBtnX, configGravityBtnY, configGravityBtnW, configGravityBtnH).build());

        y += sw(22);
        configSaveBtnX   = sx(0) + gap;   configSaveBtnY   = y; configSaveBtnW   = btnSaveW;   configSaveBtnH   = sw(14);
        configLoadBtnX   = configSaveBtnX   + btnSaveW   + gap; configLoadBtnY   = y; configLoadBtnW   = btnLoadW;   configLoadBtnH   = sw(14);
        configRemoveBtnX = configLoadBtnX   + btnLoadW   + gap; configRemoveBtnY = y; configRemoveBtnW = btnRemoveW; configRemoveBtnH = sw(14);

        addRenderableWidget(Button.builder(
                Component.translatable("gui.aelysiummod.npc.salvar_npc"),
                b -> savePreset()
        ).bounds(configSaveBtnX, configSaveBtnY, configSaveBtnW, configSaveBtnH).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.aelysiummod.npc.carregar_npc"),
                b -> loadPreset()
        ).bounds(configLoadBtnX, configLoadBtnY, configLoadBtnW, configLoadBtnH).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.aelysiummod.npc.remover_npc"),
                b -> Minecraft.getInstance().setScreen(new TelaConfirmarRemocao(this, npc))
        ).bounds(configRemoveBtnX, configRemoveBtnY, configRemoveBtnW, configRemoveBtnH).build());
    }

    @Override
    protected void renderTabContent(GuiGraphics g, int mx, int my) {
        g.drawString(font, Component.translatable("gui.aelysiummod.npc.nome"), configNameFieldX, configNameFieldY - sw(8), 0xFF404040, false);
    }

    private void savePreset() {
        saveAll();
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.PedirArquivoNpcC2S(npc.getId()));
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

        if (tag.contains("SkinData")) {
            EnvioSkinNpc.enviar(npc.getId(), tag.getByteArray("SkinData"), editModelType);
        }

        if (tag.contains("Trades")) {
            editTrades.clear();
            ListTag tradesTag = tag.getList("Trades", 10);
            for (int i = 0; i < tradesTag.size(); i++)
                editTrades.add(DadosTrocaNpc.load(tradesTag.getCompound(i),
                        Minecraft.getInstance().level.registryAccess()));
            while (editTrades.size() < TRADES_PER_PAGE) editTrades.add(new DadosTrocaNpc());
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
                Component.translatable("gui.aelysiummod.npc.preset_carregado"), true);
    }

    @Override
    protected boolean anyFieldFocused() {
        if (nameField       != null && nameField.isFocused())       return true;
        if (lookRadiusField != null && lookRadiusField.isFocused()) return true;
        return false;
    }
}