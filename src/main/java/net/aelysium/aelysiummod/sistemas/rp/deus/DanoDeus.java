package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class DanoDeus {
    public static final float DANO_DIVINO = 1.0E9F;

    private DanoDeus() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void aoReceberDano(LivingIncomingDamageEvent evento) {
        if (!UtilDeus.poder(evento.getSource().getEntity(), TipoDeus.DANO_MORTAL)) return;
        evento.setAmount(DANO_DIVINO);
        for (DamageContainer.Reduction tipo : DamageContainer.Reduction.values()) {
            evento.addReductionModifier(tipo, (container, reducao) -> 0.0F);
        }
    }
}
