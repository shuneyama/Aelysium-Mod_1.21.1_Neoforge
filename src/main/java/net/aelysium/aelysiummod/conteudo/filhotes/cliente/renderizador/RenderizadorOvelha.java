package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloOvelhaFilhote;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

public class RenderizadorOvelha extends SheepRenderer {

    private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/sheep/sheep_baby");
    private static final ResourceLocation LA_FILHOTE = UtilFilhotes.textura("filhotes/sheep/sheep_wool_baby");

    private final RenderizadorOvelha filhote;

    public RenderizadorOvelha(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorOvelha(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloOvelhaFilhote(contexto.bakeLayer(CamadasFilhotes.OVELHA));
            this.layers.clear();
            this.addLayer(new CamadaLa(this, new ModeloOvelhaFilhote(contexto.bakeLayer(CamadasFilhotes.OVELHA_LA))));
            this.filhote = null;
        } else {
            this.layers.add(0, new CamadaSubpelo(this, new SheepModel<>(contexto.bakeLayer(ModelLayers.SHEEP))));
            this.filhote = new RenderizadorOvelha(contexto, true);
        }
    }

    @Override
    public void render(Sheep entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Sheep entidade) {
        return filhote == null ? FILHOTE : super.getTextureLocation(entidade);
    }

    private static class CamadaLa extends RenderLayer<Sheep, SheepModel<Sheep>> {

        private final ModeloOvelhaFilhote modelo;

        CamadaLa(RenderLayerParent<Sheep, SheepModel<Sheep>> pai, ModeloOvelhaFilhote modelo) {
            super(pai);
            this.modelo = modelo;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, Sheep ovelha, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
            if (ovelha.isSheared()) return;
            if (ovelha.isInvisible()) {
                if (Minecraft.getInstance().shouldEntityAppearGlowing(ovelha)) {
                    this.getParentModel().copyPropertiesTo(modelo);
                    modelo.prepareMobModel(ovelha, limbSwing, limbSwingAmount, parcial);
                    modelo.setupAnim(ovelha, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                    VertexConsumer consumidor = buffer.getBuffer(RenderType.outline(LA_FILHOTE));
                    modelo.renderToBuffer(poseStack, consumidor, luz, LivingEntityRenderer.getOverlayCoords(ovelha, 0.0F), -16777216);
                }
                return;
            }
            int cor;
            if (ovelha.hasCustomName() && "jeb_".equals(ovelha.getName().getString())) {
                int passo = ovelha.tickCount / 25 + ovelha.getId();
                int total = DyeColor.values().length;
                float fator = ((float) (ovelha.tickCount % 25) + parcial) / 25.0F;
                cor = FastColor.ARGB32.lerp(fator, Sheep.getColor(DyeColor.byId(passo % total)), Sheep.getColor(DyeColor.byId((passo + 1) % total)));
            } else {
                cor = Sheep.getColor(ovelha.getColor());
            }
            coloredCutoutModelCopyLayerRender(this.getParentModel(), modelo, LA_FILHOTE, poseStack, buffer, luz, ovelha, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, parcial, cor);
        }
    }

    private static class CamadaSubpelo extends RenderLayer<Sheep, SheepModel<Sheep>> {

        private static final ResourceLocation SUBPELO = UtilFilhotes.textura("sheep/sheep_wool_undercoat");

        private final SheepModel<Sheep> modelo;

        CamadaSubpelo(RenderLayerParent<Sheep, SheepModel<Sheep>> pai, SheepModel<Sheep> modelo) {
            super(pai);
            this.modelo = modelo;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, Sheep ovelha, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
            if (ovelha.isInvisible() || ovelha.isBaby()) return;
            boolean jeb = ovelha.hasCustomName() && "jeb_".equals(ovelha.getName().getString());
            if (!jeb && ovelha.getColor() == DyeColor.WHITE) return;
            int cor;
            if (jeb) {
                int passo = ovelha.tickCount / 25 + ovelha.getId();
                int total = DyeColor.values().length;
                float fator = ((float) (ovelha.tickCount % 25) + parcial) / 25.0F;
                cor = FastColor.ARGB32.lerp(fator, Sheep.getColor(DyeColor.byId(passo % total)), Sheep.getColor(DyeColor.byId((passo + 1) % total)));
            } else {
                cor = Sheep.getColor(ovelha.getColor());
            }
            coloredCutoutModelCopyLayerRender(this.getParentModel(), modelo, SUBPELO, poseStack, buffer, luz, ovelha, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, parcial, cor);
        }
    }
}
