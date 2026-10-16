package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import net.aelysium.aelysiummod.sistemas.social.npc.cliente.JogadorNpcFalso;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc;
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
public class AbaAparenciaNpc extends TelaEditorNpc {


    private int btnModelTypeX, btnModelTypeY, btnModelTypeW, btnModelTypeH;
    private int btnUploadX,    btnUploadY,    btnUploadW,    btnUploadH;
    private int previewX, previewY, previewW, previewH;


    private int scaleBtnMinusXx, scaleBtnMinusXy, scaleBtnMinusXw, scaleBtnMinusXh;
    private int scaleBtnPlusXx,  scaleBtnPlusXy,  scaleBtnPlusXw,  scaleBtnPlusXh;
    private int scaleBtnMinusYx, scaleBtnMinusYy, scaleBtnMinusYw, scaleBtnMinusYh;
    private int scaleBtnPlusYx,  scaleBtnPlusYy,  scaleBtnPlusYw,  scaleBtnPlusYh;
    private int scaleBtnMinusZx, scaleBtnMinusZy, scaleBtnMinusZw, scaleBtnMinusZh;
    private int scaleBtnPlusZx,  scaleBtnPlusZy,  scaleBtnPlusZw,  scaleBtnPlusZh;


    private int posBtnMinusXx, posBtnMinusXy, posBtnMinusXw, posBtnMinusXh;
    private int posBtnPlusXx,  posBtnPlusXy,  posBtnPlusXw,  posBtnPlusXh;
    private int posBtnMinusYx, posBtnMinusYy, posBtnMinusYw, posBtnMinusYh;
    private int posBtnPlusYx,  posBtnPlusYy,  posBtnPlusYw,  posBtnPlusYh;
    private int posBtnMinusZx, posBtnMinusZy, posBtnMinusZw, posBtnMinusZh;
    private int posBtnPlusZx,  posBtnPlusZy,  posBtnPlusZw,  posBtnPlusZh;


    private int rotBtnMinusXx, rotBtnMinusXy, rotBtnMinusXw, rotBtnMinusXh;
    private int rotBtnPlusXx,  rotBtnPlusXy,  rotBtnPlusXw,  rotBtnPlusXh;
    private int rotBtnMinusYx, rotBtnMinusYy, rotBtnMinusYw, rotBtnMinusYh;
    private int rotBtnPlusYx,  rotBtnPlusYy,  rotBtnPlusYw,  rotBtnPlusYh;
    private int rotBtnMinusZx, rotBtnMinusZy, rotBtnMinusZw, rotBtnMinusZh;
    private int rotBtnPlusZx,  rotBtnPlusZy,  rotBtnPlusZw,  rotBtnPlusZh;


    private EditBox scaleXBox, scaleYBox, scaleZBox;
    private EditBox posXBox,   posYBox,   posZBox;
    private EditBox rotXBox,   rotYBox,   rotZBox;

    public AbaAparenciaNpc(EntidadeNpcPersonalizado npc, CompoundTag npcData, TelaEditorNpc origin) {
        super(npc, npcData);
        if (origin != null) origin.copyStateTo(this);
        this.currentTab = 1;
    }

    private static final java.util.function.Predicate<String> FILTRO_ESCALA = t -> t.matches("\\d{0,2}([.]\\d?)?");
    private static final java.util.function.Predicate<String> FILTRO_POSICAO = t -> t.matches("-?\\d{0,8}([.]\\d?)?");
    private static final java.util.function.Predicate<String> FILTRO_ROTACAO = t -> {
        if (!t.matches("\\d{0,3}([.]\\d?)?")) return false;
        try { return t.isEmpty() || t.equals(".") || Float.parseFloat(t) <= 360f; } catch (NumberFormatException e) { return false; }
    };

    private static String fmt1(float v) { return String.format(java.util.Locale.ROOT, "%.1f", v); }

    private static float arredondar(float v) { return Math.round(v * 10f) / 10f; }

    private static float limitarEscala(float v) { return Math.max(0.1f, Math.min(99.9f, arredondar(v))); }

    private static float limitarRotacao(float v) { return Math.max(0f, Math.min(360f, arredondar(v))); }

    private static float ajustarEscala(float atual, float v) { return limitarEscala(atual + v); }

    private static float ajustarPosicao(float atual, float v) { return arredondar(atual + v); }

    private static float ajustarRotacao(float atual, float v) {
        float r = (atual + v) % 360f;
        if (r < 0f) r += 360f;
        return arredondar(r);
    }

    private CampoNumeroNpc campoNumerico(int x, int y, int w, int h, String valor,
                                         java.util.function.Predicate<String> filtro,
                                         java.util.function.Consumer<Float> aplicar) {
        CampoNumeroNpc box = new CampoNumeroNpc(this.font, x, y, w, h).centralizarSemMoldura(0xC6C6C6);
        box.setMaxLength(12);
        box.setFilter(filtro);
        box.setValue(valor);
        box.setResponder(t -> { try { aplicar.accept(Float.parseFloat(t)); } catch (NumberFormatException ignored) {} });
        addRenderableWidget(box);
        return box;
    }

    @Override
    protected void initCurrentTab() {
        editScaleX = limitarEscala(editScaleX); editScaleY = limitarEscala(editScaleY); editScaleZ = limitarEscala(editScaleZ);
        editRotX = ajustarRotacao(editRotX, 0f); editRotY = ajustarRotacao(editRotY, 0f); editRotZ = ajustarRotacao(editRotZ, 0f);
        btnModelTypeX = sx(23); btnModelTypeY = sy(40); btnModelTypeW = sw(80); btnModelTypeH = sw(15);
        btnUploadX    = sx(23); btnUploadY    = sy(56); btnUploadW    = sw(80); btnUploadH    = sw(15);

        addRenderableWidget(Button.builder(
                Component.translatableEscape("gui.aelysiummod.npc.modelo", ("slim".equals(editModelType) ? "Alex" : "Steve")),
                b -> {
                    editModelType = "slim".equals(editModelType) ? "wide" : "slim";
                    b.setMessage(Component.translatableEscape("gui.aelysiummod.npc.modelo", ("slim".equals(editModelType) ? "Alex" : "Steve")));
                    syncFakePlayerModel();
                }
        ).bounds(btnModelTypeX, btnModelTypeY, btnModelTypeW, btnModelTypeH).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.aelysiummod.npc.upload_skin"),
                b -> uploadSkin()
        ).bounds(btnUploadX, btnUploadY, btnUploadW, btnUploadH).build());

        previewX = sx(166); previewY = sy(43); previewW = sw(49); previewH = sw(70);

        int btnS   = sw(9);
        int fieldW = sw(28);
        int fieldH = sw(13);


        scaleBtnMinusXx = sx(68);  scaleBtnMinusXy = sy(77);  scaleBtnMinusXw = btnS; scaleBtnMinusXh = btnS;
        int scaleXFieldX = sx(80); int scaleXFieldY = sy(75);
        scaleBtnPlusXx  = sx(111); scaleBtnPlusXy  = sy(77);  scaleBtnPlusXw  = btnS; scaleBtnPlusXh  = btnS;

        scaleBtnMinusYx = sx(68);  scaleBtnMinusYy = sy(92);  scaleBtnMinusYw = btnS; scaleBtnMinusYh = btnS;
        int scaleYFieldX = sx(80); int scaleYFieldY = sy(90);
        scaleBtnPlusYx  = sx(111); scaleBtnPlusYy  = sy(92);  scaleBtnPlusYw  = btnS; scaleBtnPlusYh  = btnS;

        scaleBtnMinusZx = sx(68);  scaleBtnMinusZy = sy(107); scaleBtnMinusZw = btnS; scaleBtnMinusZh = btnS;
        int scaleZFieldX = sx(80); int scaleZFieldY = sy(105);
        scaleBtnPlusZx  = sx(111); scaleBtnPlusZy  = sy(107); scaleBtnPlusZw  = btnS; scaleBtnPlusZh  = btnS;


        posBtnMinusXx = sx(68);  posBtnMinusXy = sy(125); posBtnMinusXw = btnS; posBtnMinusXh = btnS;
        int posXFieldX = sx(80); int posXFieldY = sy(123);
        posBtnPlusXx  = sx(111); posBtnPlusXy  = sy(125); posBtnPlusXw  = btnS; posBtnPlusXh  = btnS;

        posBtnMinusYx = sx(68);  posBtnMinusYy = sy(140); posBtnMinusYw = btnS; posBtnMinusYh = btnS;
        int posYFieldX = sx(80); int posYFieldY = sy(138);
        posBtnPlusYx  = sx(111); posBtnPlusYy  = sy(140); posBtnPlusYw  = btnS; posBtnPlusYh  = btnS;

        posBtnMinusZx = sx(68);  posBtnMinusZy = sy(155); posBtnMinusZw = btnS; posBtnMinusZh = btnS;
        int posZFieldX = sx(80); int posZFieldY = sy(153);
        posBtnPlusZx  = sx(111); posBtnPlusZy  = sy(155); posBtnPlusZw  = btnS; posBtnPlusZh  = btnS;


        rotBtnMinusXx = sx(185); rotBtnMinusXy = sy(125); rotBtnMinusXw = btnS; rotBtnMinusXh = btnS;
        int rotXFieldX = sx(197); int rotXFieldY = sy(123);
        rotBtnPlusXx  = sx(228); rotBtnPlusXy  = sy(125); rotBtnPlusXw  = btnS; rotBtnPlusXh  = btnS;

        rotBtnMinusYx = sx(185); rotBtnMinusYy = sy(140); rotBtnMinusYw = btnS; rotBtnMinusYh = btnS;
        int rotYFieldX = sx(197); int rotYFieldY = sy(138);
        rotBtnPlusYx  = sx(228); rotBtnPlusYy  = sy(140); rotBtnPlusYw  = btnS; rotBtnPlusYh  = btnS;

        rotBtnMinusZx = sx(185); rotBtnMinusZy = sy(155); rotBtnMinusZw = btnS; rotBtnMinusZh = btnS;
        int rotZFieldX = sx(197); int rotZFieldY = sy(153);
        rotBtnPlusZx  = sx(228); rotBtnPlusZy  = sy(155); rotBtnPlusZw  = btnS; rotBtnPlusZh  = btnS;


        scaleXBox = campoNumerico(scaleXFieldX, scaleXFieldY, fieldW, fieldH, fmt1(editScaleX), FILTRO_ESCALA, v -> editScaleX = limitarEscala(v));
        scaleYBox = campoNumerico(scaleYFieldX, scaleYFieldY, fieldW, fieldH, fmt1(editScaleY), FILTRO_ESCALA, v -> editScaleY = limitarEscala(v));
        scaleZBox = campoNumerico(scaleZFieldX, scaleZFieldY, fieldW, fieldH, fmt1(editScaleZ), FILTRO_ESCALA, v -> editScaleZ = limitarEscala(v));
        posXBox = campoNumerico(posXFieldX, posXFieldY, fieldW, fieldH, fmt1(editPosX), FILTRO_POSICAO, v -> editPosX = v);
        posYBox = campoNumerico(posYFieldX, posYFieldY, fieldW, fieldH, fmt1(editPosY), FILTRO_POSICAO, v -> editPosY = v);
        posZBox = campoNumerico(posZFieldX, posZFieldY, fieldW, fieldH, fmt1(editPosZ), FILTRO_POSICAO, v -> editPosZ = v);
        rotXBox = campoNumerico(rotXFieldX, rotXFieldY, fieldW, fieldH, fmt1(editRotX), FILTRO_ROTACAO, v -> editRotX = limitarRotacao(v));
        rotYBox = campoNumerico(rotYFieldX, rotYFieldY, fieldW, fieldH, fmt1(editRotY), FILTRO_ROTACAO, v -> editRotY = limitarRotacao(v));
        rotZBox = campoNumerico(rotZFieldX, rotZFieldY, fieldW, fieldH, fmt1(editRotZ), FILTRO_ROTACAO, v -> editRotZ = limitarRotacao(v));
    }
    @Override
    protected void renderTabContent(GuiGraphics g, int mx, int my) {
        try {
            JogadorNpcFalso fakePlayer = JogadorNpcFalso.getOrCreate(npc);
            if (fakePlayer != null) {
                fakePlayer.syncModelType(editModelType);
                net.minecraft.world.entity.EquipmentSlot[] ordem = {
                        net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                        net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET,
                        net.minecraft.world.entity.EquipmentSlot.MAINHAND, net.minecraft.world.entity.EquipmentSlot.OFFHAND };
                for (int i = 0; i < ordem.length; i++) fakePlayer.setItemSlot(ordem[i], editEquipment[i]);
                g.pose().pushPose();
                g.pose().scale(1f / escala, 1f / escala, 1f);
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        g, Math.round(previewX * escala), Math.round(previewY * escala),
                        Math.round((previewX + previewW) * escala), Math.round((previewY + previewH) * escala),
                        Math.round(30 * escala), 0.0625f, mx * escala, my * escala, fakePlayer);
                g.pose().popPose();
            }
        } catch (Exception ignored) {}


        hoveredSlotId = -1;
        int slotSize = sw(16);
        int[][] equipPos = {
                {sx(145), sy(43)}, {sx(145), sy(61)}, {sx(145), sy(79)}, {sx(145), sy(97)},
                {sx(220), sy(97)}, {sx(220), sy(79)}
        };
        for (int i = 0; i < 6; i++)
            renderEquipSlot(g, editEquipment[i], equipPos[i][0], equipPos[i][1], mx, my, 1000 + i, slotSize);


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
    protected boolean aoClicar(double mx, double my, int btn) {
        int imx = (int) mx, imy = (int) my;


        if (handleBaseClick(imx, imy, btn)) return true;


        if (checkMinusPlusClick(imx, imy, scaleBtnMinusXx, scaleBtnMinusXy, scaleBtnMinusXw, scaleBtnMinusXh, scaleBtnPlusXx, scaleBtnPlusXy, scaleBtnPlusXw, scaleBtnPlusXh, 0.1f, v -> { editScaleX = ajustarEscala(editScaleX, v); if (scaleXBox != null) scaleXBox.setValue(fmt1(editScaleX)); })) return true;
        if (checkMinusPlusClick(imx, imy, scaleBtnMinusYx, scaleBtnMinusYy, scaleBtnMinusYw, scaleBtnMinusYh, scaleBtnPlusYx, scaleBtnPlusYy, scaleBtnPlusYw, scaleBtnPlusYh, 0.1f, v -> { editScaleY = ajustarEscala(editScaleY, v); if (scaleYBox != null) scaleYBox.setValue(fmt1(editScaleY)); })) return true;
        if (checkMinusPlusClick(imx, imy, scaleBtnMinusZx, scaleBtnMinusZy, scaleBtnMinusZw, scaleBtnMinusZh, scaleBtnPlusZx, scaleBtnPlusZy, scaleBtnPlusZw, scaleBtnPlusZh, 0.1f, v -> { editScaleZ = ajustarEscala(editScaleZ, v); if (scaleZBox != null) scaleZBox.setValue(fmt1(editScaleZ)); })) return true;


        if (checkMinusPlusClick(imx, imy, posBtnMinusXx, posBtnMinusXy, posBtnMinusXw, posBtnMinusXh, posBtnPlusXx, posBtnPlusXy, posBtnPlusXw, posBtnPlusXh, 0.5f, v -> { editPosX = ajustarPosicao(editPosX, v); if (posXBox != null) posXBox.setValue(fmt1(editPosX)); })) return true;
        if (checkMinusPlusClick(imx, imy, posBtnMinusYx, posBtnMinusYy, posBtnMinusYw, posBtnMinusYh, posBtnPlusYx, posBtnPlusYy, posBtnPlusYw, posBtnPlusYh, 0.5f, v -> { editPosY = ajustarPosicao(editPosY, v); if (posYBox != null) posYBox.setValue(fmt1(editPosY)); })) return true;
        if (checkMinusPlusClick(imx, imy, posBtnMinusZx, posBtnMinusZy, posBtnMinusZw, posBtnMinusZh, posBtnPlusZx, posBtnPlusZy, posBtnPlusZw, posBtnPlusZh, 0.5f, v -> { editPosZ = ajustarPosicao(editPosZ, v); if (posZBox != null) posZBox.setValue(fmt1(editPosZ)); })) return true;


        if (checkMinusPlusClick(imx, imy, rotBtnMinusXx, rotBtnMinusXy, rotBtnMinusXw, rotBtnMinusXh, rotBtnPlusXx, rotBtnPlusXy, rotBtnPlusXw, rotBtnPlusXh, 5f, v -> { editRotX = ajustarRotacao(editRotX, v); if (rotXBox != null) rotXBox.setValue(fmt1(editRotX)); })) return true;
        if (checkMinusPlusClick(imx, imy, rotBtnMinusYx, rotBtnMinusYy, rotBtnMinusYw, rotBtnMinusYh, rotBtnPlusYx, rotBtnPlusYy, rotBtnPlusYw, rotBtnPlusYh, 5f, v -> { editRotY = ajustarRotacao(editRotY, v); if (rotYBox != null) rotYBox.setValue(fmt1(editRotY)); })) return true;
        if (checkMinusPlusClick(imx, imy, rotBtnMinusZx, rotBtnMinusZy, rotBtnMinusZw, rotBtnMinusZh, rotBtnPlusZx, rotBtnPlusZy, rotBtnPlusZw, rotBtnPlusZh, 5f, v -> { editRotZ = ajustarRotacao(editRotZ, v); if (rotZBox != null) rotZBox.setValue(fmt1(editRotZ)); })) return true;


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
        return super.aoClicar(mx, my, btn);
    }

    private void uploadSkin() {
        new Thread(() -> {
            try {
                String path;
                try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
                    org.lwjgl.PointerBuffer filtros = stack.mallocPointer(3);
                    filtros.put(stack.UTF8("*.png")).put(stack.UTF8("*.cpmmodel")).put(stack.UTF8("*.cpmproject")).flip();
                    path = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(
                            "Selecionar Skin", "", filtros, "PNG, CPM Model, CPM Project", false);
                }
                if (path == null) return;
                File file = new File(path);
                String nome = file.getName().toLowerCase(java.util.Locale.ROOT);
                if (!file.exists()) return;
                if (nome.endsWith(".cpmmodel") || nome.endsWith(".cpmproject")) {
                    uploadCpm(file, nome.endsWith(".cpmproject"));
                    return;
                }
                if (file.length() >= 65536L) {
                    avisar(Component.translatable("gui.aelysiummod.npc.arquivo_muito_grande_maximo_64_kb"));
                    return;
                }
                byte[] data = Files.readAllBytes(file.toPath());
                Minecraft.getInstance().execute(() ->
                        PacketDistributor.sendToServer(
                                new PacotesNpc.EnviarSkinNpcC2S(npc.getId(), data, editModelType)));
            } catch (Exception e) { e.printStackTrace(); }
        }, "FileUpload").start();
    }

    private void avisar(Component mensagem) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(mensagem, true);
        });
    }

    private void uploadCpm(File file, boolean projeto) {
        if (!net.neoforged.fml.ModList.get().isLoaded("cpm")) {
            avisar(Component.translatable("gui.aelysiummod.npc.cpm_nao_instalado"));
            return;
        }
        Minecraft.getInstance().execute(() -> {
            try {
                byte[] dados = projeto ? CompatCpmNpc.converterProjeto(file) : Files.readAllBytes(file.toPath());
                if (dados.length == 0 || (dados[0] & 0xFF) != PacotesNpc.PRIMEIRO_BYTE_CPM) {
                    avisar(Component.translatable("gui.aelysiummod.npc.cpm_arquivo_invalido"));
                    return;
                }
                if (dados.length > PacotesNpc.TAMANHO_MAXIMO_CPM) {
                    avisar(Component.translatable("gui.aelysiummod.npc.cpm_arquivo_muito_grande"));
                    return;
                }
                EnvioSkinNpc.enviarCpm(npc.getId(), dados);
            } catch (Throwable e) {
                e.printStackTrace();
                avisar(Component.translatableEscape("gui.aelysiummod.npc.cpm_erro_conversao", String.valueOf(e.getMessage())));
            }
        });
    }

    private void syncFakePlayerModel() {
        JogadorNpcFalso fakePlayer = JogadorNpcFalso.getOrCreate(npc);
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