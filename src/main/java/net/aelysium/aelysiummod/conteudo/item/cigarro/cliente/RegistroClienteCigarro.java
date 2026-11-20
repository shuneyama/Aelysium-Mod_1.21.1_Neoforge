package net.aelysium.aelysiummod.conteudo.item.cigarro.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModItens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class RegistroClienteCigarro {

    @SubscribeEvent
    public static void aoConfigurarCliente(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("curios")) {
            top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(
                    ModItens.CIGARRO.get(), RenderizadorCigarroCurio::new);
        }
    }

    @SubscribeEvent
    public static void registrarDefinicoesCamada(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModeloCigarro.LAYER_LOCATION, ModeloCigarro::createBodyLayer);
    }

    @SubscribeEvent
    public static void registrarModelosAdicionais(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/isqueiro_corpo_apagado")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/isqueiro_corpo_aceso")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/isqueiro_tampa")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/cigarro_novo")));
        event.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "item/cigarro_usado")));
    }

    @SubscribeEvent
    public static void registrarExtensoesCliente(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return RenderizadorItemIsqueiro.INSTANCE;
            }

            @Override
            public boolean applyForgeHandTransform(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                     net.minecraft.client.player.LocalPlayer player,
                                                     net.minecraft.world.entity.HumanoidArm arm,
                                                     net.minecraft.world.item.ItemStack itemInHand,
                                                     float partialTick, float equipProcess, float swingProcess) {
                int i = arm == net.minecraft.world.entity.HumanoidArm.RIGHT ? 1 : -1;
                poseStack.translate((float) i * 0.56F, -0.52F, -0.72F);
                return true;
            }
        }, ModItens.ISQUEIRO.get());

        event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return RenderizadorItemCigarro.INSTANCE;
            }

            @Override
            public boolean applyForgeHandTransform(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                     net.minecraft.client.player.LocalPlayer player,
                                                     net.minecraft.world.entity.HumanoidArm arm,
                                                     net.minecraft.world.item.ItemStack itemInHand,
                                                     float partialTick, float equipProcess, float swingProcess) {
                int i = arm == net.minecraft.world.entity.HumanoidArm.RIGHT ? 1 : -1;

                float progresso = 0F;
                if (player.isUsingItem() && player.getUseItem() == itemInHand) {
                    int decorridos = itemInHand.getUseDuration(player) - player.getUseItemRemainingTicks();
                    progresso = net.minecraft.util.Mth.clamp(decorridos / 60F, 0F, 1F);
                }

                float x = net.minecraft.util.Mth.lerp(progresso, 0.5F, 0.12F);
                float y = net.minecraft.util.Mth.lerp(progresso, -0.5F, -0.2F);
                float z = net.minecraft.util.Mth.lerp(progresso, -0.7F, -0.3F);

                poseStack.translate((float) i * x, y, z);
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(net.minecraft.util.Mth.lerp(progresso, 0F, -20F)));
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) i * net.minecraft.util.Mth.lerp(progresso, 0F, -15F)));
                return true;
            }
        }, ModItens.CIGARRO.get());
    }
}
