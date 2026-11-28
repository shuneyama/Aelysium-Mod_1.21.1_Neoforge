package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.EfeitosAtaque;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityTick;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class VoidBlossomSpikeTick implements IEntityTick<ServerLevel> {
    private final VoidBlossomEntity entity;

    public VoidBlossomSpikeTick(VoidBlossomEntity entity) {
        this.entity = entity;
    }

    @Override
    public void tick(ServerLevel level) {
        AABB spikeHitbox = new AABB(entity.position(), entity.position()).inflate(3.0, 3.0, 3.0).move(0.0, 1.5, 0.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, spikeHitbox, livingEntity -> livingEntity != entity);

        for (LivingEntity target : targets){
            if (target.position().distanceToSqr(entity.position()) < 9.0)
                EfeitosAtaque.ferir(entity, target, entity.level().damageSources().thorns(entity), ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_CONTATO));
        }
    }
}
