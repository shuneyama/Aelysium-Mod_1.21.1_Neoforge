package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AlvosChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.acao.CooldownAction;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.acao.IActionStop;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.acao.IActionWithCooldown;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.objetivos.ActionGoal;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventScheduler;

import java.util.Map;
import java.util.function.Supplier;

public class VoidBlossomAttacks {
    private final VoidBlossomEntity entity;

    private final Supplier<Boolean> cancelAttackAction;
    private final VoidBlossomMoveLogic moveLogic;

    public static final byte spikeAttack = 4;
    public static final byte spikeWaveAttack = 5;
    public static final byte stopAttackAnimation = 6;
    public static final byte sporeAttack = 7;
    public static final byte bladeAttack = 8;
    public static final byte blossomAction = 9;
    public static final byte spawnAction = 10;

    public VoidBlossomAttacks(VoidBlossomEntity entity, EventScheduler eventScheduler, Supplier<Boolean> doBlossom, AlvosChefe alvos) {
        this.entity = entity;

        this.cancelAttackAction = () -> entity.isDeadOrDying() || entity.getTarget() == null;
        Map<Byte, IActionWithCooldown> statusRegistry = Map.of(
                spikeAttack, new SpikeAction(entity, eventScheduler, cancelAttackAction),
                spikeWaveAttack, new SpikeWaveAction(entity, eventScheduler, cancelAttackAction),
                sporeAttack, new SporeAction(entity, eventScheduler, cancelAttackAction),
                bladeAttack, new BladeAction(entity, eventScheduler, cancelAttackAction),
                blossomAction, new BlossomAction(entity, eventScheduler, cancelAttackAction)
        );

        this.moveLogic = new VoidBlossomMoveLogic(statusRegistry, entity, doBlossom, alvos);
    }

    public ActionGoal buildAttackGoal() {
        CooldownAction attackAction = new CooldownAction(moveLogic, Math.max(ConfiguracaoChefes.inteiro(ConfiguracaoChefes.FLOR + ".recarga_inicial_ticks"), 1));
        IActionStop onCancel = () -> {
            entity.level().broadcastEntityEvent(entity, stopAttackAnimation);
            attackAction.stop();
        };

        return new ActionGoal(
                () -> !cancelAttackAction.get(),
                null,
                attackAction,
                null,
                onCancel
        );
    }
}
