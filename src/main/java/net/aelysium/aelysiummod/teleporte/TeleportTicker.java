package net.aelysium.aelysiummod.teleporte;

import net.aelysium.aelysiummod.deus.DeusType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TeleportTicker {

    private static final int GATHER_TICKS = 40;
    private static final int WAIT_TICKS = 40;
    private static final int DISPERSE_TICKS = 10;
    private static final int BASE_PARTICLE_COUNT = 8;
    private static final int GLOW_TOGGLE_INTERVAL = 5;

    private static final int LEVITATE_TICKS = 4;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, TeleportTransitionHandler.TransitionData>> iter =
                TeleportTransitionHandler.activeTransitions.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<UUID, TeleportTransitionHandler.TransitionData> entry = iter.next();
            TeleportTransitionHandler.TransitionData data = entry.getValue();
            ServerPlayer player = data.player;

            if (player.hasDisconnected()) {
                limparEfeitos(player);
                iter.remove();
                continue;
            }

            data.tickCount++;

            ServerLevel level = player.serverLevel();
            Vec3 pos = player.position();
            DustParticleOptions particle = createDust(data.deus);

            if (data.phase != TeleportTransitionHandler.Phase.DONE) {
                atualizarGlowing(data, player);
            }

            switch (data.phase) {
                case GATHERING -> {
                    if (data.tickCount <= LEVITATE_TICKS) {
                        player.addEffect(new MobEffectInstance(
                                MobEffects.LEVITATION, 3, 2, false, false, false));
                    } else {
                        player.removeEffect(MobEffects.LEVITATION);
                        travarNoAr(player);
                    }

                    spawnGatheringParticles(level, pos, data, particle);

                    if (data.tickCount >= GATHER_TICKS) {
                        data.phase = TeleportTransitionHandler.Phase.WAITING;
                        data.tickCount = 0;
                    }
                }
                case WAITING -> {
                    travarNoAr(player);

                    spawnColumnParticles(level, pos, particle);

                    if (data.tickCount == WAIT_TICKS - 5) {
                        level.playSound(null, player.blockPosition(),
                                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS,
                                0.2f, 1.2f);
                    }

                    if (data.tickCount >= WAIT_TICKS) {
                        level.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                                pos.x, pos.y + 1.0, pos.z,
                                1, 0, 0, 0, 0);

                        limparEfeitos(player);
                        player.connection.teleport(data.destX, data.destY, data.destZ,
                                data.destYaw, data.destPitch);

                        data.phase = TeleportTransitionHandler.Phase.DISPERSING;
                        data.tickCount = 0;
                    }
                }
                case DISPERSING -> {
                    spawnDispersingParticles(level, player.position(), data, particle);
                    if (data.tickCount >= DISPERSE_TICKS) {
                        player.setGlowingTag(false);
                        iter.remove();
                    }
                }
                case DONE -> iter.remove();
            }
        }
    }

    private void travarNoAr(ServerPlayer player) {
        Vec3 vel = player.getDeltaMovement();
        player.setDeltaMovement(vel.x * 0.1, 0, vel.z * 0.1);

        if (!player.hasEffect(MobEffects.SLOW_FALLING)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING, 200, 255, false, false, false));
        }

        if (!player.hasEffect(MobEffects.DARKNESS)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DARKNESS, 200, 0, false, false, false));
        }

        player.setNoGravity(true);
    }

    private void atualizarGlowing(TeleportTransitionHandler.TransitionData data, ServerPlayer player) {
        data.glowToggleTicks++;
        if (data.glowToggleTicks >= GLOW_TOGGLE_INTERVAL) {
            data.glowToggleTicks = 0;
            data.glowingAtivo = !data.glowingAtivo;
            player.setGlowingTag(data.glowingAtivo);
        }
    }

    private void limparEfeitos(ServerPlayer player) {
        player.removeEffect(MobEffects.LEVITATION);
        player.removeEffect(MobEffects.SLOW_FALLING);
        player.removeEffect(MobEffects.DARKNESS);
        player.setGlowingTag(false);
        player.setNoGravity(false);
    }

    private static DustParticleOptions createDust(DeusType deus) {
        int c = deus.color;
        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;
        return new DustParticleOptions(new Vector3f(r, g, b), 1.0f);
    }

    private static void spawnGatheringParticles(ServerLevel level, Vec3 base,
                                                TeleportTransitionHandler.TransitionData data,
                                                DustParticleOptions particle) {
        int tick = data.tickCount;
        Random rng = new Random();
        double progress = Math.min((double) tick / GATHER_TICKS, 1.0);
        int count = BASE_PARTICLE_COUNT + (int) (progress * 10);

        for (int i = 0; i < count; i++) {
            double targetY = 1.9 * (1.0 - progress);
            double angle = rng.nextDouble() * Math.PI * 2;
            double startRadius = 2.5 + rng.nextDouble() * 1.0;
            double startY = base.y + 0.3 + rng.nextDouble() * 0.5;
            double startX = base.x + Math.cos(angle) * startRadius;
            double startZ = base.z + Math.sin(angle) * startRadius;

            double bodyRadius = 0.3 + rng.nextDouble() * 0.15;
            double bodyAngle = rng.nextDouble() * Math.PI * 2;
            double endX = base.x + Math.cos(bodyAngle) * bodyRadius;
            double endZ = base.z + Math.sin(bodyAngle) * bodyRadius;
            double endY = base.y + targetY;

            double t = 0.55 + rng.nextDouble() * 0.35;
            double cx = (startX + endX) / 2;
            double cy = base.y + 0.05;
            double cz = (startZ + endZ) / 2;

            double px = bezier(startX, cx, endX, t);
            double py = bezier(startY, cy, endY, t);
            double pz = bezier(startZ, cz, endZ, t);

            double vx = (endX - px) * 0.03;
            double vy = (endY - py) * 0.03;
            double vz = (endZ - pz) * 0.03;

            level.sendParticles(particle, px, py, pz, 1, vx, vy, vz, 0.0);
        }
    }

    private static void spawnColumnParticles(ServerLevel level, Vec3 base, DustParticleOptions particle) {
        Random rng = new Random();
        int count = BASE_PARTICLE_COUNT * 2;
        for (int i = 0; i < count; i++) {
            double bodyRadius = 0.25 + rng.nextDouble() * 0.2;
            double angle = rng.nextDouble() * Math.PI * 2;
            double x = base.x + Math.cos(angle) * bodyRadius;
            double z = base.z + Math.sin(angle) * bodyRadius;
            double y = base.y + rng.nextDouble() * 1.9;
            double vx = (rng.nextDouble() - 0.5) * 0.01;
            double vy = (rng.nextDouble() - 0.5) * 0.01;
            double vz = (rng.nextDouble() - 0.5) * 0.01;
            level.sendParticles(particle, x, y, z, 1, vx, vy, vz, 0.0);
        }
    }

    private static void spawnDispersingParticles(ServerLevel level, Vec3 base,
                                                 TeleportTransitionHandler.TransitionData data,
                                                 DustParticleOptions particle) {
        int tick = data.tickCount;
        Random rng = new Random();
        double progress = (double) tick / DISPERSE_TICKS;
        int count = Math.max(1, (BASE_PARTICLE_COUNT * 3) - (int) (progress * 10));

        for (int i = 0; i < count; i++) {
            double bodyRadius = 0.3;
            double bodyAngle = rng.nextDouble() * Math.PI * 2;
            double startX = base.x + Math.cos(bodyAngle) * bodyRadius;
            double startZ = base.z + Math.sin(bodyAngle) * bodyRadius;
            double startY = base.y + rng.nextDouble() * 1.9;
            double vx = Math.cos(bodyAngle) * (0.05 + rng.nextDouble() * 0.04);
            double vy = (rng.nextDouble() - 0.3) * 0.03;
            double vz = Math.sin(bodyAngle) * (0.05 + rng.nextDouble() * 0.04);
            level.sendParticles(particle, startX, startY, startZ, 1, vx, vy, vz, 0.0);
        }
    }

    private static double bezier(double p0, double p1, double p2, double t) {
        double inv = 1 - t;
        return inv * inv * p0 + 2 * inv * t * p1 + t * t * p2;
    }
}