package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.aelysium.aelysiummod.nucleo.cliente.VolumeSomCliente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public abstract class TelaBaseRitualMorte extends Screen {

    protected static final int COR_FALA = 0xD01822;
    protected static final int COR_VINHETA = 0x0A0000;
    protected static final long DURACAO_FALA = 2900;
    private static final long FALA_ENTRADA = 350;
    private static final long FALA_SAIDA = 400;

    protected final Screen anterior;
    protected final Random random = new Random();

    private final Set<String> sonsTocados = new HashSet<>();
    private final Map<ResourceLocation, DynamicTexture> texturas = new HashMap<>();
    private final List<SomAmbienteRitualMorte> ambientes = new ArrayList<>();

    protected long inicioMs = -1;
    private long ultimoQuadroMs;

    protected TelaBaseRitualMorte(Component titulo) {
        super(titulo);
        this.anterior = Minecraft.getInstance().screen;
    }

    protected abstract long duracao();

    protected abstract void aoIniciar();

    protected abstract void desenharQuadro(GuiGraphics g, long t, long agora, float dt);

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void onClose() {
        minecraft.setScreen(anterior instanceof DeathScreen ? anterior : null);
    }

    @Override
    public void removed() {
        for (SomAmbienteRitualMorte ambiente : ambientes) {
            ambiente.parar();
            minecraft.getSoundManager().stop(ambiente);
        }
        ambientes.clear();
        for (Map.Entry<ResourceLocation, DynamicTexture> entrada : texturas.entrySet()) {
            entrada.getValue().setPixels(null);
            minecraft.getTextureManager().release(entrada.getKey());
        }
        texturas.clear();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long agora = Util.getMillis();
        if (inicioMs < 0) {
            inicioMs = agora;
            ultimoQuadroMs = agora;
            aoIniciar();
        }
        long t = agora - inicioMs;
        float dt = Math.min(0.1f, (agora - ultimoQuadroMs) / 1000f);
        ultimoQuadroMs = agora;
        if (t >= duracao()) {
            onClose();
            return;
        }
        desenharQuadro(g, t, agora, dt);
    }

    protected void tocar(SoundEvent som, float pitch, float volume) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(som, pitch, volume * (float) VolumeSomCliente.normalizado()));
    }

    protected void tocarUmaVez(String id, boolean pronto, Runnable acao) {
        if (pronto && sonsTocados.add(id)) acao.run();
    }

    protected SomAmbienteRitualMorte iniciarAmbiente(SoundEvent som, float pitch) {
        SomAmbienteRitualMorte ambiente = new SomAmbienteRitualMorte(som, pitch);
        ambientes.add(ambiente);
        minecraft.getSoundManager().play(ambiente);
        return ambiente;
    }

    protected void pararAmbiente(SomAmbienteRitualMorte ambiente) {
        if (ambiente == null) return;
        ambiente.parar();
        minecraft.getSoundManager().stop(ambiente);
        ambientes.remove(ambiente);
    }

    protected boolean capturado(ResourceLocation id) {
        return texturas.containsKey(id);
    }

    protected void capturar(ResourceLocation id) {
        NativeImage imagem = Screenshot.takeScreenshot(minecraft.getMainRenderTarget());
        DynamicTexture textura = new DynamicTexture(imagem);
        minecraft.getTextureManager().register(id, textura);
        texturas.put(id, textura);
    }

    protected float escalaFrase(String frase, float maxima, float ocupacao) {
        return Math.max(1f, Math.min(maxima, width * ocupacao / Math.max(1, font.width(frase))));
    }

    protected static float envelopeFala(long t, long inicio) {
        return PrimitivasDesenhoMorte.envelope(t, inicio, inicio + FALA_ENTRADA, inicio + DURACAO_FALA - FALA_SAIDA, inicio + DURACAO_FALA);
    }

    protected void desenharOlhoFinal(GuiGraphics g, long te, boolean encarando) {
        float abertura = PrimitivasDesenhoMorte.envelope(te, 200, 700, 3000, 3500);
        if (abertura <= 0.02f) return;
        float cx = width / 2f;
        float cy = height * 0.38f;
        float focoX = encarando ? cx : cx + (float) Math.sin(te * 0.0018f) * width;
        DesenhoOlhosMorte.desenharOlhoRabiscado(g, cx, cy, Math.min(width, height) * 0.16f, 0f,
                abertura, focoX, cy, te / 83, 1f, encarando);
    }

    protected void desenharFala(GuiGraphics g, String fala, float y, float alpha, long t) {
        if (random.nextFloat() < 0.03f) alpha *= 0.5f;
        float escala = escalaFrase(fala, 2.4f, 0.8f);
        DesenhoFrasesMorte.desenharFraseOndulada(g, font, fala, width / 2f, y, escala, COR_FALA, alpha,
                escala * 2.5f, 0.7f, t);
    }
}
