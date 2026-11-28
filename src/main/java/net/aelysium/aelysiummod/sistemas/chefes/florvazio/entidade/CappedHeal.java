package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ChefeAelysium;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.EventosChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.StatusChefe;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.util.UtilFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.EntityAdapter;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.EntityStats;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityTick;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

import java.util.List;

public class CappedHeal implements IEntityTick<ServerLevel> {
    private final Mob entity;
    private final List<Float> hpMilestones;
    private final String chefe;
    private final EntityAdapter adapter;
    private final EntityStats stats;

    public CappedHeal(Mob entity, List<Float> hpMilestones, String chefe) {
        this.entity = entity;
        this.hpMilestones = hpMilestones;
        this.chefe = chefe;
        this.adapter = new EntityAdapter(entity);
        this.stats = new EntityStats(entity);
    }

    @Override
    public void tick(ServerLevel level) {
        StatusChefe status = ConfiguracaoChefes.status(chefe);
        if (entity.getTarget() == null && entity instanceof ChefeAelysium chefeAelysium && EventosChefes.podeCurar(chefeAelysium, status))
            UtilFlorVazio.cappedHeal(adapter, stats, hpMilestones, status.curaPorTick(), entity::heal);
    }
}
