package net.aelysium.aelysiummod.menu;

import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.menu.gui.NpcMerchant;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;

public class NpcMerchantMenu extends MerchantMenu {

    private final Merchant npcMerchant;

    public NpcMerchantMenu(int containerId, Inventory playerInventory, Merchant merchant) {
        super(containerId, playerInventory, merchant);
        this.npcMerchant = merchant;
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
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(itemstack1, itemstack);
            playNpcTradeSound();
        } else if (index != 0 && index != 1) {
            if (index >= 3 && index < 30) {
                if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                return ItemStack.EMPTY;
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
        if (!(npcMerchant instanceof NpcMerchant npc)) return;
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
        if (npcMerchant instanceof NpcMerchant npc && npc.consumeResync()) {
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
        if (!(npcMerchant instanceof NpcMerchant npc)) return;
        CustomNpcEntity entity = npc.getNpcEntity();
        if (entity == null) return;
        entity.level().playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL,
                0.1F, 1.0F
        );
    }
}