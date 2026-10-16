package net.aelysium.aelysiummod.sistemas.rp.morte.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.aelysium.aelysiummod.nucleo.cliente.VolumeSomCliente;

@OnlyIn(Dist.CLIENT)
public class RenderizadorSobreposicaoMorte {

    private static final ResourceLocation SOM_DOWNER =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "morte.downer");
    private static final long SOM_ATRASO_MS = 500L;

    public static void triggerImpacto(boolean habilitandoAgora) {
        EstadoSobreposicaoMorte.impactoInicioMs = System.currentTimeMillis();
        EstadoSobreposicaoMorte.habilitando = habilitandoAgora;
        EstadoSobreposicaoMorte.somTocado = false;
        EstadoSobreposicaoMorte.piscaAcesoAnterior = false;
        EstadoSobreposicaoMorte.glitchInicioMs = -1L;
    }

    private static void tocarSomSeNaHora(long decorrido) {
        if (EstadoSobreposicaoMorte.somTocado || decorrido < SOM_ATRASO_MS) return;
        EstadoSobreposicaoMorte.somTocado = true;

        float volume = 1.5f * (float) VolumeSomCliente.normalizado();
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(SOM_DOWNER), 1.0f, volume)
        );
    }

    private static boolean isActive() {
        return EstadoSobreposicaoMorte.impactoInicioMs >= 0 && (System.currentTimeMillis() - EstadoSobreposicaoMorte.impactoInicioMs) < TemporizacaoSobreposicaoMorte.TOTAL_MS;
    }

    @SubscribeEvent
    public void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (!isActive()) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (!isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        DesenhoCaveiraMorte.garantirBrilhoRegistrado(mc);

        GuiGraphics gfx = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        long decorrido = System.currentTimeMillis() - EstadoSobreposicaoMorte.impactoInicioMs;
        tocarSomSeNaHora(decorrido);

        if (EstadoSobreposicaoMorte.habilitando) {
            DesenhoVinhetaMorte.desenharVinheta(gfx, screenW, screenH, decorrido);
        }
        DesenhoCaveiraMorte.desenharCaveira(gfx, screenW / 2, screenH / 2, decorrido);
        DesenhoTituloMorte.desenharTitulo(gfx, mc, screenW / 2, screenH / 2, decorrido);
    }
}
