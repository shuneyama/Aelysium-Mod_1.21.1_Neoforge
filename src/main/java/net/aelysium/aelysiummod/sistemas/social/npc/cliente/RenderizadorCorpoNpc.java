package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class RenderizadorCorpoNpc extends LivingEntityRenderer<EntidadeNpcPersonalizado, PlayerModel<EntidadeNpcPersonalizado>> {

    private static final ResourceLocation TEXTURA_STEVE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    private static final ResourceLocation TEXTURA_ALEX = ResourceLocation.withDefaultNamespace("textures/entity/player/slim/alex.png");

    private final ResourceLocation texturaPadrao;

    public RenderizadorCorpoNpc(EntityRendererProvider.Context ctx, boolean slim) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim), 0.0f);
        this.texturaPadrao = slim ? TEXTURA_ALEX : TEXTURA_STEVE;
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel<>(ctx.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(ctx.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
                ctx.getModelManager()
        ));
        this.addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
        this.addLayer(new CustomHeadLayer<>(this, ctx.getModelSet(), ctx.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, ctx.getModelSet()));
    }

    @Override
    public void render(EntidadeNpcPersonalizado npc, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        prepararModelo(npc);
        int hurtTime = npc.hurtTime;
        int deathTime = npc.deathTime;
        npc.hurtTime = 0;
        npc.deathTime = 0;
        try {
            super.render(npc, entityYaw, partialTick, poseStack, buffer, packedLight);
        } finally {
            npc.hurtTime = hurtTime;
            npc.deathTime = deathTime;
        }
    }

    private void prepararModelo(EntidadeNpcPersonalizado npc) {
        PlayerModel<EntidadeNpcPersonalizado> modelo = this.getModel();
        modelo.setAllVisible(true);
        modelo.crouching = false;
        HumanoidModel.ArmPose principal = poseBraco(npc, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose secundaria = poseBraco(npc, InteractionHand.OFF_HAND);
        if (principal.isTwoHanded()) {
            secundaria = npc.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }
        if (npc.getMainArm() == HumanoidArm.RIGHT) {
            modelo.rightArmPose = principal;
            modelo.leftArmPose = secundaria;
        } else {
            modelo.rightArmPose = secundaria;
            modelo.leftArmPose = principal;
        }
    }

    private static HumanoidModel.ArmPose poseBraco(EntidadeNpcPersonalizado npc, InteractionHand mao) {
        ItemStack item = npc.getItemInHand(mao);
        if (item.isEmpty()) return HumanoidModel.ArmPose.EMPTY;
        if (item.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(item)) return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        HumanoidModel.ArmPose pose = IClientItemExtensions.of(item).getArmPose(npc, mao, item);
        return pose != null ? pose : HumanoidModel.ArmPose.ITEM;
    }

    @Override
    public ResourceLocation getTextureLocation(EntidadeNpcPersonalizado npc) {
        ResourceLocation personalizada = GerenciadorSkinNpc.getSkinTexture(npc.getId());
        return personalizada != null ? personalizada : texturaPadrao;
    }

    @Override
    protected void scale(EntidadeNpcPersonalizado npc, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    protected float getBob(EntidadeNpcPersonalizado npc, float partialTick) {
        return partialTick;
    }

    @Override
    protected boolean isBodyVisible(EntidadeNpcPersonalizado npc) {
        return true;
    }

    @Override
    protected boolean isShaking(EntidadeNpcPersonalizado npc) {
        return false;
    }

    @Override
    protected boolean shouldShowName(EntidadeNpcPersonalizado npc) {
        return false;
    }

    @Override
    protected void renderNameTag(EntidadeNpcPersonalizado npc, Component displayName, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int packedLight, float partialTick) {
    }
}
