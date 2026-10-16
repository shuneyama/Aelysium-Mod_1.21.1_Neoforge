package net.aelysium.aelysiummod.conteudo.efeito.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class ManipuladorShaderCaecitas {

    private static final ResourceLocation SHADER_LOCATION =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "shaders/post/aelysium_caecitas.json");

    private static PostChain activeShader = null;
    private static int larguraCarregada = -1;
    private static int alturaCarregada = -1;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        boolean shouldBeActive = player != null && player.hasEffect(ModEfeitos.CAECITAS);

        if (shouldBeActive) {
            ensureLoaded(mc);
            if (activeShader != null) {
                int largura = mc.getWindow().getWidth();
                int altura = mc.getWindow().getHeight();
                if (largura != larguraCarregada || altura != alturaCarregada) {
                    activeShader.resize(largura, altura);
                    larguraCarregada = largura;
                    alturaCarregada = altura;
                }
            }
        } else if (activeShader != null) {
            removeShader(mc);
        }
    }

    private static void ensureLoaded(Minecraft mc) {
        PostChain current = mc.gameRenderer.currentEffect();
        if (current != null && current == activeShader) return;
        try {
            mc.gameRenderer.loadEffect(SHADER_LOCATION);
            activeShader = mc.gameRenderer.currentEffect();
            larguraCarregada = mc.getWindow().getWidth();
            alturaCarregada = mc.getWindow().getHeight();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar shader Caecitas: ", e);
            activeShader = null;
        }
    }

    private static void removeShader(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() == activeShader && activeShader != null) {
            mc.gameRenderer.shutdownEffect();
        }
        activeShader = null;
        larguraCarregada = -1;
        alturaCarregada = -1;
    }

    public static boolean processShader(float partialTick) {
        if (activeShader != null) {
            activeShader.process(partialTick);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            return true;
        }
        return false;
    }

    public static boolean isActive() {
        return activeShader != null;
    }
}