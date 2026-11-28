package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityTick;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.util.VanillaCopiesServer;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventScheduler;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.RandomUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class VoidBlossomDropExpDeathTick implements IEntityTick<ServerLevel> {
    private final LivingEntity entity;
    private final EventScheduler eventScheduler;
    private final int exp;

    public VoidBlossomDropExpDeathTick(LivingEntity entity, EventScheduler eventScheduler, int exp) {
        this.entity = entity;
        this.eventScheduler = eventScheduler;
        this.exp = exp;
    }

    @Override
    public void tick(ServerLevel level) {
        if (entity.deathTime == 1)
            scheduleExp();
    }

    private void scheduleExp(){
        int expTicks = 20;
        int expPerTick = (int) (exp / (float) expTicks);
        Vec3 fallDirection = VecUtils.planeProject(entity.getForward(), VecUtils.yAxis).yRot(180f);
        Vec3 originPos = entity.position().add(VecUtils.yAxis.scale(2.0));

        eventScheduler.addEvent(
                new TimedEvent(
                        () ->{
                            Vec3 pos = originPos
                                    .add(RandomUtils.randVec().scale(2.0))
                                    .add(fallDirection.scale(RandomUtils.randDouble(6.0) + 6.0));

                            VanillaCopiesServer.awardExperience(
                                    expPerTick,
                                    pos,
                                    entity.level()
                            );
                        },
                        (int) (LightBlockRemover.deathMaxAge - expTicks - 1),
                        expTicks,
                        () -> false
                )
        );
    }
}
