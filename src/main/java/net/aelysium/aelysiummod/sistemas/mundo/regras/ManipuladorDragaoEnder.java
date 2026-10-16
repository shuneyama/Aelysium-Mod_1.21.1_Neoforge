package net.aelysium.aelysiummod.sistemas.mundo.regras;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManipuladorDragaoEnder {

    private static final float MAX_DAMAGE_PER_HIT = 10.0f;

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        if (!(event.getEntity() instanceof EnderDragon)) return;
        if (event.getNewDamage() > MAX_DAMAGE_PER_HIT) {
            event.setNewDamage(MAX_DAMAGE_PER_HIT);
        }
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        if (event.getEntity() instanceof EnderDragon) {
            event.setCanceled(true);
        }
    }
}
