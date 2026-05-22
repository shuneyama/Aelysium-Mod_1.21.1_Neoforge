package net.aelysium.aelysiummod.deus;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class VanishTicker {
    private static final int GATHER_TICKS   = 40;
    private static final int WAIT_TICKS     = 40;
    private static final int DISPERSE_TICKS = 10;
    private static final int BASE_PARTICLE_COUNT = 4;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, VanishTransitionHandler.TransitionData>> iter =
                VanishTransitionHandler.activeTransitions.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<UUID, VanishTransitionHandler.TransitionData> entry = iter.next();
            VanishTransitionHandler.TransitionData data = entry.getValue();
            ServerPlayer player = data.player;

            if (player.hasDisconnected()) {
                iter.remove();
                continue;
            }

            data.tickCount++;

            ServerLevel level = player.serverLevel();
            Vec3 pos = player.position();

            DustParticleOptions particle = createDust(data.deus);

            switch (data.phase) {
                case GATHERING -> {
                    spawnGatheringParticles(level, pos, data, particle);
                    if (data.tickCount >= GATHER_TICKS) {
                        data.phase = VanishTransitionHandler.Phase.WAITING;
                        data.tickCount = 0;
                    }
                }
                case WAITING -> {
                    spawnColumnParticles(level, pos, particle);
                    if (data.tickCount >= WAIT_TICKS) {
                        if (data.goingToSpectator) {
                            player.setGameMode(GameType.SPECTATOR);
                        } else {
                            player.setGameMode(data.previousGamemode);
                        }
                        data.phase = VanishTransitionHandler.Phase.VISIBLE;
                        data.tickCount = 0;
                    }
                }
                case VISIBLE -> {
                    data.phase = VanishTransitionHandler.Phase.DISPERSING;
                    data.tickCount = 0;
                }
                case DISPERSING -> {
                    spawnDispersingParticles(level, pos, data, particle);
                    if (data.tickCount >= DISPERSE_TICKS) {
                        iter.remove();
                    }
                }
            }
        }
    }

    private static DustParticleOptions createDust(DeusType deus) {
        int c = deus.color;
        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;
        return new DustParticleOptions(new Vector3f(r, g, b), 1.0f);
    }

    private static void spawnGatheringParticles(ServerLevel level, Vec3 base,
                                                VanishTransitionHandler.TransitionData data,
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
                                                 VanishTransitionHandler.TransitionData data,
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
