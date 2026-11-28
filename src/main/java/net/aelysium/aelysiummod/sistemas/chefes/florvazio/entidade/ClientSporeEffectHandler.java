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

public class ClientSporeEffectHandler implements IEntityEventHandler {
    private final VoidBlossomEntity entity;
    private final EventScheduler eventScheduler;

    private final ClientParticleBuilder projectileParticles = new ClientParticleBuilder(RegistroFlorVazio.OBSIDILITH_BURST.get())
            .color(Vec3Colors.GREEN)
            .colorVariation(0.4)
            .scale(0.5f)
            .brightness(RegistroFlorVazio.BRILHO_TOTAL);

    public ClientSporeEffectHandler(VoidBlossomEntity entity, EventScheduler eventScheduler) {
        this.entity = entity;
        this.eventScheduler = eventScheduler;
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status == VoidBlossomAttacks.sporeAttack)
            eventScheduler.addEvent(new TimedEvent(this::spawnParticles, 25, 15, () -> false));
    }

    private void spawnParticles(){
        Vec3 pos = entity.getEyePosition().add(RandomUtils.randVec().scale(3.0)).subtract(entity.getForward().scale(2.0));
        Vec3 vel = VecUtils.yAxis.scale(0.1);
        projectileParticles.build(pos, vel);
    }
}
