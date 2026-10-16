package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManipuladorDanoDeus {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        TipoDeus deus = DadosDeus.get(player.serverLevel()).getDeus(player.getUUID());
        if (deus == TipoDeus.NONE) return;

        event.setCanceled(true);
    }
}
