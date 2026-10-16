package net.aelysium.aelysiummod.sistemas.social.npc;

import net.aelysium.aelysiummod.registro.ModMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;

public class MenuMercadorNpc extends MerchantMenu {

    private static final int SLOT_PAGAMENTO_1 = 0;
    private static final int SLOT_PAGAMENTO_2 = 1;
    private static final int SLOT_RESULTADO = 2;
    private static final int INVENTARIO_INICIO = 3;
    private static final int INVENTARIO_FIM = 39;

    private final Merchant npcMerchant;

    public MenuMercadorNpc(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ClientSideMerchant(playerInventory.player));
    }

    public MenuMercadorNpc(int containerId, Inventory playerInventory, Merchant merchant) {
        super(containerId, playerInventory, merchant);
        this.npcMerchant = merchant;

        this.tradeContainer = new ContainerTrocaNpc(merchant);
        Slot pagamento1 = this.slots.get(SLOT_PAGAMENTO_1);
        Slot pagamento2 = this.slots.get(SLOT_PAGAMENTO_2);
        Slot resultado = this.slots.get(SLOT_RESULTADO);
        substituirSlot(SLOT_PAGAMENTO_1, new SlotPagamentoNpc(this.tradeContainer, SLOT_PAGAMENTO_1, pagamento1.x, pagamento1.y));
        substituirSlot(SLOT_PAGAMENTO_2, new SlotPagamentoNpc(this.tradeContainer, SLOT_PAGAMENTO_2, pagamento2.x, pagamento2.y));
        substituirSlot(SLOT_RESULTADO, new MerchantResultSlot(playerInventory.player, merchant, this.tradeContainer, SLOT_RESULTADO, resultado.x, resultado.y));
    }

    private void substituirSlot(int indice, Slot slot) {
        slot.index = indice;
        this.slots.set(indice, slot);
    }

    @Override
    public MenuType<?> getType() {
        return ModMenus.MERCADOR_NPC.get();
    }

    @Override
    public void tryMoveItems(int indiceOferta) {
        if (indiceOferta < 0 || this.getOffers().size() <= indiceOferta) return;

        MerchantOffer oferta = this.getOffers().get(indiceOferta);
        ItemCost custoA = oferta.getItemCostA();
        ItemCost custoB = oferta.getItemCostB().orElse(null);

        if (!devolverSeIncompativel(SLOT_PAGAMENTO_1, custoA)) return;
        if (!devolverSeIncompativel(SLOT_PAGAMENTO_2, custoB)) return;

        moverInventarioParaPagamento(SLOT_PAGAMENTO_1, custoA);
        if (custoB != null) moverInventarioParaPagamento(SLOT_PAGAMENTO_2, custoB);
    }

    private boolean devolverSeIncompativel(int slotPagamento, ItemCost custo) {
        ItemStack atual = this.tradeContainer.getItem(slotPagamento);
        if (atual.isEmpty() || (custo != null && custo.test(atual))) return true;

        if (!moverTudo(atual, INVENTARIO_INICIO, INVENTARIO_FIM, true)) return false;
        this.tradeContainer.setItem(slotPagamento, atual);
        return this.tradeContainer.getItem(slotPagamento).isEmpty();
    }

    private void moverInventarioParaPagamento(int slotPagamento, ItemCost custo) {
        int limiteTotal = Math.max(custo.itemStack().getMaxStackSize(), custo.count());
        if (this.tradeContainer.getItem(slotPagamento).getCount() >= limiteTotal) return;

        for (int i = INVENTARIO_INICIO; i < INVENTARIO_FIM; i++) {
            ItemStack doInventario = this.slots.get(i).getItem();
            if (doInventario.isEmpty() || !custo.test(doInventario)) continue;

            ItemStack noPagamento = this.tradeContainer.getItem(slotPagamento);
            if (!noPagamento.isEmpty() && !ItemStack.isSameItemSameComponents(doInventario, noPagamento)) continue;

            int limite = Math.max(doInventario.getMaxStackSize(), custo.count());
            int mover = Math.min(limite - noPagamento.getCount(), doInventario.getCount());
            ItemStack novoPagamento = doInventario.copyWithCount(noPagamento.getCount() + mover);
            doInventario.shrink(mover);
            this.tradeContainer.setItem(slotPagamento, novoPagamento);
            if (novoPagamento.getCount() >= limite) break;
        }
    }

    private boolean moverTudo(ItemStack stack, int inicio, int fim, boolean reverso) {
        boolean moveuAlgo = false;
        while (!stack.isEmpty()) {
            int antes = stack.getCount();
            if (!this.moveItemStackTo(stack, inicio, fim, reverso) || stack.getCount() == antes) break;
            moveuAlgo = true;
        }
        return moveuAlgo;
    }

    private boolean cabeNoInventario(ItemStack stack) {
        int maximo = stack.getMaxStackSize();
        int livre = 0;
        for (int i = INVENTARIO_INICIO; i < INVENTARIO_FIM; i++) {
            ItemStack existente = this.slots.get(i).getItem();
            if (existente.isEmpty()) livre += maximo;
            else if (ItemStack.isSameItemSameComponents(existente, stack)) livre += Math.max(0, maximo - existente.getCount());
            if (livre >= stack.getCount()) return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return itemstack;
        }

        ItemStack itemstack1 = slot.getItem();
        itemstack = itemstack1.copy();

        if (index == 2) {
            if (itemstack1.getCount() > itemstack1.getMaxStackSize() && !cabeNoInventario(itemstack1)) {
                return ItemStack.EMPTY;
            }
            if (!moverTudo(itemstack1, 3, 39, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(itemstack1, itemstack);
            playNpcTradeSound();
        } else if (index != 0 && index != 1) {
            if (!this.moveItemStackTo(itemstack1, SLOT_PAGAMENTO_1, SLOT_RESULTADO, false)) {
                if (index >= 3 && index < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
            return ItemStack.EMPTY;
        }

        if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, itemstack1);

        checkResync(player);

        return itemstack;
    }

    private void checkResync(Player player) {
        if (!(npcMerchant instanceof MercadorNpc npc)) return;
        if (!npc.consumeResync()) return;
        if (!(player instanceof ServerPlayer sp)) return;

        npc.invalidateOffers();
        sp.sendMerchantOffers(
                this.containerId,
                npc.getOffers(),
                0,
                npc.getVillagerXp(),
                npc.showProgressBar(),
                npc.canRestock()
        );
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container inventory) {
        super.slotsChanged(inventory);
        if (npcMerchant instanceof MercadorNpc npc && npc.consumeResync()) {
            Player player = npc.getTradingPlayer();
            if (player instanceof ServerPlayer sp) {
                npc.invalidateOffers();
                sp.sendMerchantOffers(
                        this.containerId,
                        npc.getOffers(),
                        0,
                        npc.getVillagerXp(),
                        npc.showProgressBar(),
                        npc.canRestock()
                );
            }
        }
    }

    private void playNpcTradeSound() {
        if (!(npcMerchant instanceof MercadorNpc npc)) return;
        EntidadeNpcPersonalizado entity = npc.getNpcEntity();
        if (entity == null) return;
        entity.level().playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL,
                0.1F, 1.0F
        );
    }
}
