package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;

public class ModeloPiglinFilhote<T extends Mob> extends PiglinModel<T> {

    private static final float AJUSTE_ORELHA = 25.0F * (float) (Math.PI / 180.0);

    private final AjusteHumanoideFilhote ajuste;
    private final ModelPart orelhaEsquerda;
    private final ModelPart orelhaDireita;

    public ModeloPiglinFilhote(ModelPart raiz) {
        super(raiz);
        this.ajuste = new AjusteHumanoideFilhote(this);
        this.orelhaEsquerda = this.head.getChild("left_ear");
        this.orelhaDireita = this.head.getChild("right_ear");
    }

    @Override
    public void setupAnim(T entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entidade, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        orelhaEsquerda.zRot += AJUSTE_ORELHA;
        orelhaDireita.zRot -= AJUSTE_ORELHA;
        ajuste.aplicar();
    }
}
