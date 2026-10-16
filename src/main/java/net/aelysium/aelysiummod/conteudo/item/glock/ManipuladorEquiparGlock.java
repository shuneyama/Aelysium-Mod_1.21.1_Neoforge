package net.aelysium.aelysiummod.conteudo.item.glock;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.PacoteSomGlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorEquiparGlock {

    private static final Map<UUID, Boolean> segurandoGlock = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean agoraSegura = player.getMainHandItem().getItem() instanceof GlockItem;
        boolean antesSegura = segurandoGlock.getOrDefault(player.getUUID(), false);

        if (agoraSegura && !antesSegura && player.level() instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(serverLevel, null,
                    player.getX(), player.getY(), player.getZ(), 32.0,
                    new PacoteSomGlock(false, player.getX(), player.getY(), player.getZ()));
        }

        segurandoGlock.put(player.getUUID(), agoraSegura);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        segurandoGlock.remove(event.getEntity().getUUID());
    }
}
