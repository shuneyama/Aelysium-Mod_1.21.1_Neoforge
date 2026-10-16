package net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.domo;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ShadersDomo {

    private static ShaderInstance domoRegiaoShader;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "domo_regiao"),
                        DefaultVertexFormat.POSITION_TEX_COLOR
                ),
                shader -> domoRegiaoShader = shader
        );
    }

    public static ShaderInstance domoRegiao() {
        return domoRegiaoShader;
    }
}
