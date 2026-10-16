package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.DadosTrocaNpc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import java.util.Optional;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import net.aelysium.aelysiummod.sistemas.social.npc.ContainerTrocaNpc;

@OnlyIn(Dist.CLIENT)
public class AbaTrocasNpc extends TelaEditorNpc {

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

    private static final int DEL_BTN_X = 204, DEL_BTN_W = 11, DEL_BTN_H = 11;
    private static final int DEL_BTN_OFFSET_Y = 2;

    private int arrowLeftX, arrowLeftY, arrowLeftW, arrowLeftH;
    private int arrowRightX, arrowRightY, arrowRightW, arrowRightH;

    private String digitosQuantidade = "";
    private int slotDigitos = -1;

    private final List<EditBox> tradeStockFields    = new ArrayList<>();
    private final List<EditBox> tradeCooldownFields = new ArrayList<>();

    public AbaTrocasNpc(EntidadeNpcPersonalizado npc, CompoundTag npcData, TelaEditorNpc origin) {
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
            DadosTrocaNpc trade = editTrades.get(tradeIdx);
            final int idx = tradeIdx;
            int rowY = tradeRowY(i);

            EditBox sf = new CampoNumeroNpc(this.font,
                    sx(STOCK_FIELD_X), rowY + sw(STOCK_FIELD_OFFSET_Y), sw(STOCK_FIELD_W), sw(STOCK_FIELD_H));
            sf.setMaxLength(6);
            sf.setValue(String.valueOf(trade.getMaxStock()));
            sf.setResponder(s -> { try { editTrades.get(idx).setMaxStock(Integer.parseInt(s)); } catch (NumberFormatException ignored) {} });
            sf.setVisible(trade.hasStockLimit());
            addRenderableWidget(sf);
            tradeStockFields.add(sf);

            EditBox cf = new CampoNumeroNpc(this.font,
                    sx(CD_FIELD_X), rowY + sw(CD_FIELD_OFFSET_Y), sw(CD_FIELD_W), sw(CD_FIELD_H));
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
            DadosTrocaNpc trade = editTrades.get(tradeIdx);

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
                net.aelysium.aelysiummod.nucleo.cliente.BotaoExcluir.desenhar(g, delBtnX, delY, delBtnW, delBtnH, hovDel);
            }

            if (i < tradeStockFields.size())    tradeStockFields.get(i).setVisible(trade.hasStockLimit());
            if (i < tradeCooldownFields.size()) tradeCooldownFields.get(i).setVisible(trade.hasCooldown());
        }

        if (hoveredSlotId != slotDigitos) {
            slotDigitos = hoveredSlotId;
            digitosQuantidade = "";
        }
    }

    @Override
    protected void renderAllTooltips(GuiGraphics g, int mx, int my) {
        super.renderAllTooltips(g, mx, my);
        if (hoveredSlotId >= 0 && hoveredSlotId < 1000) {
            int ti = hoveredSlotId / 10, st = hoveredSlotId % 10;
            if (ti < editTrades.size()) {
                ItemStack s = getTradeSlot(editTrades.get(ti), st);
                List<Component> linhas = new ArrayList<>();
                if (!s.isEmpty() && heldStack.isEmpty()) {
                    linhas.addAll(Screen.getTooltipFromItem(Minecraft.getInstance(), s));
                    linhas.add(Component.literal(""));
                    linhas.add(Component.translatable("gui.aelysiummod.npc.botao_esquerdo_copiar_o_item_para_a"));
                    linhas.add(Component.translatable("gui.aelysiummod.npc.botao_direito_remover_o_item"));
                    linhas.add(Component.literal(""));
                    linhas.add(Component.translatable("gui.aelysiummod.npc.scroll_mudar_a_quantidade_shift_10"));
                    linhas.add(Component.translatable("gui.aelysiummod.npc.ou_digite_um_numero_para_mudar_a"));
                } else if (!heldStack.isEmpty()) {
                    linhas.add(Component.translatable("gui.aelysiummod.npc.botao_esquerdo_colocar_1_stack"));
                    linhas.add(Component.translatable("gui.aelysiummod.npc.botao_direito_colocar_1_unidade"));
                } else {
                    linhas.add(Component.translatable("gui.aelysiummod.npc.pegue_um_item_do_inventario_ou_arraste"));
                }
                g.renderTooltip(font, linhas, Optional.empty(), mx, my);
            }
        }

        int startIdx = tradePage * TRADES_PER_PAGE;
        int delBtnX  = sx(DEL_BTN_X);
        int delBtnW  = sw(DEL_BTN_W);
        int delBtnH  = sw(DEL_BTN_H);
        for (int i = 0; i < TRADES_PER_PAGE; i++) {
            int tradeIdx = startIdx + i;
            if (tradeIdx >= editTrades.size()) break;
            DadosTrocaNpc trade = editTrades.get(tradeIdx);
            int rowY = tradeRowY(i);
            int stockY = rowY + sw(STOCK_BTN_OFFSET_Y);
            int cdY    = rowY + sw(CD_BTN_OFFSET_Y);
            if (isIn(mx, my, sx(STOCK_BTN_X), stockY, sw(STOCK_BTN_W), sw(STOCK_BTN_H))) {
                desenharTooltipAtivavel(g, mx, my, "limite_estoque", trade.hasStockLimit());
            }
            if (isIn(mx, my, sx(CD_BTN_X), cdY, sw(CD_BTN_W), sw(CD_BTN_H))) {
                desenharTooltipAtivavel(g, mx, my, "cooldown", trade.hasCooldown());
            }
            if (!trade.isValid()) continue;
            int delY = rowY + sw(DEL_BTN_OFFSET_Y);
            if (isIn(mx, my, delBtnX, delY, delBtnW, delBtnH)) {
                g.renderTooltip(font, Component.translatable("gui.aelysiummod.npc.deletar_venda"), mx, my);
            }
        }
    }

    private void desenharTooltipAtivavel(GuiGraphics g, int mx, int my, String nome, boolean ativo) {
        List<Component> linhas = new ArrayList<>();
        linhas.add(Component.translatable("gui.aelysiummod.npc." + nome));
        linhas.add(Component.translatable("gui.aelysiummod.npc." + nome + "_descricao"));
        linhas.add(Component.translatable(ativo ? "gui.aelysiummod.npc.clique_desativar" : "gui.aelysiummod.npc.clique_ativar"));
        g.renderTooltip(font, linhas, Optional.empty(), mx, my);
    }

    @Override
    protected boolean aoClicar(double mx, double my, int btn) {
        int imx = (int) mx, imy = (int) my;

        for (EditBox sf : tradeStockFields) {
            if (sf.visible && sf.isMouseOver(mx, my)) {
                return super.aoClicar(mx, my, btn);
            }
        }
        for (EditBox cf : tradeCooldownFields) {
            if (cf.visible && cf.isMouseOver(mx, my)) {
                return super.aoClicar(mx, my, btn);
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
            DadosTrocaNpc trade = editTrades.get(tradeIdx);

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

        if (hoveredSlotId >= 0 && hoveredSlotId < 1000) {
            int ti = hoveredSlotId / 10, st = hoveredSlotId % 10;
            if (ti < editTrades.size()) {
                DadosTrocaNpc t = editTrades.get(ti);
                ItemStack atual = getTradeSlot(t, st);

                if (btn == 0) {
                    if (!heldStack.isEmpty()) {
                        setTradeSlot(t, st, heldStack, Math.max(1, heldStack.getMaxStackSize()));
                    } else if (!atual.isEmpty()) {
                        heldStack = atual.copyWithCount(1);
                        heldCount = 1;
                    }
                    return true;
                }
                if (btn == 1) {
                    if (!heldStack.isEmpty()) setTradeSlot(t, st, heldStack, 1);
                    else if (!atual.isEmpty()) clearTradeSlot(t, st);
                    return true;
                }
            }
        }

        if (!heldStack.isEmpty()) {
            soltarMao();
            return true;
        }
        return false;
    }

    @Override
    protected boolean aoRolar(double mx, double my, double sx, double sy) {
        if (hoveredSlotId >= 0 && hoveredSlotId < 1000) {
            int ti = hoveredSlotId / 10, st = hoveredSlotId % 10;
            if (ti < editTrades.size()) {
                DadosTrocaNpc t = editTrades.get(ti);
                if (!getTradeSlot(t, st).isEmpty()) {
                    ajustarQuantidade(t, st, (int) Math.signum(sy) * passoDeQuantidade());
                }
                return true;
            }
        }
        return super.aoRolar(mx, my, sx, sy);
    }

    @Override
    public boolean keyPressed(int kc, int sc, int mod) {
        if (!anyFieldFocused()) {
            if (kc == GLFW.GLFW_KEY_ESCAPE && !heldStack.isEmpty()) {
                soltarMao();
                return true;
            }
            if (hoveredSlotId >= 0 && hoveredSlotId < 1000) {
                int ti = hoveredSlotId / 10, st = hoveredSlotId % 10;
                if (ti < editTrades.size() && !getTradeSlot(editTrades.get(ti), st).isEmpty()) {
                    DadosTrocaNpc t = editTrades.get(ti);
                    int digito = digitoDaTecla(kc);
                    if (digito >= 0) {
                        if (digitosQuantidade.length() < 9) {
                            digitosQuantidade = (digitosQuantidade + digito).replaceFirst("^0+", "");
                        }
                        if (!digitosQuantidade.isEmpty()) definirQuantidade(t, st, Integer.parseInt(digitosQuantidade));
                        return true;
                    }
                    if (kc == GLFW.GLFW_KEY_BACKSPACE && !digitosQuantidade.isEmpty()) {
                        digitosQuantidade = digitosQuantidade.substring(0, digitosQuantidade.length() - 1);
                        definirQuantidade(t, st, digitosQuantidade.isEmpty() ? 1 : Integer.parseInt(digitosQuantidade));
                        return true;
                    }
                }
            }
        }
        return super.keyPressed(kc, sc, mod);
    }

    private static int digitoDaTecla(int kc) {
        if (kc >= GLFW.GLFW_KEY_0 && kc <= GLFW.GLFW_KEY_9) return kc - GLFW.GLFW_KEY_0;
        if (kc >= GLFW.GLFW_KEY_KP_0 && kc <= GLFW.GLFW_KEY_KP_9) return kc - GLFW.GLFW_KEY_KP_0;
        return -1;
    }

    private void soltarMao() {
        heldStack = ItemStack.EMPTY;
        heldCount = 0;
    }

    private int passoDeQuantidade() {
        return hasControlDown() ? 64 : hasShiftDown() ? 10 : 1;
    }

    private void ajustarQuantidade(DadosTrocaNpc t, int st, int delta) {
        digitosQuantidade = "";
        definirQuantidade(t, st, getTradeSlotCount(t, st) + delta);
    }

    private void definirQuantidade(DadosTrocaNpc t, int st, int valor) {
        int v = Math.max(1, Math.min(ContainerTrocaNpc.LIMITE_STACK, valor));
        switch (st) {
            case 0 -> t.setCostItem1Count(v);
            case 1 -> t.setCostItem2Count(v);
            case 2 -> t.setResultItemCount(v);
        }
    }

    private void setTradeSlot(DadosTrocaNpc t, int st, ItemStack stack, int count) {
        switch (st) {
            case 0 -> { t.setCostItem1(stack.copyWithCount(1));  t.setCostItem1Count(count); }
            case 1 -> { t.setCostItem2(stack.copyWithCount(1));  t.setCostItem2Count(count); }
            case 2 -> { t.setResultItem(stack.copyWithCount(1)); t.setResultItemCount(count); }
        }
    }

    private ItemStack getTradeSlot(DadosTrocaNpc t, int st) {
        return switch (st) {
            case 0 -> t.getCostItem1();
            case 1 -> t.getCostItem2();
            case 2 -> t.getResultItem();
            default -> ItemStack.EMPTY;
        };
    }

    private int getTradeSlotCount(DadosTrocaNpc t, int st) {
        return switch (st) {
            case 0 -> t.getCostItem1Count();
            case 1 -> t.getCostItem2Count();
            case 2 -> t.getResultItemCount();
            default -> 1;
        };
    }

    private void clearTradeSlot(DadosTrocaNpc t, int st) {
        switch (st) {
            case 0 -> { t.setCostItem1(ItemStack.EMPTY);  t.setCostItem1Count(0); }
            case 1 -> { t.setCostItem2(ItemStack.EMPTY);  t.setCostItem2Count(0); }
            case 2 -> { t.setResultItem(ItemStack.EMPTY); t.setResultItemCount(0); }
        }
    }

    private void renderTradeSlot(GuiGraphics g, ItemStack stack, int count,
                                 int x, int y, int mx, int my, int slotId, int size) {
        boolean hov = isIn(mx, my, x, y, size, size);
        if (hov) { g.fill(x, y, x + size, y + size, heldStack.isEmpty() ? 0x44FFFFFF : 0x4455FF55); hoveredSlotId = slotId; }
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
                    desenharQuantidade(g, cs, 16, 0);
                }
            } else {
                g.renderItem(stack, x, y);
                if (count > 1) {
                    g.pose().translate(0, 0, 200);
                    String cs = String.valueOf(count);
                    desenharQuantidade(g, cs, x + 16, y);
                }
            }
            g.pose().popPose();
        }
    }

    private void desenharQuantidade(GuiGraphics g, String cs, int direita, int topo) {
        g.pose().pushPose();
        g.pose().translate(direita, topo + 16, 0);
        g.pose().scale(0.8f, 0.8f, 1);
        g.drawString(font, cs, -font.width(cs), -8, 0xFFFFFF);
        g.pose().popPose();
    }

    private int tradeRowY(int i) {
        return sy(TRADE_ROW_BASE_Y + i * TRADE_ROW_STEP + TRADE_ROW_OFFSET);
    }

    private void ensureTradePageExists() {
        int needed = (tradePage + 1) * TRADES_PER_PAGE;
        while (editTrades.size() < needed) editTrades.add(new DadosTrocaNpc());
    }

    @Override
    protected boolean anyFieldFocused() {
        for (EditBox b : tradeStockFields)    if (b.isFocused()) return true;
        for (EditBox b : tradeCooldownFields) if (b.isFocused()) return true;
        return false;
    }
}