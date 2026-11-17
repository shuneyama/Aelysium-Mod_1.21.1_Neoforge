package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;

public final class AjusteHumanoideFilhote {

    private final HumanoidModel<?> modelo;
    private final PartPose cabeca;
    private final PartPose corpo;
    private final PartPose bracoDireito;
    private final PartPose bracoEsquerdo;
    private final PartPose pernaDireita;
    private final PartPose pernaEsquerda;

    public AjusteHumanoideFilhote(HumanoidModel<?> modelo) {
        this.modelo = modelo;
        this.cabeca = modelo.head.storePose();
        this.corpo = modelo.body.storePose();
        this.bracoDireito = modelo.rightArm.storePose();
        this.bracoEsquerdo = modelo.leftArm.storePose();
        this.pernaDireita = modelo.rightLeg.storePose();
        this.pernaEsquerda = modelo.leftLeg.storePose();
    }

    public void aplicar() {
        modelo.head.y = cabeca.y + modelo.head.y;
        modelo.body.y = corpo.y + modelo.body.y;
        modelo.rightArm.x = bracoDireito.x + (modelo.rightArm.x + 5.0F) * 0.5F;
        modelo.rightArm.y = bracoDireito.y + (modelo.rightArm.y - 2.0F);
        modelo.rightArm.z = bracoDireito.z + modelo.rightArm.z * 0.5F;
        modelo.leftArm.x = bracoEsquerdo.x + (modelo.leftArm.x - 5.0F) * 0.5F;
        modelo.leftArm.y = bracoEsquerdo.y + (modelo.leftArm.y - 2.0F);
        modelo.leftArm.z = bracoEsquerdo.z + modelo.leftArm.z * 0.5F;
        modelo.rightLeg.y = pernaDireita.y + (modelo.rightLeg.y - 12.0F);
        modelo.rightLeg.z = pernaDireita.z + modelo.rightLeg.z;
        modelo.leftLeg.y = pernaEsquerda.y + (modelo.leftLeg.y - 12.0F);
        modelo.leftLeg.z = pernaEsquerda.z + modelo.leftLeg.z;
        modelo.hat.copyFrom(modelo.head);
        modelo.young = false;
    }
}
