package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.aelysium.aelysiummod.AelysiumMod;

import static net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.PrimitivasDesenhoMorte.*;

@OnlyIn(Dist.CLIENT)
public class TelaEncerramentoMorteCanonica extends TelaBaseRitualMorte {

    private static final long FIM_SANGRAR = 3600;
    private static final long FIM_DESFAZER = 10600;
    private static final long FIM_APAGAR = 11300;
    private static final long FIM_TITULO = 15500;
    private static final long FIM_ESCURIDAO = 19300;
    private static final long FIM = 20800;

    private static final long DURACAO_FECHAMENTO = 6000;
    private static final long INTERVALO_FALAS = 3200;
    private static final long TRANSICAO_CAMPO = 450;

    private static final ResourceLocation ID_TELA_CONGELADA =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "morte_fim/tela_congelada");

    private final String falaSangrar;
    private final String falaDesfazer1;
    private final String falaDesfazer2;
    private final String falaFinal;

    private final DesenhoCampoSangueMorte campo;
    private final DesenhoOlhosMorte olhos;
    private final DesenhoRitualMorte ritual;
    private final DesenhoTituloRitualMorte titulo;

    private SomAmbienteRitualMorte vento;
    private SomAmbienteRitualMorte zumbido;

    private int olhosTocados;
    private long glitchAte;

    public TelaEncerramentoMorteCanonica() {
        super(Component.translatable("gui.aelysiummod.morte.fim_evento_mortes_canonicas"));
        this.falaSangrar = Component.translatable("gui.aelysiummod.morte.fala_encerramento_1").getString();
        this.falaDesfazer1 = Component.translatable("gui.aelysiummod.morte.fala_encerramento_2").getString();
        this.falaDesfazer2 = Component.translatable("gui.aelysiummod.morte.fala_encerramento_3").getString();
        this.falaFinal = Component.translatable("gui.aelysiummod.morte.fala_encerramento_final").getString();
        this.campo = new DesenhoCampoSangueMorte(random);
        this.olhos = new DesenhoOlhosMorte(random);
        this.ritual = new DesenhoRitualMorte(random);
        this.titulo = new DesenhoTituloRitualMorte(random, getTitle().getString(),
                Component.translatable("gui.aelysiummod.morte.subtitulo_encerramento").getString());
    }

    @Override
    protected long duracao() {
        return FIM;
    }

    @Override
    protected void aoIniciar() {
        vento = iniciarAmbiente(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP.value(), 0.5f);
    }

    @Override
    protected void desenharQuadro(GuiGraphics g, long t, long agora, float dt) {
        atualizarAmbiente(t);
        tocarSons(t);

        if (t < FIM_SANGRAR) {
            desenharSangramento(g, t, agora);
        } else if (t < FIM_DESFAZER) {
            desenharDesfazer(g, t, t - FIM_SANGRAR, agora);
        } else if (t < FIM_APAGAR) {
            desenharApagar(g, t - FIM_DESFAZER);
        } else if (t < FIM_TITULO) {
            desenharTitulo(g, t, t - FIM_APAGAR, agora);
        } else if (t < FIM_ESCURIDAO) {
            desenharEscuridao(g, t - FIM_TITULO);
        } else {
            desenharRetorno(g, t - FIM_ESCURIDAO);
        }
    }

    private void desenharTelaCongelada(GuiGraphics g, float alpha) {
        if (alpha < 0.01f) return;
        g.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        g.blit(ID_TELA_CONGELADA, 0, 0, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    private void desenharSangramento(GuiGraphics g, long t, long agora) {
        if (!capturado(ID_TELA_CONGELADA)) {
            capturar(ID_TELA_CONGELADA);
        }
        float p = progresso(t, 0, FIM_SANGRAR);

        g.fill(0, 0, width, height, 0xFF000000);
        desenharTelaCongelada(g, 1f);

        if (agora >= glitchAte && random.nextFloat() < 0.04f) {
            glitchAte = agora + 60 + random.nextInt(90);
        }
        if (agora < glitchAte) {
            DesenhoGlitchMorte.desenharFaixasTela(g, ID_TELA_CONGELADA, width, height, random);
        }

        g.fill(0, 0, width, height, argb(0.3f * p, 0x5A0008));
        campo.desenharBorda(g, width, height, t / 1000f, saidaCubica(p * 1.3f), 1f);
        campo.gotejarDoTopo(width, height, t, 0.06f + 0.1f * p);
        campo.desenharGotas(g, t, 1f);
        olhos.desenharOlhosRabiscados(g, width, height, t + 3000, 0.5f * p, width / 2f, height / 2f, true);
        g.fill(0, 0, width, height, argb(0.35f * p, 0x000000));

        desenharFala(g, falaSangrar, height * 0.5f, envelopeFala(t, 400), t);
        vinheta(g, width, height, p, COR_VINHETA);
    }

    private void desenharDesfazer(GuiGraphics g, long t, long td, long agora) {
        long duracaoDesfazer = FIM_DESFAZER - FIM_SANGRAR;
        float desfazer = progresso(td, 300, duracaoDesfazer - 500);
        g.fill(0, 0, width, height, 0xFF000000);
        campo.desenharCampo(g, width, height, t / 1000f, 0.8f - 0.6f * desfazer, 1f);
        desenharTelaCongelada(g, 1f - progresso(td, 0, TRANSICAO_CAMPO));
        campo.desenharBorda(g, width, height, t / 1000f, 1f - desfazer, 1f);
        campo.desenharGotas(g, t, 1f - desfazer);

        olhos.desenharOlhosFechando(g, width, height, td, DURACAO_FECHAMENTO, 1f, width / 2f, height / 2f);
        long tempoCirculo = (long) (DesenhoRitualMorte.DURACAO_DESENHO * (1f - desfazer));
        ritual.desenharCirculo(g, font, width, height, tempoCirculo, 0.4f * (1f - desfazer), 1f - progresso(td, duracaoDesfazer - 800, duracaoDesfazer));

        ritual.atualizarGlifos(t, width, height, 0.2f * (1f - desfazer));
        ritual.desenharGlifos(g, font, t, 0.7f);

        desenharFala(g, falaDesfazer1, height * 0.5f, envelopeFala(td, 300), t);
        desenharFala(g, falaDesfazer2, height * 0.5f, envelopeFala(td, 300 + INTERVALO_FALAS), t);

        if (agora >= glitchAte && random.nextFloat() < 0.025f) {
            glitchAte = agora + 60 + random.nextInt(90);
        }
        if (agora < glitchAte) {
            DesenhoGlitchMorte.desenharRuido(g, width, height, random, 0.5f);
        }

        linhasVarredura(g, width, height, 0.08f);
        vinheta(g, width, height, 1f, COR_VINHETA);
    }

    private void desenharApagar(GuiGraphics g, long ta) {
        if (ta < 400) {
            DesenhoGlitchMorte.desenharColagem(g, font, width, height, inicioMs + ta / 70);
            if ((ta / 70) % 3 == 1) {
                g.fill(0, 0, width, height, 0xFF000000);
                ritual.desenharCirculo(g, font, width, height, DesenhoRitualMorte.DURACAO_DESENHO, 1f, 1f);
            }
            linhasVarredura(g, width, height, 0.15f);
        } else {
            g.fill(0, 0, width, height, 0xFF000000);
            granulacao(g, width, height, random, 120, 0.5f);
        }
    }

    private void desenharTitulo(GuiGraphics g, long t, long tt, long agora) {
        g.fill(0, 0, width, height, 0xFF000000);
        campo.desenharCampo(g, width, height, t / 1000f, 0.08f, 1f);
        granulacao(g, width, height, random, 220, 0.5f);
        titulo.desenhar(g, font, width, height, tt, agora, FIM_TITULO - FIM_APAGAR);
        linhasVarredura(g, width, height, 0.1f);
        vinheta(g, width, height, 1f, COR_VINHETA);
    }

    private void desenharEscuridao(GuiGraphics g, long te) {
        g.fill(0, 0, width, height, 0xFF000000);
        granulacao(g, width, height, random, 160, 0.6f);

        desenharOlhoFinal(g, te, false);

        float alpha = envelopeFala(te, 700);
        if (random.nextFloat() < 0.03f) alpha *= 0.5f;
        DesenhoFrasesMorte.desenharFraseOndulada(g, font, falaFinal, width / 2f, height * 0.62f,
                escalaFrase(falaFinal, 2.5f, 0.8f), 0xE0141E, alpha, 2f, 1.2f, te);

        vinheta(g, width, height, 1f, COR_VINHETA);
    }

    private void desenharRetorno(GuiGraphics g, long tr) {
        float p = progresso(tr, 0, FIM - FIM_ESCURIDAO);
        g.fill(0, 0, width, height, argb(1f - suave(p), 0x000000));
        vinheta(g, width, height, 1f - p, 0x5A0008);
    }

    private void atualizarAmbiente(long t) {
        if (vento != null) {
            float volume;
            if (t < FIM_SANGRAR) volume = 0.3f + 0.4f * progresso(t, 0, FIM_SANGRAR);
            else if (t < FIM_DESFAZER) volume = 0.7f - 0.4f * progresso(t, FIM_SANGRAR, FIM_DESFAZER);
            else if (t < FIM_TITULO) volume = 0.25f;
            else volume = 0.25f * (1f - progresso(t, FIM_TITULO, FIM_ESCURIDAO));
            vento.definirVolume(volume);
        }

        if (t >= FIM_SANGRAR && t < FIM_DESFAZER) {
            if (zumbido == null) {
                zumbido = iniciarAmbiente(SoundEvents.BEACON_AMBIENT, 0.5f);
            }
            zumbido.definirVolume(0.8f * (1f - progresso(t, FIM_SANGRAR, FIM_DESFAZER)));
        } else if (t >= FIM_DESFAZER && zumbido != null) {
            pararAmbiente(zumbido);
            zumbido = null;
        }
    }

    private void tocarSons(long t) {
        if (t >= FIM_SANGRAR && t < FIM_DESFAZER) {
            int fechados = olhos.olhosFechados(t - FIM_SANGRAR, DURACAO_FECHAMENTO);
            while (olhosTocados < fechados) {
                tocar(SoundEvents.SCULK_CLICKING_STOP, 0.9f - 0.025f * olhosTocados, 0.35f);
                olhosTocados++;
            }
        }

        tocarUmaVez("coracao1", t >= 200, () -> tocar(SoundEvents.WARDEN_HEARTBEAT, 0.7f, 0.8f));
        tocarUmaVez("sussurro", t >= 400, () -> tocar(SoundEvents.AMBIENT_CAVE.value(), 0.7f, 1f));
        tocarUmaVez("coracao2", t >= 2200, () -> tocar(SoundEvents.WARDEN_HEARTBEAT, 0.65f, 0.8f));
        tocarUmaVez("sino1", t >= FIM_SANGRAR, () -> tocar(SoundEvents.BELL_BLOCK, 0.7f, 1f));
        tocarUmaVez("glifos", t >= FIM_SANGRAR + 300, () -> tocar(SoundEvents.ENCHANTMENT_TABLE_USE, 0.5f, 1f));
        tocarUmaVez("sino2", t >= FIM_SANGRAR + 300 + INTERVALO_FALAS, () -> tocar(SoundEvents.BELL_BLOCK, 0.6f, 0.9f));
        tocarUmaVez("coracao3", t >= FIM_SANGRAR + 4800, () -> tocar(SoundEvents.WARDEN_HEARTBEAT, 0.6f, 0.7f));
        tocarUmaVez("sino3", t >= FIM_SANGRAR + 6200, () -> tocar(SoundEvents.BELL_BLOCK, 0.5f, 0.8f));
        tocarUmaVez("apagar", t >= FIM_DESFAZER, () -> {
            tocar(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 0.6f, 1f);
            tocar(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.5f, 0.6f);
        });
        tocarUmaVez("titulo", t >= FIM_APAGAR, () -> {
            tocar(SoundEvents.BELL_BLOCK, 0.5f, 0.8f);
            tocar(SoundEvents.BELL_RESONATE, 0.5f, 0.6f);
        });
        tocarUmaVez("coracao4", t >= FIM_APAGAR + 1500, () -> tocar(SoundEvents.WARDEN_HEARTBEAT, 0.55f, 0.6f));
        tocarUmaVez("olhosEscuro", t >= FIM_TITULO + 100, () -> tocar(SoundEvents.SOUL_ESCAPE.value(), 0.5f, 1f));
        tocarUmaVez("retorno", t >= FIM_ESCURIDAO, () -> tocar(SoundEvents.BEACON_DEACTIVATE, 0.5f, 0.6f));
    }
}
