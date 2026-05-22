package net.aelysium.aelysiummod.npc.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class NpcEventHandler {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof CustomNpcEntity npc
                && event.getEntity() instanceof ServerPlayer player) {

            byte[] skinBytes = npc.getSkinData();

            if (skinBytes != null && skinBytes.length > 0) {
                NpcPackets.SyncNpcSkinS2C packet = new NpcPackets.SyncNpcSkinS2C(
                        npc.getId(), skinBytes, "vanilla", npc.getModelType());
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }
}