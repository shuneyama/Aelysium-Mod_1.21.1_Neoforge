package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public abstract class ModeloEquinoFilhote<T extends AbstractHorse> extends ModeloFilhoteHierarquico<T> {

    protected final ModelPart corpo;
    protected final ModelPart partesCabeca;
    protected final ModelPart traseiraDireita;
    protected final ModelPart traseiraEsquerda;
    protected final ModelPart dianteiraDireita;
    protected final ModelPart dianteiraEsquerda;
    protected final ModelPart cauda;
    protected float comer;
    protected float empinar;
    protected float mastigar;

    protected ModeloEquinoFilhote(ModelPart raiz, ModelPart partesCabeca, ModelPart traseiraDireita, ModelPart dianteiraDireita, ModelPart traseiraEsquerda, ModelPart dianteiraEsquerda, ModelPart cauda) {
        super(raiz);
        this.corpo = raiz.getChild("body");
        this.partesCabeca = partesCabeca;
        this.traseiraDireita = traseiraDireita;
        this.traseiraEsquerda = traseiraEsquerda;
        this.dianteiraDireita = dianteiraDireita;
        this.dianteiraEsquerda = dianteiraEsquerda;
        this.cauda = cauda;
    }

    @Override
    public void prepareMobModel(T entidade, float limbSwing, float limbSwingAmount, float partialTick) {
        this.comer = entidade.getEatAnim(partialTick);
        this.empinar = entidade.getStandAnim(partialTick);
        this.mastigar = entidade.getMouthAnim(partialTick);
    }

    @Override
    public void setupAnim(T entidade, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        UtilFilhotes.resetar(raiz);
        float yawLimitado = Mth.clamp(netHeadYaw, -20.0F, 20.0F);
        float pitchRad = headPitch * Mth.DEG_TO_RAD;
        float velocidade = limbSwingAmount;
        float posicao = UtilFilhotes.posicaoAndar(limbSwing);
        if (velocidade > 0.2F) {
            pitchRad += Mth.cos(posicao * 0.8F) * 0.15F * velocidade;
        }
        float naoEmpinar = 1.0F - empinar;
        partesCabeca.xRot = Mth.PI / 6.0F + pitchRad;
        partesCabeca.yRot = yawLimitado * Mth.DEG_TO_RAD;
        float multiplicadorAgua = entidade.isInWater() ? 0.2F : 1.0F;
        float pernaAnim = Mth.cos(multiplicadorAgua * posicao * 0.6662F + Mth.PI);
        float pernaRot = pernaAnim * 0.8F * velocidade;
        float anguloBase = (1.0F - Math.max(empinar, comer)) * (Mth.PI / 6.0F + pitchRad + mastigar * Mth.sin(ageInTicks) * 0.05F);
        partesCabeca.xRot = empinar * (0.2617994F + pitchRad) + comer * (2.1816616F + Mth.sin(ageInTicks) * 0.05F) + anguloBase;
        partesCabeca.yRot = empinar * yawLimitado * Mth.DEG_TO_RAD + (1.0F - Math.max(empinar, comer)) * partesCabeca.yRot;
        posicionarCabeca(comer, empinar);
        corpo.xRot = empinar * (-Mth.PI / 4.0F) + naoEmpinar * corpo.xRot;
        dianteiraEsquerda.y -= deslocamentoYEmpinado() * empinar;
        dianteiraEsquerda.z += deslocamentoZEmpinado() * empinar;
        dianteiraDireita.y = dianteiraEsquerda.y;
        dianteiraDireita.z = dianteiraEsquerda.z;
        float anguloEmpinar = anguloPernaEmpinado() * empinar;
        float balanco = Mth.cos(ageInTicks * 0.6F + Mth.PI);
        float rotacaoEmpinado = rotacaoXPernaEmpinada();
        float pernaDireita = (rotacaoEmpinado + balanco) * empinar + pernaRot * naoEmpinar;
        float pernaEsquerda = (rotacaoEmpinado - balanco) * empinar - pernaRot * naoEmpinar;
        traseiraEsquerda.xRot = anguloEmpinar - pernaAnim * 0.5F * velocidade * naoEmpinar;
        traseiraDireita.xRot = anguloEmpinar + pernaAnim * 0.5F * velocidade * naoEmpinar;
        dianteiraEsquerda.xRot = pernaDireita;
        dianteiraDireita.xRot = pernaEsquerda;
        ajustarPernasEmpinado(empinar);
        float escala = entidade.getAgeScale();
        cauda.xRot = rotacaoCauda() + Mth.PI / 6.0F + velocidade * 0.75F;
        cauda.y += velocidade * escala;
        cauda.z += velocidade * 2.0F * escala;
        cauda.yRot = entidade.tailCounter > 0 ? Mth.cos(ageInTicks * 0.7F) : 0.0F;
    }

    protected void ajustarPernasEmpinado(float empinar) {
    }

    protected float anguloPernaEmpinado() {
        return 0.2617994F;
    }

    protected float deslocamentoYEmpinado() {
        return 12.0F;
    }

    protected float deslocamentoZEmpinado() {
        return 4.0F;
    }

    protected float rotacaoXPernaEmpinada() {
        return -Mth.PI / 3.0F;
    }

    protected float rotacaoCauda() {
        return 0.0F;
    }

    protected void posicionarCabeca(float comer, float empinar) {
        partesCabeca.y += Mth.lerp(comer, Mth.lerp(empinar, 0.0F, -8.0F), 7.0F);
        partesCabeca.z = Mth.lerp(empinar, partesCabeca.z, -4.0F);
    }
}
