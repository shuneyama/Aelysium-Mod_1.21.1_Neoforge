package net.aelysium.aelysiummod.menu.gui;

import net.aelysium.aelysiummod.npc.client.renderer.FakeNpcPlayer;
import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.npc.network.NpcPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.nio.file.Files;

@OnlyIn(Dist.CLIENT)
public class NpcAppearanceTab extends NpcEditorScreen {

    // Coordenadas dos widgets
    private int btnModelTypeX, btnModelTypeY, btnModelTypeW, btnModelTypeH;
    private int btnUploadX,    btnUploadY,    btnUploadW,    btnUploadH;
    private int previewX, previewY, previewW, previewH;

    // Escala
    private int scaleBtnMinusXx, scaleBtnMinusXy, scaleBtnMinusXw, scaleBtnMinusXh;
    private int scaleBtnPlusXx,  scaleBtnPlusXy,  scaleBtnPlusXw,  scaleBtnPlusXh;
    private int scaleBtnMinusYx, scaleBtnMinusYy, scaleBtnMinusYw, scaleBtnMinusYh;
    private int scaleBtnPlusYx,  scaleBtnPlusYy,  scaleBtnPlusYw,  scaleBtnPlusYh;
    private int scaleBtnMinusZx, scaleBtnMinusZy, scaleBtnMinusZw, scaleBtnMinusZh;
    private int scaleBtnPlusZx,  scaleBtnPlusZy,  scaleBtnPlusZw,  scaleBtnPlusZh;

    // Posição
    private int posBtnMinusXx, posBtnMinusXy, posBtnMinusXw, posBtnMinusXh;
    private int posBtnPlusXx,  posBtnPlusXy,  posBtnPlusXw,  posBtnPlusXh;
    private int posBtnMinusYx, posBtnMinusYy, posBtnMinusYw, posBtnMinusYh;
    private int posBtnPlusYx,  posBtnPlusYy,  posBtnPlusYw,  posBtnPlusYh;
    private int posBtnMinusZx, posBtnMinusZy, posBtnMinusZw, posBtnMinusZh;
    private int posBtnPlusZx,  posBtnPlusZy,  posBtnPlusZw,  posBtnPlusZh;

    // Rotação
    private int rotBtnMinusXx, rotBtnMinusXy, rotBtnMinusXw, rotBtnMinusXh;
    private int rotBtnPlusXx,  rotBtnPlusXy,  rotBtnPlusXw,  rotBtnPlusXh;
    private int rotBtnMinusYx, rotBtnMinusYy, rotBtnMinusYw, rotBtnMinusYh;
    private int rotBtnPlusYx,  rotBtnPlusYy,  rotBtnPlusYw,  rotBtnPlusYh;
    private int rotBtnMinusZx, rotBtnMinusZy, rotBtnMinusZw, rotBtnMinusZh;
    private int rotBtnPlusZx,  rotBtnPlusZy,  rotBtnPlusZw,  rotBtnPlusZh;

    // EditBoxes
    private EditBox scaleXBox, scaleYBox, scaleZBox;
    private EditBox posXBox,   posYBox,   posZBox;
    private EditBox rotXBox,   rotYBox,   rotZBox;

    public NpcAppearanceTab(CustomNpcEntity npc, CompoundTag npcData, NpcEditorScreen origin) {
        super(npc, npcData);
        if (origin != null) origin.copyStateTo(this);
        this.currentTab = 1;
    }

    @Override
    protected void initCurrentTab() {
        btnModelTypeX = sx(23); btnModelTypeY = sy(40); btnModelTypeW = sw(80); btnModelTypeH = sw(15);
        btnUploadX    = sx(23); btnUploadY    = sy(56); btnUploadW    = sw(80); btnUploadH    = sw(15);

        addRenderableWidget(Button.builder(
                Component.literal("Modelo: " + ("slim".equals(editModelType) ? "Alex" : "Steve")),
                b -> {
                    editModelType = "slim".equals(editModelType) ? "wide" : "slim";
                    b.setMessage(Component.literal("Modelo: " + ("slim".equals(editModelType) ? "Alex" : "Steve")));
                    syncFakePlayerModel();
                }
        ).bounds(btnModelTypeX, btnModelTypeY, btnModelTypeW, btnModelTypeH).build());

        addRenderableWidget(Button.builder(
                Component.literal("Upload Skin"),
                b -> uploadSkin()
        ).bounds(btnUploadX, btnUploadY, btnUploadW, btnUploadH).build());

        previewX = sx(166); previewY = sy(43); previewW = sw(49); previewH = sw(70);

        int btnS   = sw(9);
        int fieldW = sw(28);
        int fieldH = sw(13);

        // Escala
        scaleBtnMinusXx = sx(68);  scaleBtnMinusXy = sy(77);  scaleBtnMinusXw = btnS; scaleBtnMinusXh = btnS;
        int scaleXFieldX = sx(80); int scaleXFieldY = sy(75);
        scaleBtnPlusXx  = sx(111); scaleBtnPlusXy  = sy(77);  scaleBtnPlusXw  = btnS; scaleBtnPlusXh  = btnS;

        scaleBtnMinusYx = sx(68);  scaleBtnMinusYy = sy(92);  scaleBtnMinusYw = btnS; scaleBtnMinusYh = btnS;
        int scaleYFieldX = sx(80); int scaleYFieldY = sy(90);
        scaleBtnPlusYx  = sx(111); scaleBtnPlusYy  = sy(92);  scaleBtnPlusYw  = btnS; scaleBtnPlusYh  = btnS;

        scaleBtnMinusZx = sx(68);  scaleBtnMinusZy = sy(107); scaleBtnMinusZw = btnS; scaleBtnMinusZh = btnS;
        int scaleZFieldX = sx(80); int scaleZFieldY = sy(105);
        scaleBtnPlusZx  = sx(111); scaleBtnPlusZy  = sy(107); scaleBtnPlusZw  = btnS; scaleBtnPlusZh  = btnS;

        // Posição
        posBtnMinusXx = sx(68);  posBtnMinusXy = sy(125); posBtnMinusXw = btnS; posBtnMinusXh = btnS;
        int posXFieldX = sx(80); int posXFieldY = sy(123);
        posBtnPlusXx  = sx(111); posBtnPlusXy  = sy(125); posBtnPlusXw  = btnS; posBtnPlusXh  = btnS;

        posBtnMinusYx = sx(68);  posBtnMinusYy = sy(140); posBtnMinusYw = btnS; posBtnMinusYh = btnS;
        int posYFieldX = sx(80); int posYFieldY = sy(138);
        posBtnPlusYx  = sx(111); posBtnPlusYy  = sy(140); posBtnPlusYw  = btnS; posBtnPlusYh  = btnS;

        posBtnMinusZx = sx(68);  posBtnMinusZy = sy(155); posBtnMinusZw = btnS; posBtnMinusZh = btnS;
        int posZFieldX = sx(80); int posZFieldY = sy(153);
        posBtnPlusZx  = sx(111); posBtnPlusZy  = sy(155); posBtnPlusZw  = btnS; posBtnPlusZh  = btnS;

        // Rotação
        rotBtnMinusXx = sx(185); rotBtnMinusXy = sy(125); rotBtnMinusXw = btnS; rotBtnMinusXh = btnS;
        int rotXFieldX = sx(197); int rotXFieldY = sy(123);
        rotBtnPlusXx  = sx(228); rotBtnPlusXy  = sy(125); rotBtnPlusXw  = btnS; rotBtnPlusXh  = btnS;

        rotBtnMinusYx = sx(185); rotBtnMinusYy = sy(140); rotBtnMinusYw = btnS; rotBtnMinusYh = btnS;
        int rotYFieldX = sx(197); int rotYFieldY = sy(138);
        rotBtnPlusYx  = sx(228); rotBtnPlusYy  = sy(140); rotBtnPlusYw  = btnS; rotBtnPlusYh  = btnS;

        rotBtnMinusZx = sx(185); rotBtnMinusZy = sy(155); rotBtnMinusZw = btnS; rotBtnMinusZh = btnS;
        int rotZFieldX = sx(197); int rotZFieldY = sy(153);
        rotBtnPlusZx  = sx(228); rotBtnPlusZy  = sy(155); rotBtnPlusZw  = btnS; rotBtnPlusZh  = btnS;

        // EditBoxes de escala / posição / rotação
        scaleXBox = addEditBox(scaleXFieldX, scaleXFieldY, fieldW, fieldH, fmtF(editScaleX), s -> { try { editScaleX = Float.parseFloat(s); } catch (NumberFormatException ignored) {} });
        scaleYBox = addEditBox(scaleYFieldX, scaleYFieldY, fieldW, fieldH, fmtF(editScaleY), s -> { try { editScaleY = Float.parseFloat(s); } catch (NumberFormatException ignored) {} });
        scaleZBox = addEditBox(scaleZFieldX, scaleZFieldY, fieldW, fieldH, fmtF(editScaleZ), s -> { try { editScaleZ = Float.parseFloat(s); } catch (NumberFormatException ignored) {} });
        posXBox   = addEditBox(posXFieldX,   posXFieldY,   fieldW, fieldH, fmtF(editPosX),   s -> { try { editPosX   = Float.parseFloat(s); } catch (NumberFormatException ignored) {} });
        posYBox   = addEditBox(posYFieldX,   posYFieldY,   fieldW, fieldH, fmtF(editPosY),   s -> { try { editPosY   = Float.parseFloat(s); } catch (NumberFormatException ignored) {} });
        posZBox   = addEditBox(posZFieldX,   posZFieldY,   fieldW, fieldH, fmtF(editPosZ),   s -> { try { editPosZ   = Float.parseFloat(s); } catch (NumberFormatException ignored) {} });
        rotXBox   = addEditBox(rotXFieldX,   rotXFieldY,   fieldW, fieldH, fmtF(editRotX),   s -> { try { editRotX   = Float.parseFloat(s); } catch (NumberFormatException ignored) {} });
        rotYBox   = addEditBox(rotYFieldX,   rotYFieldY,   fieldW, fieldH, fmtF(editRotY),   s -> { try { editRotY   = Float.parseFloat(s); } catch (NumberFormatException ignored) {} });
        rotZBox   = addEditBox(rotZFieldX,   rotZFieldY,   fieldW, fieldH, fmtF(editRotZ),   s -> { try { editRotZ   = Float.parseFloat(s); } catch (NumberFormatException ignored) {} });
    }
    @Override
    protected void renderTabContent(GuiGraphics g, int mx, int my) {
        try {
            FakeNpcPlayer fakePlayer = FakeNpcPlayer.getOrCreate(npc);
            if (fakePlayer != null) {
                fakePlayer.syncModelType(editModelType);
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        g, previewX, previewY, previewX + previewW, previewY + previewH,
                        30, 0.0625f, mx, my, fakePlayer);
            }
        } catch (Exception ignored) {}

        // Slots de equipamento
        hoveredSlotId = -1;
        int slotSize = sw(16);
        int[][] equipPos = {
                {sx(145), sy(43)}, {sx(145), sy(61)}, {sx(145), sy(79)}, {sx(145), sy(97)},
                {sx(220), sy(97)}, {sx(220), sy(79)}
        };
        for (int i = 0; i < 6; i++)
            renderEquipSlot(g, editEquipment[i], equipPos[i][0], equipPos[i][1], mx, my, 1000 + i, slotSize);

        // Highlight dos botões +/-
        renderMinusPlusHover(g, mx, my, scaleBtnMinusXx, scaleBtnMinusXy, scaleBtnMinusXw, scaleBtnMinusXh, scaleBtnPlusXx, scaleBtnPlusXy, scaleBtnPlusXw, scaleBtnPlusXh);
        renderMinusPlusHover(g, mx, my, scaleBtnMinusYx, scaleBtnMinusYy, scaleBtnMinusYw, scaleBtnMinusYh, scaleBtnPlusYx, scaleBtnPlusYy, scaleBtnPlusYw, scaleBtnPlusYh);
        renderMinusPlusHover(g, mx, my, scaleBtnMinusZx, scaleBtnMinusZy, scaleBtnMinusZw, scaleBtnMinusZh, scaleBtnPlusZx, scaleBtnPlusZy, scaleBtnPlusZw, scaleBtnPlusZh);
        renderMinusPlusHover(g, mx, my, posBtnMinusXx, posBtnMinusXy, posBtnMinusXw, posBtnMinusXh, posBtnPlusXx, posBtnPlusXy, posBtnPlusXw, posBtnPlusXh);
        renderMinusPlusHover(g, mx, my, posBtnMinusYx, posBtnMinusYy, posBtnMinusYw, posBtnMinusYh, posBtnPlusYx, posBtnPlusYy, posBtnPlusYw, posBtnPlusYh);
        renderMinusPlusHover(g, mx, my, posBtnMinusZx, posBtnMinusZy, posBtnMinusZw, posBtnMinusZh, posBtnPlusZx, posBtnPlusZy, posBtnPlusZw, posBtnPlusZh);
        renderMinusPlusHover(g, mx, my, rotBtnMinusXx, rotBtnMinusXy, rotBtnMinusXw, rotBtnMinusXh, rotBtnPlusXx, rotBtnPlusXy, rotBtnPlusXw, rotBtnPlusXh);
        renderMinusPlusHover(g, mx, my, rotBtnMinusYx, rotBtnMinusYy, rotBtnMinusYw, rotBtnMinusYh, rotBtnPlusYx, rotBtnPlusYy, rotBtnPlusYw, rotBtnPlusYh);
        renderMinusPlusHover(g, mx, my, rotBtnMinusZx, rotBtnMinusZy, rotBtnMinusZw, rotBtnMinusZh, rotBtnPlusZx, rotBtnPlusZy, rotBtnPlusZw, rotBtnPlusZh);
    }

    private void renderMinusPlusHover(GuiGraphics g, int mx, int my,
                                      int mx1, int my1, int mw, int mh,
                                      int px,  int py,  int pw, int ph) {
        if (isIn(mx, my, mx1, my1, mw, mh)) g.fill(mx1, my1, mx1 + mw, my1 + mh, 0x44FFFFFF);
        if (isIn(mx, my, px,  py,  pw, ph)) g.fill(px,  py,  px  + pw, py  + ph, 0x44FFFFFF);
    }

    private void renderEquipSlot(GuiGraphics g, ItemStack stack, int x, int y,
                                 int mx, int my, int slotId, int size) {
        boolean hov = isIn(mx, my, x, y, size, size);
        if (hov) { g.fill(x, y, x + size, y + size, 0x44FFFFFF); hoveredSlotId = slotId; }
        if (!stack.isEmpty()) {
            g.pose().pushPose();
            float sc = size / 16.0f;
            g.pose().translate(x, y, 0);
            g.pose().scale(sc, sc, 1);
            g.renderItem(stack, 0, 0);
            g.pose().popPose();
        }
    }

    @Override
    protected void renderAllTooltips(GuiGraphics g, int mx, int my) {
        super.renderAllTooltips(g, mx, my);
        if (heldStack.isEmpty() && hoveredSlotId >= 1000 && hoveredSlotId < 1006) {
            int idx = hoveredSlotId - 1000;
            if (!editEquipment[idx].isEmpty()) g.renderTooltip(font, editEquipment[idx], mx, my);
        }
    }
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int imx = (int) mx, imy = (int) my;

        // Tabs + inventário da classe base
        if (handleBaseClick(imx, imy, btn)) return true;

        // Botões +/- de escala
        if (checkMinusPlusClick(imx, imy, scaleBtnMinusXx, scaleBtnMinusXy, scaleBtnMinusXw, scaleBtnMinusXh, scaleBtnPlusXx, scaleBtnPlusXy, scaleBtnPlusXw, scaleBtnPlusXh, 0.1f, v -> { editScaleX += v; if (scaleXBox != null) scaleXBox.setValue(fmtF(editScaleX)); })) return true;
        if (checkMinusPlusClick(imx, imy, scaleBtnMinusYx, scaleBtnMinusYy, scaleBtnMinusYw, scaleBtnMinusYh, scaleBtnPlusYx, scaleBtnPlusYy, scaleBtnPlusYw, scaleBtnPlusYh, 0.1f, v -> { editScaleY += v; if (scaleYBox != null) scaleYBox.setValue(fmtF(editScaleY)); })) return true;
        if (checkMinusPlusClick(imx, imy, scaleBtnMinusZx, scaleBtnMinusZy, scaleBtnMinusZw, scaleBtnMinusZh, scaleBtnPlusZx, scaleBtnPlusZy, scaleBtnPlusZw, scaleBtnPlusZh, 0.1f, v -> { editScaleZ += v; if (scaleZBox != null) scaleZBox.setValue(fmtF(editScaleZ)); })) return true;

        // Botões +/- de posição
        if (checkMinusPlusClick(imx, imy, posBtnMinusXx, posBtnMinusXy, posBtnMinusXw, posBtnMinusXh, posBtnPlusXx, posBtnPlusXy, posBtnPlusXw, posBtnPlusXh, 0.5f, v -> { editPosX += v; if (posXBox != null) posXBox.setValue(fmtF(editPosX)); })) return true;
        if (checkMinusPlusClick(imx, imy, posBtnMinusYx, posBtnMinusYy, posBtnMinusYw, posBtnMinusYh, posBtnPlusYx, posBtnPlusYy, posBtnPlusYw, posBtnPlusYh, 0.5f, v -> { editPosY += v; if (posYBox != null) posYBox.setValue(fmtF(editPosY)); })) return true;
        if (checkMinusPlusClick(imx, imy, posBtnMinusZx, posBtnMinusZy, posBtnMinusZw, posBtnMinusZh, posBtnPlusZx, posBtnPlusZy, posBtnPlusZw, posBtnPlusZh, 0.5f, v -> { editPosZ += v; if (posZBox != null) posZBox.setValue(fmtF(editPosZ)); })) return true;

        // Botões +/- de rotação
        if (checkMinusPlusClick(imx, imy, rotBtnMinusXx, rotBtnMinusXy, rotBtnMinusXw, rotBtnMinusXh, rotBtnPlusXx, rotBtnPlusXy, rotBtnPlusXw, rotBtnPlusXh, 5f, v -> { editRotX += v; if (rotXBox != null) rotXBox.setValue(fmtF(editRotX)); })) return true;
        if (checkMinusPlusClick(imx, imy, rotBtnMinusYx, rotBtnMinusYy, rotBtnMinusYw, rotBtnMinusYh, rotBtnPlusYx, rotBtnPlusYy, rotBtnPlusYw, rotBtnPlusYh, 5f, v -> { editRotY += v; if (rotYBox != null) rotYBox.setValue(fmtF(editRotY)); })) return true;
        if (checkMinusPlusClick(imx, imy, rotBtnMinusZx, rotBtnMinusZy, rotBtnMinusZw, rotBtnMinusZh, rotBtnPlusZx, rotBtnPlusZy, rotBtnPlusZw, rotBtnPlusZh, 5f, v -> { editRotZ += v; if (rotZBox != null) rotZBox.setValue(fmtF(editRotZ)); })) return true;

        // Slots de equipamento
        if (btn == 0 && hoveredSlotId >= 1000 && hoveredSlotId < 1006) {
            int idx = hoveredSlotId - 1000;
            if (!heldStack.isEmpty()) {
                editEquipment[idx] = heldStack.copy();
                editEquipment[idx].setCount(1);
                heldStack = ItemStack.EMPTY; heldCount = 0; return true;
            } else if (!editEquipment[idx].isEmpty()) {
                heldStack = editEquipment[idx].copy(); heldCount = 1;
                editEquipment[idx] = ItemStack.EMPTY; return true;
            }
        }
        if (btn == 1 && hoveredSlotId >= 1000 && hoveredSlotId < 1006) {
            editEquipment[hoveredSlotId - 1000] = ItemStack.EMPTY; return true;
        }
        if (btn == 0 && !heldStack.isEmpty()) { heldStack = ItemStack.EMPTY; heldCount = 0; return true; }
        return super.mouseClicked(mx, my, btn);
    }

    private void uploadSkin() {
        new Thread(() -> {
            try {
                String path = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(
                        "Selecionar Skin PNG", "", null, "Skin PNG (*.png)", false);
                if (path != null) {
                    File file = new File(path);
                    if (file.exists() && file.length() < 65536L) {
                        byte[] data = Files.readAllBytes(file.toPath());
                        Minecraft.getInstance().execute(() ->
                                PacketDistributor.sendToServer(
                                        new NpcPackets.UploadNpcSkinC2S(npc.getId(), data, editModelType)));
                    } else if (file.length() >= 65536L) {
                        Minecraft.getInstance().execute(() ->
                                Minecraft.getInstance().player.displayClientMessage(
                                        Component.literal("§cArquivo muito grande! Maximo: 64 KB"), true));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, "FileUpload").start();
    }

    private void syncFakePlayerModel() {
        FakeNpcPlayer fakePlayer = FakeNpcPlayer.getOrCreate(npc);
        if (fakePlayer != null) fakePlayer.syncModelType(editModelType);
    }

    @Override
    protected boolean anyFieldFocused() {
        if (scaleXBox != null && scaleXBox.isFocused()) return true;
        if (scaleYBox != null && scaleYBox.isFocused()) return true;
        if (scaleZBox != null && scaleZBox.isFocused()) return true;
        if (posXBox   != null && posXBox.isFocused())   return true;
        if (posYBox   != null && posYBox.isFocused())   return true;
        if (posZBox   != null && posZBox.isFocused())   return true;
        if (rotXBox   != null && rotXBox.isFocused())   return true;
        if (rotYBox   != null && rotYBox.isFocused())   return true;
        if (rotZBox   != null && rotZBox.isFocused())   return true;
        return false;
    }
}