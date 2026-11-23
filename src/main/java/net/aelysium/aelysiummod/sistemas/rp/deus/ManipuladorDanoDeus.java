package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorDanoDeus {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!UtilDeus.ehDeus(player)) return;

        event.setCanceled(true);
    }
}
