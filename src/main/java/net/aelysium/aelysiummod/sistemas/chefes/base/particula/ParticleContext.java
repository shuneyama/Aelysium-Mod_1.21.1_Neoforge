package net.aelysium.aelysiummod.sistemas.chefes.base.particula;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;

public record ParticleContext(SpriteSet spriteSet, ClientLevel level, Vec3 pos, Vec3 vel, Boolean cycleSprites) {
}
