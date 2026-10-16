package net.aelysium.aelysiummod.sistemas.mundo.regras;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManipuladorExplosao {

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        var source = event.getExplosion().getDirectSourceEntity();
        if (source instanceof EndCrystal || source instanceof Creeper) {
            event.getAffectedBlocks().clear();
        }
    }
}
