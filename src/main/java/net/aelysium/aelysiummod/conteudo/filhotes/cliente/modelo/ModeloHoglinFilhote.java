package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;

public class ModeloHoglinFilhote<T extends Mob & HoglinBase> extends HoglinModel<T> {

    private final ModelPart cabeca;

    public ModeloHoglinFilhote(ModelPart raiz) {
        super(raiz);
        this.cabeca = raiz.getChild("head");
    }

    @Override
    public void setupAnim(T entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entidade, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int restante = entidade.getAttackAnimationRemainingTicks();
        float fator = 1.0F - Mth.abs(10 - 2 * restante) / 10.0F;
        cabeca.y = 13.0F + fator * 2.5F;
        this.young = false;
    }
}
