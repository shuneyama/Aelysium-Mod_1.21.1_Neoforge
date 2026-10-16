package net.aelysium.aelysiummod.sistemas.social.npc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DadosTrocaNpc {
    private ItemStack costItem1 = ItemStack.EMPTY;
    private int costItem1Count = 0;
    private ItemStack costItem2 = ItemStack.EMPTY;
    private int costItem2Count = 0;
    private ItemStack resultItem = ItemStack.EMPTY;
    private int resultItemCount = 0;

    private boolean hasStockLimit = false;
    private int maxStock = 1;
    private boolean hasCooldown = false;
    private int cooldownSeconds = 300;
    private int totalTimesSold = 0;


    private Map<UUID, Integer> playerStock = new HashMap<>();

    private Map<UUID, Long> playerCooldownEndTick = new HashMap<>();

    public DadosTrocaNpc() {}

    public DadosTrocaNpc(ItemStack cost1, int cost1Count, ItemStack cost2, int cost2Count,
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

    public int getPlayerStock(UUID playerId) {
        return playerStock.getOrDefault(playerId, maxStock);
    }

    public boolean isOutOfStock(UUID playerId) {
        return hasStockLimit && getPlayerStock(playerId) <= 0;
    }

    public boolean isCooldownActive(UUID playerId, long currentTick) {
        if (!isOutOfStock(playerId) || !hasCooldown) return false;
        Long endTick = playerCooldownEndTick.get(playerId);
        return endTick != null && endTick > currentTick;
    }

    public long getRemainingCooldownTicks(UUID playerId, long currentTick) {
        if (!isCooldownActive(playerId, currentTick)) return 0;
        return playerCooldownEndTick.get(playerId) - currentTick;
    }

    public boolean canSell(UUID playerId, long currentTick) {
        if (!isValid()) return false;
        if (isOutOfStock(playerId)) return false;
        if (isCooldownActive(playerId, currentTick)) return false;
        return true;
    }

    public void executeSale(UUID playerId, long currentTick) {
        totalTimesSold++;
        if (hasStockLimit) {
            int stock = getPlayerStock(playerId) - 1;
            playerStock.put(playerId, stock);
            if (stock <= 0 && hasCooldown) {
                playerCooldownEndTick.put(playerId, currentTick + (long) cooldownSeconds * 20L);
            }
        }
    }

    public void checkAndRestock(long currentTick) {
        if (!hasStockLimit || !hasCooldown) return;
        playerCooldownEndTick.entrySet().removeIf(entry -> {
            if (currentTick >= entry.getValue()) {
                playerStock.put(entry.getKey(), maxStock);
                return true;
            }
            return false;
        });
    }

    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (!costItem1.isEmpty()) tag.put("CostItem1", costItem1.save(provider));
        tag.putInt("CostItem1Count", costItem1Count);
        if (!costItem2.isEmpty()) tag.put("CostItem2", costItem2.save(provider));
        tag.putInt("CostItem2Count", costItem2Count);
        if (!resultItem.isEmpty()) tag.put("ResultItem", resultItem.save(provider));
        tag.putInt("ResultItemCount", resultItemCount);
        tag.putBoolean("HasStockLimit", hasStockLimit);
        tag.putInt("MaxStock", maxStock);
        tag.putBoolean("HasCooldown", hasCooldown);
        tag.putInt("CooldownSeconds", cooldownSeconds);
        tag.putInt("TotalTimesSold", totalTimesSold);

        CompoundTag stockTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : playerStock.entrySet()) {
            stockTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("PlayerStock", stockTag);

        CompoundTag cooldownTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : playerCooldownEndTick.entrySet()) {
            cooldownTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("PlayerCooldown", cooldownTag);

        return tag;
    }

    public static DadosTrocaNpc load(CompoundTag tag, HolderLookup.Provider provider) {
        DadosTrocaNpc data = new DadosTrocaNpc();
        if (tag.contains("CostItem1")) data.costItem1 = ItemStack.parse(provider, tag.getCompound("CostItem1")).orElse(ItemStack.EMPTY);
        data.costItem1Count = tag.getInt("CostItem1Count");
        if (tag.contains("CostItem2")) data.costItem2 = ItemStack.parse(provider, tag.getCompound("CostItem2")).orElse(ItemStack.EMPTY);
        data.costItem2Count = tag.getInt("CostItem2Count");
        if (tag.contains("ResultItem")) data.resultItem = ItemStack.parse(provider, tag.getCompound("ResultItem")).orElse(ItemStack.EMPTY);
        data.resultItemCount = tag.getInt("ResultItemCount");
        data.hasStockLimit = tag.getBoolean("HasStockLimit");
        data.maxStock = tag.getInt("MaxStock");
        data.hasCooldown = tag.getBoolean("HasCooldown");
        data.cooldownSeconds = tag.getInt("CooldownSeconds");
        data.totalTimesSold = tag.getInt("TotalTimesSold");

        if (tag.contains("PlayerStock")) {
            CompoundTag stockTag = tag.getCompound("PlayerStock");
            for (String key : stockTag.getAllKeys()) {
                try { data.playerStock.put(UUID.fromString(key), stockTag.getInt(key)); } catch (IllegalArgumentException ignored) {}
            }
        }

        if (tag.contains("PlayerCooldown")) {
            CompoundTag cooldownTag = tag.getCompound("PlayerCooldown");
            for (String key : cooldownTag.getAllKeys()) {
                try { data.playerCooldownEndTick.put(UUID.fromString(key), cooldownTag.getLong(key)); } catch (IllegalArgumentException ignored) {}
            }
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
        if (val) playerStock.replaceAll((uuid, stock) -> Math.min(stock, maxStock));
    }

    public int getMaxStock() { return maxStock; }
    public void setMaxStock(int val) {
        this.maxStock = val;
        playerStock.replaceAll((uuid, stock) -> Math.min(stock, val));
    }


    public void setCurrentStock(int val) {
        playerStock.clear();
    }


    public void setCooldownEndTick(long val) {
        if (val == -1) playerCooldownEndTick.clear();
    }

    public boolean hasCooldown() { return hasCooldown; }
    public void setHasCooldown(boolean val) { this.hasCooldown = val; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int val) { this.cooldownSeconds = val; }

    public int getTotalTimesSold() { return totalTimesSold; }
    public void setTotalTimesSold(int val) { this.totalTimesSold = val; }

    public DadosTrocaNpc copy() {
        DadosTrocaNpc copy = new DadosTrocaNpc();
        copy.costItem1 = this.costItem1.copy();
        copy.costItem1Count = this.costItem1Count;
        copy.costItem2 = this.costItem2.copy();
        copy.costItem2Count = this.costItem2Count;
        copy.resultItem = this.resultItem.copy();
        copy.resultItemCount = this.resultItemCount;
        copy.hasStockLimit = this.hasStockLimit;
        copy.maxStock = this.maxStock;
        copy.hasCooldown = this.hasCooldown;
        copy.cooldownSeconds = this.cooldownSeconds;
        copy.totalTimesSold = this.totalTimesSold;
        copy.playerStock = new HashMap<>(this.playerStock);
        copy.playerCooldownEndTick = new HashMap<>(this.playerCooldownEndTick);
        return copy;
    }
}
