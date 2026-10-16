package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ObjetivoBruxaTatica<T extends PathfinderMob & RangedAttackMob> extends Goal {

    private final T bruxa;
    private final int nivel;
    private int recarga;
    private int recalculoFuga;
    private int fugaTicks;
    private int descanso;

    public ObjetivoBruxaTatica(T bruxa, int nivel) {
        this.bruxa = bruxa;
        this.nivel = nivel;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity alvo = bruxa.getTarget();
        return alvo != null && alvo.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        recarga = PerfilDificuldade.intervaloPocaoBruxa(nivel) / 2;
        recalculoFuga = 0;
    }

    @Override
    public void stop() {
        bruxa.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity alvo = bruxa.getTarget();
        if (alvo == null) return;

        bruxa.getLookControl().setLookAt(alvo, 30.0f, 30.0f);

        double distanciaSegura = PerfilDificuldade.distanciaSeguraBruxa();
        double distanciaMaxima = PerfilDificuldade.distanciaMaximaAtaqueBruxa();
        double distanciaAlvo = bruxa.distanceTo(alvo);

        Player ameaca = bruxa.level().getNearestPlayer(bruxa.getX(), bruxa.getY(), bruxa.getZ(), distanciaSegura, EntitySelector.NO_CREATIVE_OR_SPECTATOR);

        if (descanso > 0) descanso--;
        if (ameaca == null && fugaTicks > 0) fugaTicks--;

        if (ameaca != null && descanso <= 0) {
            fugaTicks++;
            if (fugaTicks >= PerfilDificuldade.duracaoMaximaFugaBruxa()) {
                fugaTicks = 0;
                descanso = PerfilDificuldade.descansoAposFugaBruxa();
                bruxa.getNavigation().stop();
            } else if (--recalculoFuga <= 0 || bruxa.getNavigation().isDone()) {
                recalculoFuga = 8;
                Vec3 destino = DefaultRandomPos.getPosAway(bruxa, 16, 7, ameaca.position());
                if (destino != null) {
                    bruxa.getNavigation().moveTo(destino.x, destino.y, destino.z, PerfilDificuldade.velocidadeFugaBruxa());
                }
            }
        } else if (ameaca != null) {
            bruxa.getNavigation().stop();
        } else if (distanciaAlvo > distanciaMaxima) {
            bruxa.getNavigation().moveTo(alvo, 1.0);
        } else {
            bruxa.getNavigation().stop();
        }

        recarga--;
        if (recarga <= 0 && distanciaAlvo <= distanciaMaxima + 2.0 && bruxa.getSensing().hasLineOfSight(alvo)) {
            float forca = Mth.clamp((float) (distanciaSegura / Math.max(distanciaAlvo, 1.0)), 0.1f, 1.0f);
            bruxa.performRangedAttack(alvo, forca);
            recarga = PerfilDificuldade.intervaloPocaoBruxa(nivel);
        }
    }
}
