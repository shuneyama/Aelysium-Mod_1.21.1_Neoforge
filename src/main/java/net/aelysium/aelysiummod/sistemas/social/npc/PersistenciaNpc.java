package net.aelysium.aelysiummod.sistemas.social.npc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

class PersistenciaNpc {

    static void salvar(EntidadeNpcPersonalizado e, CompoundTag tag) {
        HolderLookup.Provider provider = e.registryAccess();
        tag.putString("NpcName", e.getNpcName());
        tag.putString("SkinMode", e.getSkinMode());
        tag.putString("SkinModelType", e.getModelType());
        tag.putBoolean("NpcInvulnerable", e.isNpcInvulnerable());
        tag.putBoolean("LookAtPlayers", e.getLookAtPlayers());
        tag.putFloat("LookRadius", e.getLookRadius());
        tag.putFloat("ScaleX", e.getScaleX());
        tag.putFloat("ScaleY", e.getScaleY());
        tag.putFloat("ScaleZ", e.getScaleZ());
        tag.putBoolean("HasGravity", e.getHasGravity());
        tag.putBoolean("NameVisible", e.getNameVisible());
        tag.putFloat("RotationX", e.getRotationX());
        tag.putFloat("RotationY", e.getRotationY());
        tag.putFloat("RotationZ", e.getRotationZ());
        tag.putInt("NpcId", e.npcId);
        if (e.skinData != null) tag.putByteArray("SkinData", e.skinData);
        if (e.linkedBookUUID != null) tag.putUUID("LinkedBookUUID", e.linkedBookUUID);
        ListTag equipTag = new ListTag();
        for (int i = 0; i < e.equipment.length; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("Slot", i);
            if (!e.equipment[i].isEmpty()) slotTag.put("Item", e.equipment[i].save(provider));
            equipTag.add(slotTag);
        }
        tag.put("NpcEquipment", equipTag);
        ListTag tradesTag = new ListTag();
        for (DadosTrocaNpc trade : e.trades) tradesTag.add(trade.save(provider));
        tag.put("Trades", tradesTag);
        CompoundTag logTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : e.tradeLog.entrySet()) logTag.putInt(entry.getKey(), entry.getValue());
        tag.put("TradeLog", logTag);
    }

    static void carregar(EntidadeNpcPersonalizado e, CompoundTag tag) {
        HolderLookup.Provider provider = e.registryAccess();
        if (tag.contains("NpcId")) { e.npcId = tag.getInt("NpcId");
            e.idAssigned = true;
        }
        if (tag.contains("NpcName")) e.setNpcName(tag.getString("NpcName"));
        if (tag.contains("SkinMode")) e.setSkinMode(tag.getString("SkinMode"));
        if (tag.contains("SkinModelType")) e.setModelType(tag.getString("SkinModelType"));
        if (tag.contains("NpcInvulnerable")) e.setNpcInvulnerable(tag.getBoolean("NpcInvulnerable"));
        if (tag.contains("LookAtPlayers")) e.setLookAtPlayers(tag.getBoolean("LookAtPlayers"));
        if (tag.contains("LookRadius")) e.setLookRadius(tag.getFloat("LookRadius"));
        if (tag.contains("ScaleX")) e.setScaleX(tag.getFloat("ScaleX"));
        if (tag.contains("ScaleY")) e.setScaleY(tag.getFloat("ScaleY"));
        if (tag.contains("ScaleZ")) e.setScaleZ(tag.getFloat("ScaleZ"));
        if (tag.contains("HasGravity")) e.setHasGravity(tag.getBoolean("HasGravity"));
        if (tag.contains("NameVisible")) e.setNameVisible(tag.getBoolean("NameVisible"));
        if (tag.contains("RotationX")) e.setRotationX(tag.getFloat("RotationX"));
        if (tag.contains("RotationY")) e.setRotationY(tag.getFloat("RotationY"));
        if (tag.contains("RotationZ")) e.setRotationZ(tag.getFloat("RotationZ"));
        if (tag.contains("SkinData")) e.skinData = tag.getByteArray("SkinData");
        if (tag.contains("LinkedBookUUID")) e.linkedBookUUID = tag.getUUID("LinkedBookUUID");
        if (tag.contains("NpcEquipment")) {
            ListTag equipTag = tag.getList("NpcEquipment", Tag.TAG_COMPOUND);
            for (int i = 0; i < equipTag.size(); i++) {
                CompoundTag slotTag = equipTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                if (slot >= 0 && slot < e.equipment.length && slotTag.contains("Item")) {
                    e.equipment[slot] = ItemStack.parse(provider, slotTag.getCompound("Item")).orElse(ItemStack.EMPTY);
                }
            }
        }
        e.trades.clear();
        if (tag.contains("Trades")) {
            ListTag tradesTag = tag.getList("Trades", Tag.TAG_COMPOUND);
            for (int i = 0; i < tradesTag.size(); i++) e.trades.add(DadosTrocaNpc.load(tradesTag.getCompound(i), provider));
        }
        e.tradeLog.clear();
        if (tag.contains("TradeLog")) {
            CompoundTag logTag = tag.getCompound("TradeLog");
            for (String key : logTag.getAllKeys()) e.tradeLog.put(key, logTag.getInt(key));
        }
    }
}
