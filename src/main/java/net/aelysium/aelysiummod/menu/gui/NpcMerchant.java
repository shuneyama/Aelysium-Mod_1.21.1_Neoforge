package net.aelysium.aelysiummod.menu.gui;

import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.npc.entity.NpcTradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NpcMerchant implements Merchant {

    private final CustomNpcEntity npc;
    private final ServerPlayer customer;
    private MerchantOffers offers;
    private final List<Integer> offerToTradeIndex = new ArrayList<>();
    private boolean needsResync = false;

    public NpcMerchant(CustomNpcEntity npc, ServerPlayer customer) {
        this.npc = npc;
        this.customer = customer;
    }

    public CustomNpcEntity getNpcEntity() {
        return npc;
    }

    public boolean consumeResync() {
        if (needsResync) {
            needsResync = false;
            return true;
        }
        return false;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return customer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (offers == null) {
            offers = buildOffers();
        }
        return offers;
    }

    public void invalidateOffers() {
        offers = null;
    }

    private MerchantOffers buildOffers() {
        MerchantOffers merchantOffers = new MerchantOffers();
        offerToTradeIndex.clear();

        List<NpcTradeData> trades = npc.getTrades();
        long currentTick = npc.level().getGameTime();

        for (int i = 0; i < trades.size(); i++) {
            NpcTradeData trade = trades.get(i);
            if (!trade.isValid()) continue;

            trade.checkAndRestock(currentTick);

            ItemCost itemCost1 = new ItemCost(trade.getCostItem1().getItem(), trade.getCostItem1Count());

            Optional<ItemCost> cost2 = Optional.empty();
            if (!trade.getCostItem2().isEmpty() && trade.getCostItem2Count() > 0) {
                cost2 = Optional.of(new ItemCost(trade.getCostItem2().getItem(), trade.getCostItem2Count()));
            }

            ItemStack resultDisplay = trade.getResultItem().copy();
            resultDisplay.setCount(trade.getResultItemCount());

            int maxUses;
            int currentUses;

            if (trade.hasStockLimit()) {
                maxUses = trade.getMaxStock();
                currentUses = Math.max(0, trade.getMaxStock() - trade.getCurrentStock());
            } else {
                maxUses = Integer.MAX_VALUE;
                currentUses = 0;
            }

            MerchantOffer offer = new MerchantOffer(
                    itemCost1,
                    cost2,
                    resultDisplay,
                    currentUses,
                    maxUses,
                    0,
                    1.0f
            );

            if (trade.isOutOfStock() || trade.isCooldownActive(currentTick)) {
                while (offer.getUses() < offer.getMaxUses()) {
                    offer.increaseUses();
                }
            }

            merchantOffers.add(offer);
            offerToTradeIndex.add(i);
        }
        return merchantOffers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        MerchantOffers currentOffers = getOffers();
        int offerIndex = -1;
        for (int i = 0; i < currentOffers.size(); i++) {
            if (currentOffers.get(i) == offer) {
                offerIndex = i;
                break;
            }
        }

        if (offerIndex < 0 || offerIndex >= offerToTradeIndex.size()) return;

        int tradeIndex = offerToTradeIndex.get(offerIndex);
        List<NpcTradeData> trades = npc.getTrades();
        if (tradeIndex >= trades.size()) return;

        NpcTradeData trade = trades.get(tradeIndex);
        long currentTick = npc.level().getGameTime();

        if (!trade.canSell(currentTick)) {
            while (offer.getUses() < offer.getMaxUses()) {
                offer.increaseUses();
            }
            needsResync = true;
            return;
        }

        trade.executeSale(currentTick);

        if (trade.hasStockLimit() && trade.isOutOfStock()) {
            while (offer.getUses() < offer.getMaxUses()) {
                offer.increaseUses();
            }
            needsResync = true;
        }

        String itemName = trade.getResultItem().getHoverName().getString();
        npc.logTrade(itemName);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    public void openTradingScreen(ServerPlayer player, Component displayName, int level) {
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (containerId, playerInv, p) ->
                        new net.aelysium.aelysiummod.menu.NpcMerchantMenu(containerId, playerInv, this),
                displayName
        ));
        player.sendMerchantOffers(player.containerMenu.containerId, getOffers(),
                level, getVillagerXp(), showProgressBar(), canRestock());
    }

    @Override
    public boolean canRestock() {
        return false;
    }
}