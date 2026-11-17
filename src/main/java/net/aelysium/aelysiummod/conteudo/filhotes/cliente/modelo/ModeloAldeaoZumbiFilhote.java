package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Zombie;

public class ModeloAldeaoZumbiFilhote<T extends Zombie> extends ZombieVillagerModel<T> {

    private final AjusteHumanoideFilhote ajuste;

    public ModeloAldeaoZumbiFilhote(ModelPart raiz) {
        super(raiz);
        this.ajuste = new AjusteHumanoideFilhote(this);
    }

    @Override
    public void setupAnim(T entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entidade, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        ajuste.aplicar();
    }
}
