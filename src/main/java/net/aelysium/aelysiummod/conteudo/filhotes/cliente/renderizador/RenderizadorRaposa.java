package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloRaposaFilhote;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderizadorRaposa extends FoxRenderer {

    private static final ResourceLocation VERMELHA = UtilFilhotes.textura("filhotes/fox/fox_baby");
    private static final ResourceLocation VERMELHA_DORMINDO = UtilFilhotes.textura("filhotes/fox/fox_sleep_baby");
    private static final ResourceLocation NEVE = UtilFilhotes.textura("filhotes/fox/fox_snow_baby");
    private static final ResourceLocation NEVE_DORMINDO = UtilFilhotes.textura("filhotes/fox/fox_snow_sleep_baby");

    private final RenderizadorRaposa filhote;

    public RenderizadorRaposa(EntityRendererProvider.Context contexto) {
        this(contexto, false);
    }

    private RenderizadorRaposa(EntityRendererProvider.Context contexto, boolean ehFilhote) {
        super(contexto);
        if (ehFilhote) {
            this.model = new ModeloRaposaFilhote(contexto.bakeLayer(CamadasFilhotes.RAPOSA));
            this.layers.clear();
            this.addLayer(new CamadaItem(this, contexto.getItemInHandRenderer()));
            this.filhote = null;
        } else {
            this.filhote = new RenderizadorRaposa(contexto, true);
        }
    }

    @Override
    public void render(Fox entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
        if (filhote != null && entidade.isBaby()) {
            filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
            return;
        }
        super.render(entidade, yaw, parcial, poseStack, buffer, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(Fox entidade) {
        if (filhote != null) return super.getTextureLocation(entidade);
        boolean dormindo = entidade.isSleeping();
        if (entidade.getVariant() == Fox.Type.SNOW) return dormindo ? NEVE_DORMINDO : NEVE;
        return dormindo ? VERMELHA_DORMINDO : VERMELHA;
    }

    private static class CamadaItem extends RenderLayer<Fox, FoxModel<Fox>> {

        private final ItemInHandRenderer renderizadorItem;

        CamadaItem(RenderLayerParent<Fox, FoxModel<Fox>> pai, ItemInHandRenderer renderizadorItem) {
            super(pai);
            this.renderizadorItem = renderizadorItem;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, Fox raposa, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
            ItemStack item = raposa.getItemBySlot(EquipmentSlot.MAINHAND);
            if (item.isEmpty()) return;
            boolean dormindo = raposa.isSleeping();
            FoxModel<Fox> modelo = this.getParentModel();
            poseStack.pushPose();
            poseStack.translate(modelo.head.x / 16.0F, modelo.head.y / 16.0F, modelo.head.z / 16.0F);
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.mulPose(Axis.ZP.rotation(raposa.getHeadRollAngle(parcial)));
            poseStack.mulPose(Axis.YP.rotationDegrees(netHeadYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));
            if (dormindo) {
                poseStack.translate(0.4F, 0.26F, 0.15F);
            } else {
                poseStack.translate(0.06F, 0.26F, -0.5F);
            }
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            if (dormindo) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            renderizadorItem.renderItem(raposa, item, ItemDisplayContext.GROUND, false, poseStack, buffer, luz);
            poseStack.popPose();
        }
    }
}
