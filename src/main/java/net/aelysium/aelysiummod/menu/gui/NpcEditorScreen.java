package net.aelysium.aelysiummod.menu.gui;

import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.npc.entity.NpcTradeData;
import net.aelysium.aelysiummod.npc.network.NpcPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class NpcEditorScreen extends Screen {

    static final ResourceLocation TEX_TROCAS =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/gui/npc/trocas.png");
    static final ResourceLocation TEX_APARENCIA =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/gui/npc/aparencia.png");
    static final ResourceLocation TEX_CONFIG =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/gui/npc/configuracoes.png");

    static final int TEX_W = 388;
    static final int TEX_H = 388;
    static final int S = 18;
    static final float GUI_SCALE = 1f;
    static final int TRADES_PER_PAGE = 9;
    static final int MAX_TRADE_PAGES = 10;

    final CustomNpcEntity npc;
    final CompoundTag npcData;

    int currentTab = 0;
    int tradePage = 0;
    List<NpcTradeData> editTrades = new ArrayList<>();

    ItemStack heldStack = ItemStack.EMPTY;
    int heldCount = 0;
    int hoveredSlotId = -1;

    String editName;
    boolean editInvulnerable;
    boolean editLookAtPlayers;
    float editLookRadius;
    String editSkinMode;
    String editModelType;
    float editScaleX, editScaleY, editScaleZ;
    float editPosX, editPosY, editPosZ;
    float editRotX, editRotY, editRotZ;
    boolean editHasGravity;
    boolean editNameVisible;
    final ItemStack[] editEquipment = new ItemStack[6];

    int gX, gY;
    int invX, invY;
    int contentY;

    int tabTrocasX, tabTrocasY, tabTrocasW, tabTrocasH;
    int tabAparenciaX, tabAparenciaY, tabAparenciaW, tabAparenciaH;
    int tabConfigX, tabConfigY, tabConfigW, tabConfigH;

    public NpcEditorScreen(CustomNpcEntity npc, CompoundTag npcData) {
        super(Component.literal("NPC Editor"));
        this.npc = npc;
        this.npcData = npcData;

        this.editName          = npcData.getString("NpcName");
        this.editSkinMode      = npcData.getString("SkinMode");
        this.editModelType     = npcData.getString("ModelType");
        this.editInvulnerable  = npcData.getBoolean("Invulnerable");
        this.editLookAtPlayers = npcData.getBoolean("LookAtPlayers");
        this.editLookRadius    = npcData.getFloat("LookRadius");
        this.editScaleX = npcData.contains("ScaleX") ? npcData.getFloat("ScaleX") : 1.0f;
        this.editScaleY = npcData.contains("ScaleY") ? npcData.getFloat("ScaleY") : 1.0f;
        this.editScaleZ = npcData.contains("ScaleZ") ? npcData.getFloat("ScaleZ") : 1.0f;
        this.editPosX = (float) npc.getX();
        this.editPosY = (float) npc.getY();
        this.editPosZ = (float) npc.getZ();
        this.editRotX = npcData.contains("RotationX") ? npcData.getFloat("RotationX") : 0f;
        this.editRotY = npcData.contains("RotationY") ? npcData.getFloat("RotationY") : 0f;
        this.editRotZ = npcData.contains("RotationZ") ? npcData.getFloat("RotationZ") : 0f;
        this.editHasGravity  = npcData.contains("HasGravity") && npcData.getBoolean("HasGravity");
        this.editNameVisible = !npcData.contains("NameVisible") || npcData.getBoolean("NameVisible");

        for (int i = 0; i < editEquipment.length; i++) editEquipment[i] = ItemStack.EMPTY;
        if (npcData.contains("Equipment")) {
            ListTag equipTag = npcData.getList("Equipment", 10);
            for (int i = 0; i < equipTag.size(); i++) {
                CompoundTag slotTag = equipTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                if (slot >= 0 && slot < editEquipment.length && slotTag.contains("Item")) {
                    editEquipment[slot] = ItemStack.parse(
                            Minecraft.getInstance().level.registryAccess(),
                            slotTag.getCompound("Item")).orElse(ItemStack.EMPTY);
                }
            }
        }
        if (npcData.contains("Trades")) {
            ListTag tradesTag = npcData.getList("Trades", 10);
            for (int i = 0; i < tradesTag.size(); i++) {
                editTrades.add(NpcTradeData.load(tradesTag.getCompound(i),
                        Minecraft.getInstance().level.registryAccess()));
            }
        }
        while (editTrades.size() < TRADES_PER_PAGE) editTrades.add(new NpcTradeData());
    }

    @Override
    protected void init() {
        super.init();

        gX = (this.width - Math.round(253 * GUI_SCALE)) / 2;
        gY = (this.height - Math.round(277 * GUI_SCALE)) / 2;

        contentY = sy(22);
        invX = sx(47);
        invY = sy(190);

        tabTrocasX    = sx(1);   tabTrocasY    = sy(1); tabTrocasW    = sw(83); tabTrocasH    = sw(19);
        tabAparenciaX = sx(85);  tabAparenciaY = sy(1); tabAparenciaW = sw(82); tabAparenciaH = sw(19);
        tabConfigX    = sx(169); tabConfigY    = sy(1); tabConfigW    = sw(82); tabConfigH    = sw(19);

        clearWidgets();
        initCurrentTab();
    }

    protected void initCurrentTab() {
    }

    void switchTab(int tab) {
        Screen next = switch (tab) {
            case 0 -> new NpcTradesTab(npc, npcData, this);
            case 1 -> new NpcAppearanceTab(npc, npcData, this);
            case 2 -> new NpcSettingsTab(npc, npcData, this);
            default -> this;
        };
        if (next != this) {
            copyStateTo((NpcEditorScreen) next);
            Minecraft.getInstance().setScreen(next);
        }
    }

    void copyStateTo(NpcEditorScreen target) {
        target.editName          = this.editName;
        target.editSkinMode      = this.editSkinMode;
        target.editModelType     = this.editModelType;
        target.editInvulnerable  = this.editInvulnerable;
        target.editLookAtPlayers = this.editLookAtPlayers;
        target.editLookRadius    = this.editLookRadius;
        target.editScaleX        = this.editScaleX;
        target.editScaleY        = this.editScaleY;
        target.editScaleZ        = this.editScaleZ;
        target.editPosX          = this.editPosX;
        target.editPosY          = this.editPosY;
        target.editPosZ          = this.editPosZ;
        target.editRotX          = this.editRotX;
        target.editRotY          = this.editRotY;
        target.editRotZ          = this.editRotZ;
        target.editHasGravity    = this.editHasGravity;
        target.editNameVisible   = this.editNameVisible;
        target.tradePage         = this.tradePage;
        target.editTrades        = this.editTrades;
        target.heldStack         = this.heldStack;
        target.heldCount         = this.heldCount;
        for (int i = 0; i < editEquipment.length; i++)
            target.editEquipment[i] = this.editEquipment[i].copy();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        renderGuiPanel(g, mx, my);
        renderTabContent(g, mx, my);
        renderPlayerInventory(g, mx, my);
        renderTabButtons(g, mx, my);
        super.render(g, mx, my, pt);

        if (!heldStack.isEmpty()) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 300);
            g.renderItem(heldStack, mx - 8, my - 8);
            if (heldCount > 1) {
                String cs = String.valueOf(heldCount);
                g.drawString(font, cs, mx - 8 + 17 - font.width(cs), my, 0xFFFFFF);
            }
            g.pose().popPose();
        }
        renderAllTooltips(g, mx, my);
    }

    protected void renderTabContent(GuiGraphics g, int mx, int my) {}

    private void renderGuiPanel(GuiGraphics g, int mx, int my) {
        ResourceLocation tex = switch (currentTab) {
            case 1  -> TEX_APARENCIA;
            case 2  -> TEX_CONFIG;
            default -> TEX_TROCAS;
        };
        g.pose().pushPose();
        g.pose().translate(gX, gY, 0);
        g.pose().scale(GUI_SCALE, GUI_SCALE, 1);
        g.blit(tex, 0, 0, 0, 0, 253, TEX_H, TEX_W, TEX_H);
        g.pose().popPose();
    }

    private void renderTabButtons(GuiGraphics g, int mx, int my) {
        int[][] tabs = {
                { tabTrocasX,    tabTrocasY,    tabTrocasW,    tabTrocasH    },
                { tabAparenciaX, tabAparenciaY, tabAparenciaW, tabAparenciaH },
                { tabConfigX,    tabConfigY,    tabConfigW,    tabConfigH    }
        };
        ResourceLocation[] texs = { TEX_TROCAS, TEX_APARENCIA, TEX_CONFIG };
        int[] dstX  = { 1, 85, 169 };
        int[] srcX  = { 256, 256, 256 };
        int[] srcYs = { 3, 24, 45 };

        for (int i = 0; i < 3; i++) {
            boolean hovered = isIn(mx, my, tabs[i][0], tabs[i][1], tabs[i][2], tabs[i][3]);
            if (currentTab == i || hovered) {
                g.pose().pushPose();
                g.pose().translate(gX, gY, 0);
                g.pose().scale(GUI_SCALE, GUI_SCALE, 1);
                g.blit(texs[i], dstX[i], 1, srcX[i], srcYs[i], 84, 18, TEX_W, TEX_H);
                g.pose().popPose();
            }
        }
    }

    void renderPlayerInventory(GuiGraphics g, int mx, int my) {
        Inventory inv = Minecraft.getInstance().player.getInventory();
        int slotStep = sw(S);
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                renderInvSlot(g, inv.getItem(9 + row * 9 + col),
                        invX + col * slotStep, invY + row * slotStep, mx, my);

        int hbY = sy(244);
        for (int col = 0; col < 9; col++)
            renderInvSlot(g, inv.getItem(col), invX + col * slotStep, hbY, mx, my);
    }

    void renderInvSlot(GuiGraphics g, ItemStack stack, int x, int y, int mx, int my) {
        int slotW = sw(16);
        boolean hov = isIn(mx, my, x, y, slotW, slotW);
        if (hov) g.fill(x, y, x + slotW, y + slotW, 0x44FFFFFF);
        if (!stack.isEmpty()) {
            g.renderItem(stack, x, y);
            if (stack.getCount() > 1) {
                g.pose().pushPose();
                g.pose().translate(0, 0, 200);
                String cs = String.valueOf(stack.getCount());
                g.drawString(font, cs, x + slotW - font.width(cs), y + 9, 0xFFFFFF);
                g.pose().popPose();
            }
        }
    }

    void renderAllTooltips(GuiGraphics g, int mx, int my) {
        Inventory inv = Minecraft.getInstance().player.getInventory();
        int slotStep = sw(S), slotW = sw(16);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            int sx2 = invX + col * slotStep, sy2 = invY + row * slotStep;
            if (isIn(mx, my, sx2, sy2, slotW, slotW)) {
                ItemStack s = inv.getItem(9 + row * 9 + col);
                if (!s.isEmpty() && heldStack.isEmpty()) g.renderTooltip(font, s, mx, my);
            }
        }
        int hbY = sy(244);
        for (int col = 0; col < 9; col++) {
            int sx2 = invX + col * slotStep;
            if (isIn(mx, my, sx2, hbY, slotW, slotW)) {
                ItemStack s = inv.getItem(col);
                if (!s.isEmpty() && heldStack.isEmpty()) g.renderTooltip(font, s, mx, my);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int imx = (int) mx, imy = (int) my;

        if (isIn(imx, imy, tabTrocasX,    tabTrocasY,    tabTrocasW,    tabTrocasH))    { switchTab(0); return true; }
        if (isIn(imx, imy, tabAparenciaX, tabAparenciaY, tabAparenciaW, tabAparenciaH)) { switchTab(1); return true; }
        if (isIn(imx, imy, tabConfigX,    tabConfigY,    tabConfigW,    tabConfigH))    { switchTab(2); return true; }

        if (handleInventoryClick(imx, imy, btn)) return true;

        return super.mouseClicked(mx, my, btn);
    }

    protected boolean handleBaseClick(int imx, int imy, int btn) {
        if (isIn(imx, imy, tabTrocasX,    tabTrocasY,    tabTrocasW,    tabTrocasH))    { switchTab(0); return true; }
        if (isIn(imx, imy, tabAparenciaX, tabAparenciaY, tabAparenciaW, tabAparenciaH)) { switchTab(1); return true; }
        if (isIn(imx, imy, tabConfigX,    tabConfigY,    tabConfigW,    tabConfigH))    { switchTab(2); return true; }
        if (handleInventoryClick(imx, imy, btn)) return true;
        return false;
    }

    boolean handleInventoryClick(int mx, int my, int btn) {
        if (btn == 0) {
            ItemStack invItem = getInvItemAt(mx, my);
            if (invItem != null && !invItem.isEmpty() && heldStack.isEmpty()) {
                heldStack = invItem.copy(); heldCount = invItem.getCount(); return true;
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        saveAll();
        super.onClose();
    }

    void saveAll() {
        PacketDistributor.sendToServer(new NpcPackets.UpdateNpcSettingsC2S(
                npc.getId(), editName, editInvulnerable,
                editLookAtPlayers, editLookRadius, editSkinMode, editModelType,
                editScaleX, editScaleY, editScaleZ,
                editPosX, editPosY, editPosZ,
                editRotX, editRotY, editRotZ,
                editHasGravity, editNameVisible));

        CompoundTag td = new CompoundTag();
        ListTag tl = new ListTag();
        for (NpcTradeData t : editTrades) {
            if (t.isValid()) {
                if (t.hasStockLimit()) {
                    t.setCurrentStock(t.getMaxStock());
                }
                t.setCooldownEndTick(-1);
                tl.add(t.save(Minecraft.getInstance().level.registryAccess()));
            }
        }
        td.put("Trades", tl);
        PacketDistributor.sendToServer(new NpcPackets.UpdateNpcTradesC2S(npc.getId(), td));

        CompoundTag equipData = new CompoundTag();
        ListTag equipList = new ListTag();
        for (int i = 0; i < editEquipment.length; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("Slot", i);
            if (!editEquipment[i].isEmpty())
                slotTag.put("Item", editEquipment[i].save(Minecraft.getInstance().level.registryAccess()));
            equipList.add(slotTag);
        }
        equipData.put("Equipment", equipList);
        PacketDistributor.sendToServer(new NpcPackets.UpdateNpcEquipmentC2S(npc.getId(), equipData));
    }

    int sx(int designOffsetX) { return gX + Math.round(designOffsetX * GUI_SCALE); }
    int sy(int designOffsetY) { return gY + Math.round(designOffsetY * GUI_SCALE); }
    int sw(int designW)       { return Math.max(1, Math.round(designW * GUI_SCALE)); }

    String fmtF(float v) { return String.format("%.2f", v); }

    boolean isIn(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    EditBox addEditBox(int x, int y, int w, int h, String val,
                       java.util.function.Consumer<String> responder) {
        EditBox box = new EditBox(this.font, x, y, w, h, Component.literal(""));
        box.setMaxLength(32);
        box.setValue(val);
        box.setResponder(responder);
        addRenderableWidget(box);
        return box;
    }

    boolean checkMinusPlusClick(int mx, int my,
                                int mx1, int my1, int mw, int mh,
                                int px,  int py,  int pw, int ph,
                                float step, java.util.function.Consumer<Float> apply) {
        if (isIn(mx, my, mx1, my1, mw, mh)) { apply.accept(-step); return true; }
        if (isIn(mx, my, px,  py,  pw, ph)) { apply.accept( step); return true; }
        return false;
    }

    ItemStack getInvItemAt(int mx, int my) {
        Inventory inv = Minecraft.getInstance().player.getInventory();
        int slotStep = sw(S), slotW = sw(16);
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) {
            int sx2 = invX + c * slotStep, sy2 = invY + r * slotStep;
            if (isIn(mx, my, sx2, sy2, slotW, slotW)) return inv.getItem(9 + r * 9 + c);
        }
        int hbY = sy(244);
        for (int c = 0; c < 9; c++) {
            int sx2 = invX + c * slotStep;
            if (isIn(mx, my, sx2, hbY, slotW, slotW)) return inv.getItem(c);
        }
        return null;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public boolean keyPressed(int kc, int sc, int mod) {
        if (anyFieldFocused()) {
            if (kc == 256) { setFocused(null); return true; }
            return super.keyPressed(kc, sc, mod);
        }
        return super.keyPressed(kc, sc, mod);
    }

    protected boolean anyFieldFocused() { return false; }

    @OnlyIn(Dist.CLIENT)
    public static class ConfirmRemoveScreen extends Screen {
        private final Screen parent;
        private final CustomNpcEntity npc;

        public ConfirmRemoveScreen(Screen parent, CustomNpcEntity npc) {
            super(Component.literal("Confirmar"));
            this.parent = parent;
            this.npc    = npc;
        }

        @Override
        protected void init() {
            int cx = width / 2, cy = height / 2;
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                    Component.literal("§c§lSim"), b -> {
                        PacketDistributor.sendToServer(new NpcPackets.RemoveNpcC2S(npc.getId()));
                        Minecraft.getInstance().setScreen(null);
                    }).bounds(cx - 55, cy + 10, 50, 20).build());
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                    Component.literal("§aNao"),
                    b -> Minecraft.getInstance().setScreen(parent)
            ).bounds(cx + 5, cy + 10, 50, 20).build());
        }

        @Override
        public void render(GuiGraphics g, int mx, int my, float pt) {
            renderBackground(g, mx, my, pt);
            g.fill(width / 2 - 100, height / 2 - 20, width / 2 + 100, height / 2 + 38, 0xDD000000);
            g.drawCenteredString(font, "§cRemover este NPC?", width / 2, height / 2 - 12, 0xFFFFFF);
            super.render(g, mx, my, pt);
        }

        @Override public boolean isPauseScreen() { return false; }
    }
}