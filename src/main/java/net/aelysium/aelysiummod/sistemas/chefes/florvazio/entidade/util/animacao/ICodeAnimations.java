package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.animacao;

import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.ModeloBedrock;
import net.minecraft.world.entity.Entity;

public interface ICodeAnimations<T extends Entity> {
    void animate(T animatable, ModeloBedrock model, float partialTick);
}
