package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityEventHandler;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventScheduler;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.ClientParticleBuilder;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.RandomUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.Vec3Colors;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import net.minecraft.world.phys.Vec3;

public class ClientDeathEffectHandler implements IEntityEventHandler {
    private final VoidBlossomEntity entity;
    private final EventScheduler eventScheduler;

    private final ClientParticleBuilder deathParticle = new ClientParticleBuilder(RegistroFlorVazio.FLUFF.get())
            .color(Vec3Colors.DARK_GREY)
            .colorVariation(0.1)
            .age(20, 30)
            .scale(0.3f);

    public ClientDeathEffectHandler(VoidBlossomEntity entity, EventScheduler eventScheduler) {
        this.entity = entity;
        this.eventScheduler = eventScheduler;
    }

    @Override
    public void handleEntityEvent(byte status) {
        if ((int) status == 3){
            int delay = 3;
            Vec3 fallDirection = VecUtils.planeProject(entity.getForward(), VecUtils.yAxis).yRot(180f);
            Vec3 originPos = entity.position().add(VecUtils.yAxis.scale(2.0));
            eventScheduler.addEvent(
                    new TimedEvent(
                            () -> {
                                Vec3 pos = originPos
                                        .add(RandomUtils.randVec().scale(5.0))
                                        .add(fallDirection.scale(RandomUtils.randDouble(6.0) + 6.0));
                                Vec3 vel = RandomUtils.randVec().add(VecUtils.yAxis).scale(0.05);
                                deathParticle.build(pos, vel);
                            },
                            delay,
                            (int) LightBlockRemover.deathMaxAge - delay,
                            () -> false
                    )
            );
        }
    }
}
