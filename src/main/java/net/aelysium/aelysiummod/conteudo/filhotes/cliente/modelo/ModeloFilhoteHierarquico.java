package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

public abstract class ModeloFilhoteHierarquico<T extends Entity> extends HierarchicalModel<T> {

    protected final ModelPart raiz;

    protected ModeloFilhoteHierarquico(ModelPart raiz) {
        this.raiz = raiz;
    }

    @Override
    public ModelPart root() {
        return raiz;
    }
}
