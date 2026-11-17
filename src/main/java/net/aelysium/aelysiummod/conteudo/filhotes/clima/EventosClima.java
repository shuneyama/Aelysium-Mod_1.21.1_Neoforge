package net.aelysium.aelysiummod.conteudo.filhotes.clima;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class EventosClima {

    private EventosClima() {
    }

    @SubscribeEvent
    public static void aoEntrar(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        Entity entidade = event.getEntity();
        if (ClimaAnimal.aplicavel(entidade)) {
            ClimaAnimal.garantir(entidade);
        }
    }

    @SubscribeEvent
    public static void aoRastrear(PlayerEvent.StartTracking event) {
        Entity alvo = event.getTarget();
        if (!(event.getEntity() instanceof ServerPlayer jogador) || !ClimaAnimal.aplicavel(alvo)) return;
        PacketDistributor.sendToPlayer(jogador, new PacoteClimaAnimal(alvo.getId(), ClimaAnimal.garantir(alvo)));
    }
}
