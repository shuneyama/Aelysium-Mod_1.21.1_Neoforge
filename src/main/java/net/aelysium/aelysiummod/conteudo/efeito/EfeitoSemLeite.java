package net.aelysium.aelysiummod.conteudo.efeito;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

import java.util.Set;

public abstract class EfeitoSemLeite extends MobEffect {

    protected EfeitoSemLeite(MobEffectCategory categoria, int cor) {
        super(categoria, cor);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> curas, MobEffectInstance instancia) {
        super.fillEffectCures(curas, instancia);
        curas.remove(EffectCures.MILK);
    }
}
