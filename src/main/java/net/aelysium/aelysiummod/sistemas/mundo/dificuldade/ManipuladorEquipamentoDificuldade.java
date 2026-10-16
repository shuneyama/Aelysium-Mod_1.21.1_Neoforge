package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorEquipamentoDificuldade {

    @SubscribeEvent
    public static void aoTickEntidade(EntityTickEvent.Post evento) {
        if (!(evento.getEntity() instanceof Mob mob)) return;
        if (mob.level().isClientSide() || mob.tickCount % 20 != 0) return;
        if (!AplicadorDificuldade.marcado(mob)) return;
        AplicadorDificuldade.bloquearDropsEquipamento(mob);
    }
}
