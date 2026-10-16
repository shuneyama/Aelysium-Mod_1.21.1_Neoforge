package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import net.aelysium.aelysiummod.sistemas.social.npc.DadosTrocaNpc;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

class PersistenciaEditorNpc {

    static void carregar(TelaEditorNpc t, EntidadeNpcPersonalizado npc, CompoundTag npcData) {
        t.editName          = npcData.getString("NpcName");
        t.editSkinMode      = npcData.getString("SkinMode");
        t.editModelType     = npcData.getString("ModelType");
        t.editInvulnerable  = npcData.getBoolean("Invulnerable");
        t.editLookAtPlayers = npcData.getBoolean("LookAtPlayers");
        t.editLookRadius    = npcData.getFloat("LookRadius");
        t.editScaleX = npcData.contains("ScaleX") ? npcData.getFloat("ScaleX") : 1.0f;
        t.editScaleY = npcData.contains("ScaleY") ? npcData.getFloat("ScaleY") : 1.0f;
        t.editScaleZ = npcData.contains("ScaleZ") ? npcData.getFloat("ScaleZ") : 1.0f;
        t.editPosX = (float) npc.getX();
        t.editPosY = (float) npc.getY();
        t.editPosZ = (float) npc.getZ();
        t.editRotX = npcData.contains("RotationX") ? npcData.getFloat("RotationX") : 0f;
        t.editRotY = npcData.contains("RotationY") ? npcData.getFloat("RotationY") : 0f;
        t.editRotZ = npcData.contains("RotationZ") ? npcData.getFloat("RotationZ") : 0f;
        t.editHasGravity  = npcData.contains("HasGravity") && npcData.getBoolean("HasGravity");
        t.editNameVisible = !npcData.contains("NameVisible") || npcData.getBoolean("NameVisible");

        for (int i = 0; i < t.editEquipment.length; i++) t.editEquipment[i] = ItemStack.EMPTY;
        if (npcData.contains("Equipment")) {
            ListTag equipTag = npcData.getList("Equipment", 10);
            for (int i = 0; i < equipTag.size(); i++) {
                CompoundTag slotTag = equipTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                if (slot >= 0 && slot < t.editEquipment.length && slotTag.contains("Item")) {
                    t.editEquipment[slot] = ItemStack.parse(
                            Minecraft.getInstance().level.registryAccess(),
                            slotTag.getCompound("Item")).orElse(ItemStack.EMPTY);
                }
            }
        }
        if (npcData.contains("Trades")) {
            ListTag tradesTag = npcData.getList("Trades", 10);
            for (int i = 0; i < tradesTag.size(); i++) {
                t.editTrades.add(DadosTrocaNpc.load(tradesTag.getCompound(i),
                        Minecraft.getInstance().level.registryAccess()));
            }
        }
        while (t.editTrades.size() < TelaEditorNpc.TRADES_PER_PAGE) t.editTrades.add(new DadosTrocaNpc());
    }

    static void salvar(TelaEditorNpc t) {
        PacketDistributor.sendToServer(new PacotesNpc.AtualizarConfiguracoesNpcC2S(
                t.npc.getId(), t.editName, t.editInvulnerable,
                t.editLookAtPlayers, t.editLookRadius, t.editSkinMode, t.editModelType,
                t.editScaleX, t.editScaleY, t.editScaleZ,
                t.editPosX, t.editPosY, t.editPosZ,
                t.editRotX, t.editRotY, t.editRotZ,
                t.editHasGravity, t.editNameVisible));

        CompoundTag td = new CompoundTag();
        ListTag tl = new ListTag();
        for (DadosTrocaNpc troca : t.editTrades) {
            if (troca.isValid()) {
                if (troca.hasStockLimit()) {
                    troca.setCurrentStock(troca.getMaxStock());
                }
                troca.setCooldownEndTick(-1);
                tl.add(troca.save(Minecraft.getInstance().level.registryAccess()));
            }
        }
        td.put("Trades", tl);
        PacketDistributor.sendToServer(new PacotesNpc.AtualizarTrocasNpcC2S(t.npc.getId(), td));

        CompoundTag equipData = new CompoundTag();
        ListTag equipList = new ListTag();
        for (int i = 0; i < t.editEquipment.length; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("Slot", i);
            if (!t.editEquipment[i].isEmpty())
                slotTag.put("Item", t.editEquipment[i].save(Minecraft.getInstance().level.registryAccess()));
            equipList.add(slotTag);
        }
        equipData.put("Equipment", equipList);
        PacketDistributor.sendToServer(new PacotesNpc.AtualizarEquipamentoNpcC2S(t.npc.getId(), equipData));
    }
}
