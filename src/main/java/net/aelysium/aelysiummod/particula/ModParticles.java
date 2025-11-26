package net.aelysium.aelysiummod.particula;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles  {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, AelysiumMod.MOD_ID);



    public static final Supplier<SimpleParticleType> DAMA_DA_NOITE_PARTICULA = PARTICLES.register("dama_da_noite_particula",
            () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> DAMA_VERMELHA_DA_NOITE_PARTICULA = PARTICLES.register("dama_vermelha_da_noite_particula",
            () -> new SimpleParticleType(true));



    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }
}
