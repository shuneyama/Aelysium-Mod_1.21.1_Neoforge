package net.aelysium.aelysiummod.npc.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class NpcTradeData {
    private ItemStack costItem1 = ItemStack.EMPTY;
    private int costItem1Count = 0;
    private ItemStack costItem2 = ItemStack.EMPTY;
    private int costItem2Count = 0;
    private ItemStack resultItem = ItemStack.EMPTY;
    private int resultItemCount = 0;

    private boolean hasStockLimit = false;
    private int maxStock = 1;
    private int currentStock = 1;
    private boolean hasCooldown = false;
    private int cooldownSeconds = 300;
    private long cooldownEndTick = -1;
    private int totalTimesSold = 0;

    public NpcTradeData() {}

    public NpcTradeData(ItemStack cost1, int cost1Count, ItemStack cost2, int cost2Count,
                        ItemStack result, int resultCount) {
        this.costItem1 = cost1.copy();
        this.costItem1Count = cost1Count;
        this.costItem2 = cost2.copy();
        this.costItem2Count = cost2Count;
        this.resultItem = result.copy();
        this.resultItemCount = resultCount;
    }

    public boolean isValid() {
        return !costItem1.isEmpty() && costItem1Count > 0 && !resultItem.isEmpty() && resultItemCount > 0;
    }

    public boolean isOutOfStock() {
        return hasStockLimit && currentStock <= 0;
    }

    public boolean isCooldownActive(long currentTick) {
        return isOutOfStock() && hasCooldown && cooldownEndTick > currentTick;
    }

    public long getRemainingCooldownTicks(long currentTick) {
        if (!isCooldownActive(currentTick)) return 0;
        return cooldownEndTick - currentTick;
    }

    public boolean canSell(long currentTick) {
        if (!isValid()) return false;
        if (isOutOfStock()) return false;
        if (isCooldownActive(currentTick)) return false;
        return true;
    }

    public void executeSale(long currentTick) {
        totalTimesSold++;
        if (hasStockLimit) {
            currentStock--;
            if (currentStock <= 0 && hasCooldown) {
                cooldownEndTick = currentTick + (long) cooldownSeconds * 20L;
            }
        }
    }

    public void checkAndRestock(long currentTick) {
        if (hasStockLimit && currentStock <= 0 && hasCooldown && cooldownEndTick > 0 && currentTick >= cooldownEndTick) {
            currentStock = maxStock;
            cooldownEndTick = -1;
        }
    }

    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (!costItem1.isEmpty()) {
            tag.put("CostItem1", costItem1.save(provider));
        }
        tag.putInt("CostItem1Count", costItem1Count);
        if (!costItem2.isEmpty()) {
            tag.put("CostItem2", costItem2.save(provider));
        }
        tag.putInt("CostItem2Count", costItem2Count);
        if (!resultItem.isEmpty()) {
            tag.put("ResultItem", resultItem.save(provider));
        }
        tag.putInt("ResultItemCount", resultItemCount);
        tag.putBoolean("HasStockLimit", hasStockLimit);
        tag.putInt("MaxStock", maxStock);
        tag.putInt("CurrentStock", currentStock);
        tag.putBoolean("HasCooldown", hasCooldown);
        tag.putInt("CooldownSeconds", cooldownSeconds);
        tag.putLong("CooldownEndTick", cooldownEndTick);
        tag.putInt("TotalTimesSold", totalTimesSold);
        return tag;
    }

    public static NpcTradeData load(CompoundTag tag, HolderLookup.Provider provider) {
        NpcTradeData data = new NpcTradeData();
        if (tag.contains("CostItem1")) {
            data.costItem1 = ItemStack.parse(provider, tag.getCompound("CostItem1")).orElse(ItemStack.EMPTY);
        }
        data.costItem1Count = tag.getInt("CostItem1Count");
        if (tag.contains("CostItem2")) {
            data.costItem2 = ItemStack.parse(provider, tag.getCompound("CostItem2")).orElse(ItemStack.EMPTY);
        }
        data.costItem2Count = tag.getInt("CostItem2Count");
        if (tag.contains("ResultItem")) {
            data.resultItem = ItemStack.parse(provider, tag.getCompound("ResultItem")).orElse(ItemStack.EMPTY);
        }
        data.resultItemCount = tag.getInt("ResultItemCount");
        data.hasStockLimit = tag.getBoolean("HasStockLimit");
        data.maxStock = tag.getInt("MaxStock");
        data.currentStock = tag.getInt("CurrentStock");
        data.hasCooldown = tag.getBoolean("HasCooldown");
        data.cooldownSeconds = tag.getInt("CooldownSeconds");
        data.cooldownEndTick = tag.getLong("CooldownEndTick");
        data.totalTimesSold = tag.getInt("TotalTimesSold");

        if (data.hasStockLimit && data.currentStock > data.maxStock) {
            data.currentStock = data.maxStock;
        }

        return data;
    }

    public ItemStack getCostItem1() { return costItem1; }
    public void setCostItem1(ItemStack item) { this.costItem1 = item.copy(); }
    public int getCostItem1Count() { return costItem1Count; }
    public void setCostItem1Count(int count) { this.costItem1Count = count; }

    public ItemStack getCostItem2() { return costItem2; }
    public void setCostItem2(ItemStack item) { this.costItem2 = item.copy(); }
    public int getCostItem2Count() { return costItem2Count; }
    public void setCostItem2Count(int count) { this.costItem2Count = count; }

    public ItemStack getResultItem() { return resultItem; }
    public void setResultItem(ItemStack item) { this.resultItem = item.copy(); }
    public int getResultItemCount() { return resultItemCount; }
    public void setResultItemCount(int count) { this.resultItemCount = count; }

    public boolean hasStockLimit() { return hasStockLimit; }
    public void setHasStockLimit(boolean val) {
        this.hasStockLimit = val;
        if (val && this.currentStock > this.maxStock) {
            this.currentStock = this.maxStock;
        }
    }

    public int getMaxStock() { return maxStock; }
    public void setMaxStock(int val) {
        this.maxStock = val;
        if (this.currentStock > val) {
            this.currentStock = val;
        }
    }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int val) { this.currentStock = val; }

    public boolean hasCooldown() { return hasCooldown; }
    public void setHasCooldown(boolean val) { this.hasCooldown = val; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int val) { this.cooldownSeconds = val; }
    public long getCooldownEndTick() { return cooldownEndTick; }
    public void setCooldownEndTick(long val) { this.cooldownEndTick = val; }

    public int getTotalTimesSold() { return totalTimesSold; }
    public void setTotalTimesSold(int val) { this.totalTimesSold = val; }

    public NpcTradeData copy() {
        NpcTradeData copy = new NpcTradeData();
        copy.costItem1 = this.costItem1.copy();
        copy.costItem1Count = this.costItem1Count;
        copy.costItem2 = this.costItem2.copy();
        copy.costItem2Count = this.costItem2Count;
        copy.resultItem = this.resultItem.copy();
        copy.resultItemCount = this.resultItemCount;
        copy.hasStockLimit = this.hasStockLimit;
        copy.maxStock = this.maxStock;
        copy.currentStock = this.currentStock;
        copy.hasCooldown = this.hasCooldown;
        copy.cooldownSeconds = this.cooldownSeconds;
        copy.cooldownEndTick = this.cooldownEndTick;
        copy.totalTimesSold = this.totalTimesSold;
        return copy;
    }
}