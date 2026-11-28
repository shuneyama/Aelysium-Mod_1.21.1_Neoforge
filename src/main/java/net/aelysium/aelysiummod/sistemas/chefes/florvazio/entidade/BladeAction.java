package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AtaqueChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.acao.IActionWithCooldown;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox.HitboxId;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.ProjectileThrower;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.PetalBladeProjectile;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventScheduler;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventSeries;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.MathUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.RandomUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.SoundUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public class BladeAction implements IActionWithCooldown {
    private final VoidBlossomEntity entity;
    private final EventScheduler eventScheduler;
    private final Supplier<Boolean> shouldCancel;

    public BladeAction(VoidBlossomEntity entity, EventScheduler eventScheduler, Supplier<Boolean> shouldCancel){
        this.entity = entity;
        this.eventScheduler = eventScheduler;
        this.shouldCancel = shouldCancel;
    }

    @Override
    public int perform() {
        LivingEntity target = entity.getTarget();
        if (!(target instanceof ServerPlayer)) return 80;
        AtaqueChefe ataque = ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_LAMINAS);
        List<LivingEntity> alvos = entity.getAlvos().alvos(ataque.alvos(), ataque.miraVoadores());

        eventScheduler.addEvent(
                new EventSeries(
                        new TimedEvent(
                                () -> entity.getEntityData().set(VoidBlossomEntity.hitbox, HitboxId.Petal.getId()),
                                10,
                                1,
                                shouldCancel
                        ),
                        new TimedEvent(
                                () -> entity.getEntityData().set(VoidBlossomEntity.hitbox, HitboxId.Idle.getId()),
                                90
                        )
                )
        );

        Runnable thrower = () -> alvos.forEach(alvo -> {
            Vec3 eyePos = alvo.getBoundingBox().getCenter();
            Vec3 dir = entity.getEyePosition().subtract(eyePos);
            Vec3 left = dir.cross(VecUtils.yAxis).normalize();
            double rotation = RandomUtils.randSign() * 20.0;
            Vec3 angled = VecUtils.rotateVector(left, dir, rotation);
            Vec3 lineStart = eyePos.add(angled.scale(7.0));
            Vec3 lineEnd = eyePos.add(angled.scale(-7.0));
            ProjectileThrower projectileThrower = new ProjectileThrower(
                    () -> {
                        PetalBladeProjectile projectile = new PetalBladeProjectile(entity, entity.level(), livingEntity -> {}, List.of(entity.getType()), (float) rotation);
                        projectile.setPos(entity.getEyePosition().add(VecUtils.yAxis));
                        projectile.setNoGravity(true);
                        return new ProjectileThrower.ProjectileData(projectile, 0.9f, 0f, 0.0);
                    }
            );

            SoundUtils.playSound((ServerLevel) entity.level(), entity.position(), RegistroFlorVazio.PETAL_BLADE_SOM.get(), SoundSource.HOSTILE, 3.0f, SoundUtils.randomPitch(alvo.getRandom()), 64.0, null);
            MathUtils.lineCallback(lineStart, lineEnd, 11, (vec3, integer) -> projectileThrower.throwProjectile(vec3));
        });

        eventScheduler.addEvent(new TimedEvent(thrower, 28, 1, shouldCancel));
        eventScheduler.addEvent(new TimedEvent(thrower, 52, 1, shouldCancel));
        eventScheduler.addEvent(new TimedEvent(thrower, 75, 1, shouldCancel));

        return ataque.recarga();
    }
}
