package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorAtaqueDificuldade {

    @SubscribeEvent
    public static void aoTickEntidade(EntityTickEvent.Post evento) {
        if (!(evento.getEntity() instanceof Mob mob) || mob.level().isClientSide()) return;
        if (mob.getTarget() == null) return;

        int nivel = AplicadorDificuldade.nivelAplicado(mob);
        if (nivel <= 0) return;
        if (!ConfiguracaoDificuldade.ataqueAcelerado(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()))) return;
        if (mob.getRandom().nextDouble() >= PerfilDificuldade.chanceTickExtraAtaque(nivel)) return;

        if (mob instanceof Breeze vortice && vortice.level() instanceof ServerLevel mundo) {
            vortice.getBrain().tick(mundo, vortice);
        } else {
            mob.goalSelector.tickRunningGoals(false);
        }
    }

    @SubscribeEvent
    public static void aoTickVex(EntityTickEvent.Post evento) {
        if (!(evento.getEntity() instanceof Mob vex) || vex.level().isClientSide()) return;
        if (!ConfiguracaoDificuldade.vexConfigurado(BuiltInRegistries.ENTITY_TYPE.getKey(vex.getType()))) return;

        LivingEntity alvo = vex.getTarget();
        int nivel = AplicadorDificuldade.nivelAplicado(vex);
        if (alvo == null || nivel <= 0) return;

        Vec3 direcao = alvo.getEyePosition().subtract(vex.position()).normalize();
        vex.setDeltaMovement(vex.getDeltaMovement().add(direcao.scale(PerfilDificuldade.aceleracaoVex(nivel))));
    }

    @SubscribeEvent
    public static void aoCausarDano(LivingDamageEvent.Post evento) {
        if (!(evento.getEntity() instanceof ServerPlayer jogador)) return;
        if (!(evento.getSource().getEntity() instanceof Mob atacante)) return;

        int nivel = AplicadorDificuldade.nivelAplicado(atacante);
        if (nivel <= 0 || DadosDificuldade.get(jogador.server).getNivel() <= 0) return;

        boolean corpoACorpo = evento.getSource().getDirectEntity() == atacante;
        boolean flecha = evento.getSource().getDirectEntity() instanceof AbstractArrow;

        if (corpoACorpo && atacante instanceof CaveSpider) {
            aplicar(jogador, MobEffects.POISON, PerfilDificuldade.duracaoVenenoAranha(), PerfilDificuldade.amplificadorVenenoAranha(nivel));
        } else if (corpoACorpo && atacante instanceof WitherSkeleton) {
            aplicar(jogador, MobEffects.WITHER, PerfilDificuldade.duracaoWitherEsqueleto(), PerfilDificuldade.amplificadorWitherEsqueleto(nivel));
        } else if (corpoACorpo && atacante instanceof Husk) {
            aplicar(jogador, MobEffects.HUNGER, PerfilDificuldade.duracaoFomeHusk(), PerfilDificuldade.amplificadorFomeHusk(nivel));
        } else if (flecha && atacante instanceof Stray) {
            aplicar(jogador, MobEffects.MOVEMENT_SLOWDOWN, PerfilDificuldade.duracaoLentidaoStray(), PerfilDificuldade.amplificadorLentidaoStray(nivel));
        } else if (flecha && atacante instanceof Bogged) {
            aplicar(jogador, MobEffects.POISON, PerfilDificuldade.duracaoVenenoBogged(), PerfilDificuldade.amplificadorVenenoBogged(nivel));
        }
    }

    private static void aplicar(ServerPlayer jogador, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> efeito, int duracao, int amplificador) {
        if (amplificador <= 0) return;
        jogador.addEffect(new MobEffectInstance(efeito, duracao, amplificador));
    }
}
