package net.aelysium.aelysiummod.menu.gui;

import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.npc.entity.NpcTradeData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class NpcTradesTab extends NpcEditorScreen {

    private static final int TRADE_ROW_BASE_Y  = 23;
    private static final int TRADE_ROW_STEP    = 18;
    private static final int TRADE_ROW_OFFSET  = 2;

    private static final int STOCK_BTN_OFFSET_Y = 2;
    private static final int CD_BTN_OFFSET_Y    = 2;
    private static final int STOCK_FIELD_OFFSET_Y = 1;
    private static final int CD_FIELD_OFFSET_Y    = 1;

    private static final int STOCK_BTN_X   = 112, STOCK_BTN_W  = 8,  STOCK_BTN_H  = 11;
    private static final int STOCK_FIELD_X = 125, STOCK_FIELD_W = 27, STOCK_FIELD_H = 13;
    private static final int CD_BTN_X      = 157, CD_BTN_W     = 8,  CD_BTN_H     = 11;
    private static final int CD_FIELD_X    = 170, CD_FIELD_W   = 27, CD_FIELD_H   = 13;

    private static final int DEL_BTN_X = 200, DEL_BTN_W = 11, DEL_BTN_H = 11;
    private static final int DEL_BTN_OFFSET_Y = 2;

    private int arrowLeftX, arrowLeftY, arrowLeftW, arrowLeftH;
    private int arrowRightX, arrowRightY, arrowRightW, arrowRightH;

    private final List<EditBox> tradeStockFields    = new ArrayList<>();
    private final List<EditBox> tradeCooldownFields = new ArrayList<>();

    public NpcTradesTab(CustomNpcEntity npc, CompoundTag npcData, NpcEditorScreen origin) {
        super(npc, npcData);
        if (origin != null) origin.copyStateTo(this);
        this.currentTab = 0;
    }

    @Override
    protected void initCurrentTab() {
        arrowLeftX  = sx(30);  arrowLeftY  = sy(169); arrowLeftW  = sw(15); arrowLeftH  = sw(15);
        arrowRightX = sx(209); arrowRightY = sy(169); arrowRightW = sw(15); arrowRightH = sw(15);

        ensureTradePageExists();
        tradeStockFields.clear();
        tradeCooldownFields.clear();

        int startIdx = tradePage * TRADES_PER_PAGE;
        for (int i = 0; i < TRADES_PER_PAGE; i++) {
            int tradeIdx = startIdx + i;
            if (tradeIdx >= editTrades.size()) break;
            NpcTradeData trade = editTrades.get(tradeIdx);
            final int idx = tradeIdx;
            int rowY = tradeRowY(i);

            EditBox sf = new EditBox(this.font,
                    sx(STOCK_FIELD_X), rowY + sw(STOCK_FIELD_OFFSET_Y), sw(STOCK_FIELD_W), sw(STOCK_FIELD_H),
                    Component.literal(""));
            sf.setMaxLength(6);
            sf.setValue(String.valueOf(trade.getMaxStock()));
            sf.setResponder(s -> { try { editTrades.get(idx).setMaxStock(Integer.parseInt(s)); } catch (NumberFormatException ignored) {} });
            sf.setVisible(trade.hasStockLimit());
            addRenderableWidget(sf);
            tradeStockFields.add(sf);

            EditBox cf = new EditBox(this.font,
                    sx(CD_FIELD_X), rowY + sw(CD_FIELD_OFFSET_Y), sw(CD_FIELD_W), sw(CD_FIELD_H),
                    Component.literal(""));
            cf.setMaxLength(6);
            cf.setValue(String.valueOf(trade.getCooldownSeconds()));
            cf.setResponder(s -> { try { editTrades.get(idx).setCooldownSeconds(Integer.parseInt(s)); } catch (NumberFormatException ignored) {} });
            cf.setVisible(trade.hasCooldown());
            addRenderableWidget(cf);
            tradeCooldownFields.add(cf);
        }
    }

    @Override
    protected void renderTabContent(GuiGraphics g, int mx, int my) {
        int startIdx = tradePage * TRADES_PER_PAGE;
        hoveredSlotId = -1;
        int slotSize  = sw(16);
        int stockBtnW = sw(STOCK_BTN_W), stockBtnH = sw(STOCK_BTN_H);
        int cdBtnW    = sw(CD_BTN_W),    cdBtnH    = sw(CD_BTN_H);
        int delBtnW   = sw(DEL_BTN_W),   delBtnH   = sw(DEL_BTN_H);
        int stockBtnX = sx(STOCK_BTN_X);
        int cdBtnX    = sx(CD_BTN_X);
        int delBtnX   = sx(DEL_BTN_X);

        if (isIn(mx, my, arrowLeftX,  arrowLeftY,  arrowLeftW,  arrowLeftH))
            g.fill(arrowLeftX,  arrowLeftY,  arrowLeftX  + arrowLeftW,  arrowLeftY  + arrowLeftH,  0x44FFFFFF);
        if (isIn(mx, my, arrowRightX, arrowRightY, arrowRightW, arrowRightH))
            g.fill(arrowRightX, arrowRightY, arrowRightX + arrowRightW, arrowRightY + arrowRightH, 0x44FFFFFF);

        for (int i = 0; i < TRADES_PER_PAGE; i++) {
            int tradeIdx = startIdx + i;
            if (tradeIdx >= editTrades.size()) break;
            NpcTradeData trade = editTrades.get(tradeIdx);

            int slotAx = sx(55), slotBx = sx(73), slotCx = sx(91);
            int rowY   = tradeRowY(i);
            int stockY = rowY + sw(STOCK_BTN_OFFSET_Y);
            int cdY    = rowY + sw(CD_BTN_OFFSET_Y);
            int delY   = rowY + sw(DEL_BTN_OFFSET_Y);

            renderTradeSlot(g, trade.getCostItem1(),  trade.getCostItem1Count(),  slotAx, rowY, mx, my, tradeIdx * 10,     slotSize);
            renderTradeSlot(g, trade.getCostItem2(),  trade.getCostItem2Count(),  slotBx, rowY, mx, my, tradeIdx * 10 + 1, slotSize);
            renderTradeSlot(g, trade.getResultItem(), trade.getResultItemCount(), slotCx, rowY, mx, my, tradeIdx * 10 + 2, slotSize);

            boolean hovStock = isIn(mx, my, stockBtnX, stockY, stockBtnW, stockBtnH);
            boolean hovCd    = isIn(mx, my, cdBtnX,    cdY,    cdBtnW,    cdBtnH);
            boolean hovDel   = isIn(mx, my, delBtnX,   delY,   delBtnW,  delBtnH);

            g.fill(stockBtnX, stockY, stockBtnX + stockBtnW, stockY + stockBtnH,
                    hovStock ? 0x88FFFFFF : (trade.hasStockLimit() ? 0x8855AA55 : 0x88AA5555));
            g.fill(cdBtnX,    cdY,    cdBtnX    + cdBtnW,    cdY    + cdBtnH,
                    hovCd    ? 0x88FFFFFF : (trade.hasCooldown()   ? 0x8855AA55 : 0x88AA5555));

            if (trade.isValid()) {
                g.fill(delBtnX, delY, delBtnX + delBtnW, delY + delBtnH,
                        hovDel ? 0xCCFF4444 : 0x88AA3333);
                int textX = delBtnX + (13 - font.width("x")) / 2;
                int textY = delY + (delBtnH - 8) / 2;
                g.drawString(font, "x", textX, textY, 0xFFFFFFFF, false);
            }

            if (i < tradeStockFields.size())    tradeStockFields.get(i).setVisible(trade.hasStockLimit());
            if (i < tradeCooldownFields.size()) tradeCooldownFields.get(i).setVisible(trade.hasCooldown());
        }
    }

    @Override
    protected void renderAllTooltips(GuiGraphics g, int mx, int my) {
        super.renderAllTooltips(g, mx, my);
        if (heldStack.isEmpty() && hoveredSlotId >= 0 && hoveredSlotId < 1000) {
            int ti = hoveredSlotId / 10, st = hoveredSlotId % 10;
            if (ti < editTrades.size()) {
                NpcTradeData t = editTrades.get(ti);
                ItemStack s = switch (st) {
                    case 0 -> t.getCostItem1();
                    case 1 -> t.getCostItem2();
                    case 2 -> t.getResultItem();
                    default -> ItemStack.EMPTY;
                };
                if (!s.isEmpty()) g.renderTooltip(font, s, mx, my);
            }
        }

        int startIdx = tradePage * TRADES_PER_PAGE;
        int delBtnX  = sx(DEL_BTN_X);
        int delBtnW  = sw(DEL_BTN_W);
        int delBtnH  = sw(DEL_BTN_H);
        for (int i = 0; i < TRADES_PER_PAGE; i++) {
            int tradeIdx = startIdx + i;
            if (tradeIdx >= editTrades.size()) break;
            NpcTradeData trade = editTrades.get(tradeIdx);
            if (!trade.isValid()) continue;
            int rowY = tradeRowY(i);
            int delY = rowY + sw(DEL_BTN_OFFSET_Y);
            if (isIn(mx, my, delBtnX, delY, delBtnW, delBtnH)) {
                g.renderTooltip(font, Component.literal("§cDeletar venda"), mx, my);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int imx = (int) mx, imy = (int) my;

        if (btn == 0 && !heldStack.isEmpty()) {
            boolean clickedTradeSlot = (hoveredSlotId >= 0 && hoveredSlotId < 1000);

            if (!clickedTradeSlot) {
                heldStack = ItemStack.EMPTY;
                heldCount = 0;
                return true;
            }
        }

        for (EditBox sf : tradeStockFields) {
            if (sf.visible && sf.isMouseOver(mx, my)) {
                return super.mouseClicked(mx, my, btn);
            }
        }
        for (EditBox cf : tradeCooldownFields) {
            if (cf.visible && cf.isMouseOver(mx, my)) {
                return super.mouseClicked(mx, my, btn);
            }
        }

        if (handleBaseClick(imx, imy, btn)) return true;

        if (isIn(imx, imy, arrowLeftX, arrowLeftY, arrowLeftW, arrowLeftH)) {
            if (tradePage > 0) { tradePage--; switchTab(0); }
            return true;
        }
        if (isIn(imx, imy, arrowRightX, arrowRightY, arrowRightW, arrowRightH)) {
            if (tradePage < MAX_TRADE_PAGES - 1) { tradePage++; ensureTradePageExists(); switchTab(0); }
            return true;
        }

        int startIdx  = tradePage * TRADES_PER_PAGE;
        int stockBtnX = sx(STOCK_BTN_X);
        int cdBtnX    = sx(CD_BTN_X);
        int delBtnX   = sx(DEL_BTN_X);
        int stockBtnW = sw(STOCK_BTN_W), stockBtnH = sw(STOCK_BTN_H);
        int cdBtnW    = sw(CD_BTN_W),    cdBtnH    = sw(CD_BTN_H);
        int delBtnW   = sw(DEL_BTN_W),   delBtnH   = sw(DEL_BTN_H);

        for (int i = 0; i < TRADES_PER_PAGE; i++) {
            int tradeIdx = startIdx + i;
            if (tradeIdx >= editTrades.size()) break;
            int rowY   = tradeRowY(i);
            int stockY = rowY + sw(STOCK_BTN_OFFSET_Y);
            int cdY    = rowY + sw(CD_BTN_OFFSET_Y);
            int delY   = rowY + sw(DEL_BTN_OFFSET_Y);
            NpcTradeData trade = editTrades.get(tradeIdx);

            if (btn == 0 && isIn(imx, imy, delBtnX, delY, delBtnW, delBtnH) && trade.isValid()) {
                trade.setCostItem1(ItemStack.EMPTY);
                trade.setCostItem1Count(0);
                trade.setCostItem2(ItemStack.EMPTY);
                trade.setCostItem2Count(0);
                trade.setResultItem(ItemStack.EMPTY);
                trade.setResultItemCount(0);
                trade.setHasStockLimit(false);
                trade.setHasCooldown(false);
                switchTab(0);
                return true;
            }

            if (isIn(imx, imy, stockBtnX, stockY, stockBtnW, stockBtnH)) {
                trade.setHasStockLimit(!trade.hasStockLimit()); switchTab(0); return true;
            }
            if (isIn(imx, imy, cdBtnX, cdY, cdBtnW, cdBtnH)) {
                trade.setHasCooldown(!trade.hasCooldown()); switchTab(0); return true;
            }
        }

        if (btn == 0 && hoveredSlotId >= 0 && hoveredSlotId < 1000) {
            int ti = hoveredSlotId / 10, st = hoveredSlotId % 10;
            if (ti < editTrades.size()) {
                NpcTradeData t = editTrades.get(ti);
                if (!heldStack.isEmpty()) {
                    setTradeSlot(t, st, heldStack, heldCount);
                    heldStack = ItemStack.EMPTY; heldCount = 0; return true;
                } else {
                    ItemStack ex = getTradeSlot(t, st);
                    if (!ex.isEmpty()) {
                        heldStack = ex.copy();
                        heldCount = getTradeSlotCount(t, st);
                        clearTradeSlot(t, st);
                        return true;
                    }
                }
            }
            if (!heldStack.isEmpty()) { heldStack = ItemStack.EMPTY; heldCount = 0; return true; }
        }

        if (btn == 1 && hoveredSlotId >= 0 && hoveredSlotId < 1000) {
            int ti = hoveredSlotId / 10, st = hoveredSlotId % 10;
            if (ti < editTrades.size()) {
                NpcTradeData t = editTrades.get(ti);
                int d = hasShiftDown() ? 10 : 1;
                switch (st) {
                    case 0 -> { if (!t.getCostItem1().isEmpty())  t.setCostItem1Count(t.getCostItem1Count()   + d); }
                    case 1 -> { if (!t.getCostItem2().isEmpty())  t.setCostItem2Count(t.getCostItem2Count()   + d); }
                    case 2 -> { if (!t.getResultItem().isEmpty()) t.setResultItemCount(t.getResultItemCount() + d); }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (hoveredSlotId >= 0 && hoveredSlotId < 1000) {
            int ti = hoveredSlotId / 10, st = hoveredSlotId % 10;
            if (ti < editTrades.size()) {
                NpcTradeData t = editTrades.get(ti);
                int d = (int) Math.signum(sy) * (hasShiftDown() ? 10 : 1);
                switch (st) {
                    case 0 -> { if (!t.getCostItem1().isEmpty())  t.setCostItem1Count(Math.max(1,  t.getCostItem1Count()   + d)); }
                    case 1 -> { if (!t.getCostItem2().isEmpty())  t.setCostItem2Count(Math.max(1,  t.getCostItem2Count()   + d)); }
                    case 2 -> { if (!t.getResultItem().isEmpty()) t.setResultItemCount(Math.max(1, t.getResultItemCount()  + d)); }
                }
                return true;
            }
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    private void setTradeSlot(NpcTradeData t, int st, ItemStack stack, int count) {
        switch (st) {
            case 0 -> { t.setCostItem1(stack);  t.setCostItem1Count(count); }
            case 1 -> { t.setCostItem2(stack);  t.setCostItem2Count(count); }
            case 2 -> { t.setResultItem(stack); t.setResultItemCount(count); }
        }
    }

    private ItemStack getTradeSlot(NpcTradeData t, int st) {
        return switch (st) {
            case 0 -> t.getCostItem1();
            case 1 -> t.getCostItem2();
            case 2 -> t.getResultItem();
            default -> ItemStack.EMPTY;
        };
    }

    private int getTradeSlotCount(NpcTradeData t, int st) {
        return switch (st) {
            case 0 -> t.getCostItem1Count();
            case 1 -> t.getCostItem2Count();
            case 2 -> t.getResultItemCount();
            default -> 1;
        };
    }

    private void clearTradeSlot(NpcTradeData t, int st) {
        switch (st) {
            case 0 -> { t.setCostItem1(ItemStack.EMPTY);  t.setCostItem1Count(0); }
            case 1 -> { t.setCostItem2(ItemStack.EMPTY);  t.setCostItem2Count(0); }
            case 2 -> { t.setResultItem(ItemStack.EMPTY); t.setResultItemCount(0); }
        }
    }

    private void renderTradeSlot(GuiGraphics g, ItemStack stack, int count,
                                 int x, int y, int mx, int my, int slotId, int size) {
        boolean hov = isIn(mx, my, x, y, size, size);
        if (hov) { g.fill(x, y, x + size, y + size, 0x44FFFFFF); hoveredSlotId = slotId; }
        if (!stack.isEmpty()) {
            g.pose().pushPose();
            if (size != 16) {
                float sc = size / 16.0f;
                g.pose().translate(x, y, 0);
                g.pose().scale(sc, sc, 1);
                g.renderItem(stack, 0, 0);
                if (count > 1) {
                    g.pose().translate(0, 0, 200);
                    String cs = String.valueOf(count);
                    g.drawString(font, cs, (int)(16 - font.width(cs)), 9, 0xFFFFFF);
                }
            } else {
                g.renderItem(stack, x, y);
                if (count > 1) {
                    g.pose().translate(0, 0, 200);
                    String cs = String.valueOf(count);
                    g.drawString(font, cs, x + 16 - font.width(cs), y + 9, 0xFFFFFF);
                }
            }
            g.pose().popPose();
        }
    }

    private int tradeRowY(int i) {
        return sy(TRADE_ROW_BASE_Y + i * TRADE_ROW_STEP + TRADE_ROW_OFFSET);
    }

    private void ensureTradePageExists() {
        int needed = (tradePage + 1) * TRADES_PER_PAGE;
        while (editTrades.size() < needed) editTrades.add(new NpcTradeData());
    }

    @Override
    protected boolean anyFieldFocused() {
        for (EditBox b : tradeStockFields)    if (b.isFocused()) return true;
        for (EditBox b : tradeCooldownFields) if (b.isFocused()) return true;
        return false;
    }
}