package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AtaqueChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.acao.IActionWithCooldown;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox.HitboxId;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.ProjectileThrower;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.SporeBallProjectile;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.util.ExemptEntities;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventScheduler;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventSeries;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.SoundUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.function.Supplier;

public class SporeAction implements IActionWithCooldown {
    private final VoidBlossomEntity entity;
    private final EventScheduler eventScheduler;
    private final Supplier<Boolean> shouldCancel;

    public SporeAction(VoidBlossomEntity entity, EventScheduler eventScheduler, Supplier<Boolean> shouldCancel) {
        this.entity = entity;
        this.eventScheduler = eventScheduler;
        this.shouldCancel = shouldCancel;
    }

    @Override
    public int perform() {
        LivingEntity target = entity.getTarget();
        if (!(target instanceof ServerPlayer)) return 80;
        AtaqueChefe ataque = ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_ESPOROS);
        List<LivingEntity> alvos = entity.getAlvos().alvos(ataque.alvos());

        eventScheduler.addEvent(
                new EventSeries(
                        new TimedEvent(
                                () -> entity.getEntityData().set(VoidBlossomEntity.hitbox, HitboxId.Spore.getId()),
                                20,
                                1,
                                shouldCancel
                        ),
                        new TimedEvent(
                                () -> entity.getEntityData().set(VoidBlossomEntity.hitbox, HitboxId.Idle.getId()),
                                27,
                                1,
                                () -> false
                        )
                )
        );

        eventScheduler.addEvent(
                new TimedEvent(
                        () -> SoundUtils.playSound(((ServerPlayer) target).serverLevel(), entity.position(), RegistroFlorVazio.SPORE_PREPARE.get(), SoundSource.HOSTILE, 1.5f, 32, null),
                        26
                )
        );

        eventScheduler.addEvent(
                new TimedEvent(
                        () -> alvos.forEach(alvo -> new ProjectileThrower(
                                () -> {
                                    SporeBallProjectile projectile = new SporeBallProjectile(entity, entity.level(), new ExemptEntities(List.of(RegistroFlorVazio.VOID_BLOSSOM.get())));
                                    projectile.setPos(entity.getEyePosition());
                                    return new ProjectileThrower.ProjectileData(projectile, 0.75f, 0f, 0.2);
                                }
                        ).throwProjectile(alvo.getEyePosition())),
                        45,
                        1,
                        shouldCancel
                )
        );

        return ataque.recarga();
    }
}
