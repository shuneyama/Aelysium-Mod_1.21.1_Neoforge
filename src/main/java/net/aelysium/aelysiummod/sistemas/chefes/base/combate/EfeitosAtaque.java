package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

import net.aelysium.aelysiummod.sistemas.mundo.dificuldade.ControleVooDificuldade;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EfeitosAtaque {
    private EfeitosAtaque() {
    }

    public static boolean ferir(LivingEntity atacante, Entity alvo, DamageSource fonte, AtaqueChefe ataque) {
        float dano = atacante instanceof ChefeAelysium chefe ? ataque.dano() * chefe.multiplicadorDano(ataque) : ataque.dano();
        boolean ferido = alvo.hurt(fonte, dano);
        if (ferido) aplicar(atacante, alvo, ataque);
        return ferido;
    }

    public static void aplicar(LivingEntity atacante, Entity alvo, AtaqueChefe ataque) {
        if (!(alvo instanceof LivingEntity vivo) || vivo.level().isClientSide() || !vivo.isAlive()) return;
        if (ataque.efeito() != null && ataque.duracaoTicks() > 0) vivo.addEffect(new MobEffectInstance(ataque.efeito(), ataque.duracaoTicks(), ataque.nivel() - 1), atacante);
        if (ataque.removeArmadura()) QuebraArmadura.aplicar(vivo);
        if (ataque.derrubaVoo() && vivo instanceof ServerPlayer jogador && ControleVooDificuldade.estaVoando(jogador)) {
            ControleVooDificuldade.bloquear(jogador, (int) Math.round(ConfiguracaoChefes.numero(ConfiguracaoChefes.VOO + ".bloqueio_segundos") * 20));
        }
    }
}
