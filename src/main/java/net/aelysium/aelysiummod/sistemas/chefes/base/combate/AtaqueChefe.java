package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public record AtaqueChefe(float dano, int recarga, double peso, int alvos, Holder<MobEffect> efeito, int nivel, int duracaoTicks, boolean removeArmadura, boolean miraVoadores, boolean derrubaVoo) {
}
