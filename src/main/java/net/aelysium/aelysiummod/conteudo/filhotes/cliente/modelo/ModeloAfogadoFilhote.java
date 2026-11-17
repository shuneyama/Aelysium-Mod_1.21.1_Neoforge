package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Zombie;

public class ModeloAfogadoFilhote<T extends Zombie> extends DrownedModel<T> {

    private final AjusteHumanoideFilhote ajuste;

    public ModeloAfogadoFilhote(ModelPart raiz) {
        super(raiz);
        this.ajuste = new AjusteHumanoideFilhote(this);
    }

    @Override
    public void setupAnim(T entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entidade, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        ajuste.aplicar();
    }
}
