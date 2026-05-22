package net.aelysium.aelysiummod.efeitos;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEfeitos {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, AelysiumMod.MOD_ID);

    public static final Holder<MobEffect> CONGELAMENTO =
            MOB_EFFECTS.register("congelamento",
                    () -> new CongelamentoEfeito(MobEffectCategory.NEUTRAL, 0x00D8FF));

    public static final DeferredHolder<MobEffect, CegueiraAbissalEfeito> CEGUEIRA_ABISSAL =
            MOB_EFFECTS.register("cegueira_abissal", CegueiraAbissalEfeito::new);

    public static final DeferredHolder<MobEffect, CaecitasEfeito> CAECITAS =
            MOB_EFFECTS.register("caecitas", CaecitasEfeito::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}



