package net.aelysium.aelysiummod.conteudo.efeito;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.aelysium.aelysiummod.registro.ModEfeitos;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorCigarroDano {

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        MobEffectInstance instance = victim.getEffect(ModEfeitos.CIGARRO_DANO);
        if (instance == null) return;

        int amplifier = Math.min(instance.getAmplifier(), CigarroDanoEfeito.MULTIPLICADORES.length - 1);
        double multiplicador = CigarroDanoEfeito.MULTIPLICADORES[amplifier];

        float novoDano = (float) (event.getContainer().getNewDamage() * multiplicador);
        event.getContainer().setNewDamage(novoDano);
    }
}
