package net.aelysium.aelysiummod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

import java.util.Random;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class RenderFormaDivina {

    private static final Random rng = new Random();

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (Player player : mc.level.players()) {
            UUID uuid = player.getUUID();
            if (!FormaDivinaCache.isActive(uuid)) continue;

            FormaDivinaCache.DivinaState state = FormaDivinaCache.get(uuid);
            if (state == null) continue;

            float r = ((state.cor() >> 16) & 0xFF) / 255f;
            float g = ((state.cor() >> 8) & 0xFF) / 255f;
            float b = (state.cor() & 0xFF) / 255f;

            DustParticleOptions dust = new DustParticleOptions(new Vector3f(r, g, b), 0.6f);

            double cx = player.getX();
            double cy = player.getY() + 0.8;
            double cz = player.getZ();

            for (int i = 0; i < 3; i++) {
                double ox = (rng.nextDouble() - 0.5) * 0.35;
                double oy = (rng.nextDouble() - 0.5) * 0.9;
                double oz = (rng.nextDouble() - 0.5) * 0.35;

                mc.level.addParticle(dust,
                        cx + ox, cy + oy, cz + oz,
                        (rng.nextDouble() - 0.5) * 0.005,
                        0.01 + rng.nextDouble() * 0.01,
                        (rng.nextDouble() - 0.5) * 0.005);
            }

            double angle = (player.tickCount * 0.12) % (Math.PI * 2);
            for (int i = 0; i < 2; i++) {
                double a = angle + i * Math.PI;
                double rx = Math.cos(a) * 0.3;
                double rz = Math.sin(a) * 0.3;

                mc.level.addParticle(dust,
                        cx + rx, cy + Math.sin(player.tickCount * 0.15 + i) * 0.2, cz + rz,
                        0, 0.005, 0);
            }
        }
    }
}