package net.aelysium.aelysiummod.conteudo.filhotes.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UtilFilhotes {

    private static final Map<ResourceLocation, Boolean> EXISTENCIA = new ConcurrentHashMap<>();

    private UtilFilhotes() {
    }

    public static ResourceLocation textura(String caminho) {
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/entity/" + caminho + ".png");
    }

    public static ModelLayerLocation camada(String nome) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, nome), "main");
    }

    public static boolean existe(ResourceLocation local) {
        return EXISTENCIA.computeIfAbsent(local, l -> Minecraft.getInstance().getResourceManager().getResource(l).isPresent());
    }

    public static void limparCache() {
        EXISTENCIA.clear();
    }

    public static ResourceLocation versaoFilhote(ResourceLocation adulto, String pasta, ResourceLocation reserva) {
        if (!"minecraft".equals(adulto.getNamespace())) return reserva;
        String caminho = adulto.getPath();
        int barra = caminho.lastIndexOf('/');
        String arquivo = caminho.substring(barra + 1);
        if (arquivo.endsWith(".png")) arquivo = arquivo.substring(0, arquivo.length() - 4);
        ResourceLocation filhote = textura("filhotes/" + pasta + "/" + arquivo + "_baby");
        return existe(filhote) ? filhote : reserva;
    }

    public static void vazias(PartDefinition pai, String... nomes) {
        for (String nome : nomes) {
            pai.addOrReplaceChild(nome, CubeListBuilder.create(), PartPose.ZERO);
        }
    }

    public static void resetar(ModelPart raiz) {
        raiz.getAllParts().forEach(ModelPart::resetPose);
    }

    public static void cabeca(ModelPart cabeca, float pitch, float yaw) {
        cabeca.xRot = pitch * Mth.DEG_TO_RAD;
        cabeca.yRot = yaw * Mth.DEG_TO_RAD;
    }

    public static void pernas(ModelPart traseiraDireita, ModelPart traseiraEsquerda, ModelPart dianteiraDireita, ModelPart dianteiraEsquerda, float posicao, float velocidade) {
        traseiraDireita.xRot = Mth.cos(posicao * 0.6662F) * 1.4F * velocidade;
        traseiraEsquerda.xRot = Mth.cos(posicao * 0.6662F + Mth.PI) * 1.4F * velocidade;
        dianteiraDireita.xRot = Mth.cos(posicao * 0.6662F + Mth.PI) * 1.4F * velocidade;
        dianteiraEsquerda.xRot = Mth.cos(posicao * 0.6662F) * 1.4F * velocidade;
    }

    public static float posicaoAndar(float limbSwing) {
        return limbSwing;
    }
}
