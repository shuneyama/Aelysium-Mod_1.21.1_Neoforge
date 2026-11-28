package net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente;

import net.aelysium.aelysiummod.sistemas.chefes.base.evento.AgendadorNivel;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.ClientParticleBuilder;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.MathUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.RandomUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.Vec3Colors;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class EfeitosBlocoFlorVazio {
    private static final int healAnimationDelay = 16;

    private EfeitosBlocoFlorVazio() {
    }

    public static void quebrar(BlockPos pos) {
        for (int i = 0; i < 12; i++){
            Vec3 vel = VecUtils.yAxis.scale(RandomUtils.range(0.1, 0.2));
            Vec3 spawnPos = VecUtils.asVec3(pos).add(VecUtils.unit.scale(0.5)).add(VecUtils.planeProject(RandomUtils.randVec(), VecUtils.yAxis).normalize().scale(0.5));
            Particles.spikeParticleFactory.build(spawnPos, vel);
        }
    }

    public static void handleVoidBlossomHeal(ClientLevel level, Vec3 source, Vec3 dest){
        spawnHealParticle(dest, source, level);
        spawnChargeParticle(source, level);
    }

    private static void spawnChargeParticle(Vec3 source, ClientLevel level){
        Vec3 particlePos = source.add(VecUtils.yAxis.scale(0.25));
        AgendadorNivel.de(level).addEvent(
                new TimedEvent(
                        () -> Particles.healParticleFactory.build(particlePos.add(RandomUtils.randVec().scale(0.2)), Vec3.ZERO),
                        32,
                        32,
                        () -> false
                )
        );
    }

    private static void spawnHealParticle(Vec3 dest, Vec3 source, ClientLevel level){
        List<Vec3> particlePositions = new ArrayList<>();
        int numCirclePoints = healAnimationDelay;
        List<Vec3> circlePoints = MathUtils.circlePoints(0.5, numCirclePoints, dest.subtract(source).normalize()).stream().toList();
        MathUtils.lineCallback(source, dest, 32, (pos, i) ->
                particlePositions.add(pos.add(circlePoints.get(i % numCirclePoints))));

        AtomicInteger i = new AtomicInteger();
        AgendadorNivel.de(level).addEvent(
                new TimedEvent(
                        () -> {
                            Particles.healParticleFactory.build(particlePositions.get(i.get()), Vec3.ZERO);
                            Particles.healParticleFactory.build(particlePositions.get(i.get() + 1), Vec3.ZERO);
                            i.addAndGet(2);
                        },
                        0,
                        healAnimationDelay,
                        () -> false
                )
        );
    }

    public static void handleVoidBlossomPlace(Vec3 pos){
        for (int i = 0; i <= 12; i++){
            Vec3 spawnPos = pos.add(VecUtils.planeProject(RandomUtils.randVec(), VecUtils.yAxis).normalize().scale(0.5));
            Vec3 vel = VecUtils.yAxis.scale(RandomUtils.range(0.1, 0.3));
            int randomRot = RandomUtils.range(0, 360);
            float angularMomentum = RandomUtils.randSign() * 4f;

            Particles.petalParticleFactory
                    .continuousRotation(f -> randomRot + f.getAge() * angularMomentum)
                    .continuousVelocity(f -> vel.scale(1.0 - f.ageRatio))
                    .build(spawnPos, vel);
        }
    }

    public static class Particles {
        private static final ClientParticleBuilder spikeParticleFactory = new ClientParticleBuilder(RegistroFlorVazio.LINE.get())
                .color(f -> MathUtils.lerpVec(f, Vec3Colors.VOID_PURPLE, Vec3Colors.ULTRA_DARK_PURPLE))
                .colorVariation(0.15)
                .brightness(RegistroFlorVazio.BRILHO_TOTAL)
                .scale(0.25f)
                .age(10, 15);

        private static final ClientParticleBuilder healParticleFactory = new ClientParticleBuilder(RegistroFlorVazio.OBSIDILITH_BURST.get())
                .color(f -> MathUtils.lerpVec(f, Vec3Colors.PINK, Vec3Colors.ULTRA_DARK_PURPLE))
                .colorVariation(0.15)
                .brightness(RegistroFlorVazio.BRILHO_TOTAL)
                .scale(f -> 0.4f * (1 - f * 0.75f))
                .age(10);

        private static final ClientParticleBuilder petalParticleFactory = new ClientParticleBuilder(RegistroFlorVazio.PETAL.get())
                .color(f -> MathUtils.lerpVec(f, Vec3Colors.PINK, Vec3Colors.ULTRA_DARK_PURPLE))
                .brightness(RegistroFlorVazio.BRILHO_TOTAL)
                .colorVariation(0.15)
                .scale(f -> 0.15f * (1 - f * 0.25f))
                .age(30);
    }
}
