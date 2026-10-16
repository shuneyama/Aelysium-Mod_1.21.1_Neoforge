package net.aelysium.aelysiummod.sistemas.social.npc.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManipuladorEventosNpc {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntidadeNpcPersonalizado npc
                && event.getEntity() instanceof ServerPlayer player) {

            byte[] skinBytes = npc.getSkinData();

            if (skinBytes != null && skinBytes.length > 0) {
                PacotesNpc.SincSkinNpcS2C packet = new PacotesNpc.SincSkinNpcS2C(
                        npc.getId(), skinBytes, "vanilla", npc.getModelType());
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }
}