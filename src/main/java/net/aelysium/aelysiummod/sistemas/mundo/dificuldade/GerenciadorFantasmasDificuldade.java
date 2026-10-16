package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GerenciadorFantasmasDificuldade {

    public static final String CHAVE_FANTASMA = "AelysiumFantasmaDificuldade";

    private static final double VELOCIDADE_FANTASMA = 0.55;

    private static final Map<UUID, Long> PROXIMA_CAVALGADA = new HashMap<>();

    public static boolean ehFantasmaMontado(Entity entidade) {
        return entidade instanceof Phantom fantasma && fantasma.getPersistentData().getBoolean(CHAVE_FANTASMA);
    }

    public static boolean golpeVeioDeFantasma(Entity atacante) {
        if (atacante == null) return false;
        return ehFantasmaMontado(atacante) || ehFantasmaMontado(atacante.getVehicle());
    }

    public static void aoTickJogador(ServerPlayer jogador, int nivel) {
        if (nivel < PerfilDificuldade.nivelMinimoFantasma()) return;
        if (jogador.isCreative() || jogador.isSpectator()) return;
        if (!(jogador.level() instanceof ServerLevel mundo)) return;
        if (!estaNoAlto(jogador, mundo)) return;

        long agora = mundo.getGameTime();
        Long proxima = PROXIMA_CAVALGADA.get(jogador.getUUID());
        if (proxima != null && agora < proxima) return;
        if (jogador.getRandom().nextDouble() >= PerfilDificuldade.chanceFantasma(nivel)) return;

        List<Phantom> ativos = mundo.getEntitiesOfClass(Phantom.class, jogador.getBoundingBox().inflate(64.0),
                fantasma -> fantasma.getPersistentData().getBoolean(CHAVE_FANTASMA));
        if (ativos.size() >= PerfilDificuldade.limiteFantasmas(nivel)) return;

        List<Mob> candidatos = mundo.getEntitiesOfClass(Mob.class, jogador.getBoundingBox().inflate(24.0, 48.0, 24.0),
                mob -> mob.isAlive() && !mob.isPassenger() && !mob.isVehicle()
                        && AplicadorDificuldade.marcado(mob)
                        && ConfiguracaoDificuldade.cavaleiroDeFantasma(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType())));
        if (candidatos.isEmpty()) return;

        Mob cavaleiro = candidatos.get(jogador.getRandom().nextInt(candidatos.size()));
        if (gerarFantasma(mundo, cavaleiro, jogador)) {
            PROXIMA_CAVALGADA.put(jogador.getUUID(), agora + PerfilDificuldade.recargaFantasmaTicks(nivel));
        }
    }

    public static void guiarFantasmas(ServerPlayer jogador) {
        if (!(jogador.level() instanceof ServerLevel mundo)) return;
        if (jogador.isCreative() || jogador.isSpectator()) return;

        List<Phantom> fantasmas = mundo.getEntitiesOfClass(Phantom.class, jogador.getBoundingBox().inflate(64.0),
                fantasma -> fantasma.isAlive() && fantasma.getPersistentData().getBoolean(CHAVE_FANTASMA));
        for (Phantom fantasma : fantasmas) {
            for (Entity passageiro : fantasma.getPassengers()) {
                if (passageiro instanceof Mob cavaleiro) {
                    cavaleiro.goalSelector.setControlFlag(Goal.Flag.MOVE, false);
                    cavaleiro.goalSelector.setControlFlag(Goal.Flag.JUMP, false);
                    cavaleiro.getNavigation().stop();
                }
            }

            LivingEntity alvo = fantasma.getTarget();
            if (alvo == null || !alvo.isAlive()) {
                fantasma.setTarget(jogador);
                alvo = jogador;
            }
            if (alvo.level() != fantasma.level()) continue;

            Vec3 destino = alvo.position().add(0.0, alvo.getBbHeight() * 0.5, 0.0);
            Vec3 direcao = destino.subtract(fantasma.position());
            double distancia = direcao.length();
            if (distancia < 0.5) continue;

            Vec3 velocidadeDesejada = direcao.scale(VELOCIDADE_FANTASMA / distancia);
            Vec3 nova = fantasma.getDeltaMovement().scale(0.6).add(velocidadeDesejada.scale(0.4));
            fantasma.setDeltaMovement(nova);
            fantasma.hasImpulse = true;
            fantasma.setYRot((float) (Math.toDegrees(Math.atan2(-nova.x, nova.z))));
            fantasma.setXRot((float) -Math.toDegrees(Math.atan2(nova.y, Math.sqrt(nova.x * nova.x + nova.z * nova.z))));
            fantasma.yBodyRot = fantasma.getYRot();
            fantasma.yHeadRot = fantasma.getYRot();
        }
    }

    public static void limpar(UUID jogador) {
        PROXIMA_CAVALGADA.remove(jogador);
    }

    private static boolean estaNoAlto(ServerPlayer jogador, ServerLevel mundo) {
        if (ControleVooDificuldade.estaVoando(jogador)) return true;
        if (jogador.onGround()) return false;
        int solo = mundo.getHeight(Heightmap.Types.MOTION_BLOCKING, jogador.getBlockX(), jogador.getBlockZ());
        return jogador.getY() - solo >= PerfilDificuldade.alturaMinimaFantasma();
    }

    private static boolean gerarFantasma(ServerLevel mundo, Mob cavaleiro, ServerPlayer alvo) {
        Phantom fantasma = EntityType.PHANTOM.create(mundo);
        if (fantasma == null) return false;

        fantasma.moveTo(cavaleiro.getX(), cavaleiro.getY() + 1.0, cavaleiro.getZ(), cavaleiro.getYRot(), 0.0f);
        fantasma.finalizeSpawn(mundo, mundo.getCurrentDifficultyAt(BlockPos.containing(fantasma.position())), MobSpawnType.EVENT, null);
        fantasma.getPersistentData().putBoolean(CHAVE_FANTASMA, true);
        fantasma.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, false, false));
        fantasma.setTarget(alvo);

        mundo.addFreshEntity(fantasma);
        cavaleiro.startRiding(fantasma, true);
        return true;
    }
}
