package net.aelysium.aelysiummod.sistemas.mundo.mochila.cliente;

import net.aelysium.aelysiummod.sistemas.mundo.mochila.ConteudoMochila;
import net.aelysium.aelysiummod.sistemas.mundo.mochila.ModMochilas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ModeloMochila extends BakedModelWrapper<BakedModel> {

    private final ModelResourceLocation costas;
    private final ModelResourceLocation frente;
    private final ItemOverrides sobreposicoes = new ItemOverrides() {
        @Override
        public BakedModel resolve(BakedModel modelo, ItemStack pilha, ClientLevel mundo, LivingEntity entidade, int semente) {
            if (entidade != null) return modelo;
            int indice = ConteudoMochila.selecionado(pilha);
            BundleContents conteudo = pilha.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            if (indice < 0 || indice >= ConteudoMochila.quantidadeMostrar(conteudo.size())) return modelo;
            ItemStack escolhido = conteudo.getItemUnsafe(indice);
            Minecraft jogo = Minecraft.getInstance();
            BakedModel modeloEscolhido = jogo.getItemRenderer().getModel(escolhido, mundo, null, semente);
            if (modeloEscolhido == null || modeloEscolhido.isCustomRenderer()) return modelo;
            BakedModel modeloCostas = jogo.getModelManager().getModel(costas);
            BakedModel modeloFrente = jogo.getModelManager().getModel(frente);
            List<BakedQuad> quadros = new ArrayList<>();
            RandomSource aleatorio = RandomSource.create(42L);
            coletar(modeloCostas, aleatorio, quadros);
            coletar(modeloEscolhido, aleatorio, quadros);
            coletar(modeloFrente, aleatorio, quadros);
            return new Composto(originalModel, quadros, modeloEscolhido.usesBlockLight());
        }
    };

    public ModeloMochila(BakedModel base, ModelResourceLocation costas, ModelResourceLocation frente) {
        super(base);
        this.costas = costas;
        this.frente = frente;
    }

    @Override
    public ItemOverrides getOverrides() {
        return sobreposicoes;
    }

    private static void coletar(BakedModel modelo, RandomSource aleatorio, List<BakedQuad> destino) {
        ItemTransform transformacao = modelo.getTransforms().getTransform(ItemDisplayContext.GUI);
        Matrix4f matriz = new Matrix4f()
                .translate(0.5F, 0.5F, 0.5F)
                .translate(transformacao.translation)
                .rotate(new Quaternionf().rotationXYZ(
                        transformacao.rotation.x * ((float) Math.PI / 180F),
                        transformacao.rotation.y * ((float) Math.PI / 180F),
                        transformacao.rotation.z * ((float) Math.PI / 180F)))
                .scale(transformacao.scale)
                .translate(-0.5F, -0.5F, -0.5F);
        Quaternionf rotacao = matriz.getUnnormalizedRotation(new Quaternionf()).normalize();
        List<Direction> lados = new ArrayList<>(List.of(Direction.values()));
        lados.add(null);
        for (Direction lado : lados) {
            for (BakedQuad quadro : modelo.getQuads((BlockState) null, lado, aleatorio)) {
                destino.add(transformar(quadro, matriz, rotacao));
            }
        }
    }

    private static BakedQuad transformar(BakedQuad quadro, Matrix4f matriz, Quaternionf rotacao) {
        int[] vertices = quadro.getVertices().clone();
        Vector3f ponto = new Vector3f();
        Vector3f normal = new Vector3f();
        for (int i = 0; i < 4; i++) {
            int base = i * 8;
            ponto.set(Float.intBitsToFloat(vertices[base]), Float.intBitsToFloat(vertices[base + 1]), Float.intBitsToFloat(vertices[base + 2]));
            matriz.transformPosition(ponto);
            vertices[base] = Float.floatToRawIntBits(ponto.x);
            vertices[base + 1] = Float.floatToRawIntBits(ponto.y);
            vertices[base + 2] = Float.floatToRawIntBits(ponto.z);
            int empacotada = vertices[base + 7];
            normal.set(((byte) (empacotada & 0xFF)) / 127.0F, ((byte) ((empacotada >> 8) & 0xFF)) / 127.0F, ((byte) ((empacotada >> 16) & 0xFF)) / 127.0F);
            normal.rotate(rotacao).normalize();
            vertices[base + 7] = (empacotada & 0xFF000000)
                    | (((byte) Math.round(normal.x * 127.0F)) & 0xFF)
                    | ((((byte) Math.round(normal.y * 127.0F)) & 0xFF) << 8)
                    | ((((byte) Math.round(normal.z * 127.0F)) & 0xFF) << 16);
        }
        return new BakedQuad(vertices, quadro.getTintIndex(), quadro.getDirection(), quadro.getSprite(), quadro.isShade(), quadro.hasAmbientOcclusion());
    }

    private static class Composto extends BakedModelWrapper<BakedModel> {
        private final List<BakedQuad> quadros;
        private final boolean luzDeBloco;

        Composto(BakedModel base, List<BakedQuad> quadros, boolean luzDeBloco) {
            super(base);
            this.quadros = quadros;
            this.luzDeBloco = luzDeBloco;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState estado, Direction lado, RandomSource aleatorio) {
            return lado == null ? quadros : List.of();
        }

        @Override
        public boolean usesBlockLight() {
            return luzDeBloco;
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }
    }
}
