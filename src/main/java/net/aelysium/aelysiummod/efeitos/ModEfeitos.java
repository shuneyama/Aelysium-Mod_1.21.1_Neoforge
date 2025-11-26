package net.aelysium.aelysiummod.efeitos;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEfeitos {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, AelysiumMod.MOD_ID);

    public static final Holder<MobEffect> CONGELAMENTO =
            MOB_EFFECTS.register("congelamento",
                    () -> new CongelamentoEfeito(MobEffectCategory.NEUTRAL, 0x00D8FF));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}



