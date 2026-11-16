package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.aelysium.aelysiummod.AelysiumMod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.PrimitivasDesenhoMorte.*;

@OnlyIn(Dist.CLIENT)
public class TelaAberturaMorteCanonica extends TelaBaseRitualMorte {

    private static final long FIM_RACHADURA = 3200;
    private static final long FIM_QUEDA = 5000;
    private static final long FIM_OLHOS = 11000;
    private static final long FIM_RITUAL = 18500;
    private static final long FIM_COLAPSO = 19300;
    private static final long FIM_TITULO = 23500;
    private static final long FIM_ESCURIDAO = 27300;
    private static final long FIM = 28800;

    private static final long INICIO_ENCARAR = 3000;
    private static final long INTERVALO_FALAS_RITUAL = 3000;
    private static final long INICIO_REVELACAO = 5800;
    private static final long ABERTURA_OLHO_FINAL = 200;

    private static final long PRIMEIRO_GOLPE = 200;
    private static final long INTERVALO_GOLPE = 290;
    private static final int TOTAL_GOLPES = 10;

    private static final int COLUNAS_CACOS = 9;
    private static final int LINHAS_CACOS = 6;
    private static final float GRAVIDADE = 1100f;

    private static final ResourceLocation ID_TELA_CONGELADA =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "morte_intro/tela_congelada");
    private static final ResourceLocation ID_TELA_RACHADA =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "morte_intro/tela_rachada");
    private static final ResourceLocation[] ESTAGIOS_QUEBRA = new ResourceLocation[10];

    static {
        for (int i = 0; i < ESTAGIOS_QUEBRA.length; i++) {
            ESTAGIOS_QUEBRA[i] = ResourceLocation.withDefaultNamespace("textures/block/destroy_stage_" + i + ".png");
        }
    }

    private record Caco(float[] xs, float[] ys, float centroX, float centroY, long atraso,
                        float velocidadeX, float velocidadeY, float rotacao) {
    }

    private final List<Caco> cacos = new ArrayList<>();
    private final long[] batidas;

    private final String falaOlhos1;
    private final String falaOlhos2;
    private final String[] falasRitual;
    private final String falaColapso;
    private final String falaFinal;

    private final DesenhoCampoSangueMorte campo;
    private final DesenhoOlhosMorte olhos;
    private final DesenhoRitualMorte ritual;
    private final DesenhoTituloRitualMorte titulo;

    private SomAmbienteRitualMorte vento;
    private SomAmbienteRitualMorte zumbido;

    private float larguraCacos;
    private float alturaCacos;
    private int golpesDados;
    private int proximaBatida;
    private int olhosTocados;
    private float tremor;
    private long glitchAte;
    private long subliminarAte;

    public TelaAberturaMorteCanonica() {
        super(Component.translatable("gui.aelysiummod.morte.evento_mortes_canonicas"));
        this.falaOlhos1 = Component.translatable("gui.aelysiummod.morte.fala_olhos_1").getString();
        this.falaOlhos2 = Component.translatable("gui.aelysiummod.morte.fala_olhos_2").getString();
        this.falasRitual = new String[]{
                Component.translatable("gui.aelysiummod.morte.fala_ritual_1").getString(),
                Component.translatable("gui.aelysiummod.morte.fala_ritual_2").getString()
        };
        this.falaColapso = Component.translatable("gui.aelysiummod.morte.fala_colapso").getString();
        this.falaFinal = Component.translatable("gui.aelysiummod.morte.fala_final").getString();
        this.campo = new DesenhoCampoSangueMorte(random);
        this.olhos = new DesenhoOlhosMorte(random);
        this.ritual = new DesenhoRitualMorte(random);
        this.titulo = new DesenhoTituloRitualMorte(random, getTitle().getString(),
                Component.translatable("gui.aelysiummod.morte.subtitulo_abertura").getString());
        this.batidas = gerarBatidas();
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

        if (t < FIM_RACHADURA) {
            desenharRachadura(g, t, agora, dt);
        } else if (t < FIM_QUEDA) {
            desenharQueda(g, t, t - FIM_RACHADURA);
        } else if (t < FIM_OLHOS) {
            desenharOlhos(g, t, t - FIM_QUEDA, agora);
        } else if (t < FIM_RITUAL) {
            desenharRitual(g, t, t - FIM_OLHOS, agora);
        } else if (t < FIM_COLAPSO) {
            desenharColapso(g, t, t - FIM_RITUAL);
        } else if (t < FIM_TITULO) {
            desenharTitulo(g, t, t - FIM_COLAPSO, agora);
        } else if (t < FIM_ESCURIDAO) {
            desenharEscuridao(g, t - FIM_TITULO);
        } else {
            desenharRetorno(g, t - FIM_ESCURIDAO);
        }
    }

    private void desenharRachadura(GuiGraphics g, long t, long agora, float dt) {
        if (!capturado(ID_TELA_CONGELADA)) {
            capturar(ID_TELA_CONGELADA);
        }

        int golpesEsperados = t < PRIMEIRO_GOLPE ? 0
                : Math.min(TOTAL_GOLPES, 1 + (int) ((t - PRIMEIRO_GOLPE) / INTERVALO_GOLPE));
        while (golpesDados < golpesEsperados) {
            golpear(t, agora);
        }

        if (agora < subliminarAte) {
            desenharPentagramaSubliminar(g);
            return;
        }

        tremor = Math.max(0, tremor - dt * 20f);
        float dx = (random.nextFloat() - 0.5f) * 2f * tremor;
        float dy = (random.nextFloat() - 0.5f) * 2f * tremor;
        desenharTelaRachada(g, t, dx, dy, golpesDados - 1, agora < glitchAte, progresso(t, 0, FIM_RACHADURA));
    }

    private void golpear(long t, long agora) {
        golpesDados++;
        tremor = 3f + golpesDados * 0.7f;
        glitchAte = agora + 90 + random.nextInt(90);
        if (golpesDados == 4 || golpesDados == 8) {
            subliminarAte = agora + 70;
        }
        if (golpesDados >= 5) {
            for (int i = 0; i < 2 + random.nextInt(2); i++) {
                campo.adicionarGota(random.nextFloat() * width, height * 0.015f, t, 1.5f + random.nextFloat() * 2f,
                        height * (0.15f + random.nextFloat() * 0.45f), 30f + random.nextFloat() * 40f);
            }
        }

        float pitch = Math.max(0.5f, 1f - golpesDados * 0.05f);
        tocar(golpesDados % 3 == 0 ? SoundEvents.DEEPSLATE_HIT : SoundEvents.STONE_HIT, pitch, 1f);
        if (golpesDados >= 5) {
            tocar(SoundEvents.SCULK_BLOCK_BREAK, 0.6f, 0.6f);
        }
        if (golpesDados >= 7) {
            tocar(SoundEvents.POLISHED_DEEPSLATE_BREAK, pitch, 0.9f);
        }
    }

    private void desenharTelaRachada(GuiGraphics g, long t, float dx, float dy, int estagio, boolean glitch, float progresso) {
        g.fill(0, 0, width, height, 0xFF000000);
        g.flush();

        g.pose().pushPose();
        g.pose().translate(dx, dy, 0);
        g.blit(ID_TELA_CONGELADA, 0, 0, 0, 0, width, height, width, height);
        if (estagio >= 0) {
            desenharRachaduraTelaCheia(g, estagio);
        }
        if (glitch) {
            DesenhoGlitchMorte.desenharFaixasTela(g, ID_TELA_CONGELADA, width, height, random);
        }
        g.pose().popPose();

        g.fill(0, 0, width, height, argb(0.22f * progresso, 0x5A0008));
        campo.desenharCampo(g, width, height, t / 1000f, 1f, 0.2f * progresso * progresso);
        campo.desenharBorda(g, width, height, t / 1000f, progresso(t, 1300, FIM_RACHADURA), 1f);
        campo.desenharGotas(g, t, 1f);
        g.fill(0, 0, width, height, argb(0.3f * progresso, 0x000000));
        vinheta(g, width, height, progresso, COR_VINHETA);
    }

    private void desenharRachaduraTelaCheia(GuiGraphics g, int estagio) {
        ResourceLocation textura = ESTAGIOS_QUEBRA[Math.min(ESTAGIOS_QUEBRA.length - 1, estagio)];
        int realce = Math.max(1, width / 240);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.22f, 0f, 0.01f, 0.95f);
        g.blit(textura, 0, 0, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(0.9f, 0.05f, 0.08f, 0.35f);
        g.blit(textura, realce, realce, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    private void desenharQueda(GuiGraphics g, long t, long tq) {
        if (!capturado(ID_TELA_RACHADA)) {
            desenharTelaRachada(g, t, 0, 0, TOTAL_GOLPES - 1, false, 1f);
            g.flush();
            capturar(ID_TELA_RACHADA);
            criarCacos();
            campo.limparGotas();
        }

        g.fill(0, 0, width, height, 0xFF000000);
        campo.desenharCampo(g, width, height, t / 1000f, 1f, 1f);
        g.flush();
        desenharCacos(g, tq);

        if (tq < 160) {
            g.fill(0, 0, width, height, argb(0.9f * (1f - tq / 160f), 0xFF1020));
        }
        vinheta(g, width, height, 1f, COR_VINHETA);
    }

    private void criarCacos() {
        cacos.clear();
        larguraCacos = width;
        alturaCacos = height;

        float larguraCelula = larguraCacos / COLUNAS_CACOS;
        float alturaCelula = alturaCacos / LINHAS_CACOS;
        float[][] vx = new float[COLUNAS_CACOS + 1][LINHAS_CACOS + 1];
        float[][] vy = new float[COLUNAS_CACOS + 1][LINHAS_CACOS + 1];
        for (int i = 0; i <= COLUNAS_CACOS; i++) {
            for (int j = 0; j <= LINHAS_CACOS; j++) {
                float x = i * larguraCelula;
                float y = j * alturaCelula;
                if (i > 0 && i < COLUNAS_CACOS && j > 0 && j < LINHAS_CACOS) {
                    x += (random.nextFloat() - 0.5f) * larguraCelula * 0.7f;
                    y += (random.nextFloat() - 0.5f) * alturaCelula * 0.7f;
                }
                vx[i][j] = x;
                vy[i][j] = y;
            }
        }

        float impactoX = larguraCacos / 2f + (random.nextFloat() - 0.5f) * larguraCacos * 0.2f;
        float impactoY = alturaCacos / 2f + (random.nextFloat() - 0.5f) * alturaCacos * 0.2f;
        float distanciaMax = (float) Math.hypot(Math.max(impactoX, larguraCacos - impactoX),
                Math.max(impactoY, alturaCacos - impactoY));

        for (int i = 0; i < COLUNAS_CACOS; i++) {
            for (int j = 0; j < LINHAS_CACOS; j++) {
                float ax = vx[i][j], ay = vy[i][j];
                float bx = vx[i + 1][j], by = vy[i + 1][j];
                float cx = vx[i + 1][j + 1], cy = vy[i + 1][j + 1];
                float dx = vx[i][j + 1], dy = vy[i][j + 1];
                if (random.nextBoolean()) {
                    adicionarCaco(new float[]{ax, bx, cx}, new float[]{ay, by, cy}, impactoX, impactoY, distanciaMax);
                    adicionarCaco(new float[]{ax, cx, dx}, new float[]{ay, cy, dy}, impactoX, impactoY, distanciaMax);
                } else {
                    adicionarCaco(new float[]{ax, bx, dx}, new float[]{ay, by, dy}, impactoX, impactoY, distanciaMax);
                    adicionarCaco(new float[]{bx, cx, dx}, new float[]{by, cy, dy}, impactoX, impactoY, distanciaMax);
                }
            }
        }
    }

    private void adicionarCaco(float[] xs, float[] ys, float impactoX, float impactoY, float distanciaMax) {
        float centroX = (xs[0] + xs[1] + xs[2]) / 3f;
        float centroY = (ys[0] + ys[1] + ys[2]) / 3f;
        float distancia = (float) Math.hypot(centroX - impactoX, centroY - impactoY);

        long atraso = (long) (distancia / distanciaMax * 450f) + random.nextInt(120);
        float direcaoX = distancia < 1f ? 0f : (centroX - impactoX) / distancia;
        float velocidadeX = direcaoX * (40f + random.nextFloat() * 140f) + (random.nextFloat() - 0.5f) * 30f;
        float velocidadeY = -(60f + random.nextFloat() * 160f);
        float rotacao = (random.nextFloat() - 0.5f) * 5f;

        cacos.add(new Caco(xs, ys, centroX, centroY, atraso, velocidadeX, velocidadeY, rotacao));
    }

    private void desenharCacos(GuiGraphics g, long tq) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, ID_TELA_RACHADA);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        Matrix4f matriz = g.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (Caco caco : cacos) {
            float s = Math.max(0f, (tq - caco.atraso()) / 1000f);
            float deslocX = caco.velocidadeX() * s;
            float deslocY = caco.velocidadeY() * s + 0.5f * GRAVIDADE * s * s;
            float angulo = caco.rotacao() * s;
            float cos = (float) Math.cos(angulo);
            float sin = (float) Math.sin(angulo);
            float queda = clamp01(s / 1.1f);
            float vermelho = 1f - 0.5f * queda;
            float outros = 1f - 0.85f * queda;

            for (int k = 0; k < 3; k++) {
                float rx = caco.xs()[k] - caco.centroX();
                float ry = caco.ys()[k] - caco.centroY();
                float px = caco.centroX() + rx * cos - ry * sin + deslocX;
                float py = caco.centroY() + rx * sin + ry * cos + deslocY;
                buffer.addVertex(matriz, px, py, 0)
                        .setUv(caco.xs()[k] / larguraCacos, caco.ys()[k] / alturaCacos)
                        .setColor(vermelho, outros, outros, 1f);
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void desenharOlhos(GuiGraphics g, long t, long to, long agora) {
        g.fill(0, 0, width, height, 0xFF000000);
        campo.desenharCampo(g, width, height, t / 1000f, 1f - 0.65f * suave(to / 700f), 1f);
        campo.desenharBorda(g, width, height, t / 1000f, 1f, 1f);
        campo.gotejarDoTopo(width, height, t, 0.04f);
        campo.desenharGotas(g, t, 1f);

        ritual.atualizarGlifos(t, width, height, 0.05f);
        ritual.desenharGlifos(g, font, t, 0.6f);

        olhos.desenharOlhosRabiscados(g, width, height, to, 1f, width / 2f, height / 2f, to > INICIO_ENCARAR);

        desenharFala(g, falaOlhos1, height * 0.5f, envelopeFala(to, 100), t);
        desenharFala(g, falaOlhos2, height * 0.5f, envelopeFala(to, INICIO_ENCARAR), t);

        if (agora >= glitchAte && random.nextFloat() < 0.03f) {
            glitchAte = agora + 60 + random.nextInt(100);
        }
        if (agora < glitchAte) {
            DesenhoGlitchMorte.desenharRuido(g, width, height, random, 0.6f);
        }

        linhasVarredura(g, width, height, 0.08f);
        vinheta(g, width, height, 1f, COR_VINHETA);
    }

    private void desenharRitual(GuiGraphics g, long t, long tr, long agora) {
        long duracaoRitual = FIM_RITUAL - FIM_OLHOS;
        float revelacao = progresso(tr, INICIO_REVELACAO, INICIO_REVELACAO + 500);
        float tremorFinal = 2.5f * progresso(tr, INICIO_REVELACAO + 1100, duracaoRitual);

        g.fill(0, 0, width, height, 0xFF000000);
        g.pose().pushPose();
        g.pose().translate((random.nextFloat() - 0.5f) * tremorFinal, (random.nextFloat() - 0.5f) * tremorFinal, 0);

        campo.desenharCampo(g, width, height, t / 1000f, 0.3f + 0.7f * revelacao, 1f);
        float alphaOlhos = Math.max(1f - progresso(tr, 0, 1200), progresso(tr, INICIO_REVELACAO + 700, INICIO_REVELACAO + 1300));
        olhos.desenharOlhosRabiscados(g, width, height, t - FIM_QUEDA, alphaOlhos, width / 2f, height / 2f, true);
        ritual.desenharCirculo(g, font, width, height, tr, pulso(t), 1f);

        ritual.atualizarGlifos(t, width, height, 0.08f + 0.25f * progresso(tr, 0, duracaoRitual));
        ritual.desenharGlifos(g, font, t, 0.8f);

        for (int i = 0; i < falasRitual.length; i++) {
            desenharFala(g, falasRitual[i], height * 0.5f, envelopeFala(tr, i * INTERVALO_FALAS_RITUAL + 100), t);
        }
        g.pose().popPose();

        if (agora >= glitchAte && random.nextFloat() < 0.02f + 0.06f * progresso(tr, 0, duracaoRitual)) {
            glitchAte = agora + 60 + random.nextInt(100);
        }
        if (agora < glitchAte) {
            DesenhoGlitchMorte.desenharRuido(g, width, height, random, 0.7f);
        }

        linhasVarredura(g, width, height, 0.08f);
        vinheta(g, width, height, 1f, COR_VINHETA);
    }

    private void desenharColapso(GuiGraphics g, long t, long tc) {
        long passo = tc / 60;
        g.pose().pushPose();
        g.pose().translate((random.nextFloat() - 0.5f) * 8f, (random.nextFloat() - 0.5f) * 8f, 0);
        DesenhoGlitchMorte.desenharColagem(g, font, width, height, inicioMs + passo);
        DesenhoFrasesMorte.desenharSpam(g, font, falaColapso, width, height, inicioMs * 31 + passo,
                12 + (int) (tc / 25), 0.9f);
        if (passo % 4 == 1) {
            desenharPentagramaSubliminar(g);
        }
        ritual.atualizarGlifos(t, width, height, 0.6f);
        ritual.desenharGlifos(g, font, t, 1f);
        g.pose().popPose();
        linhasVarredura(g, width, height, 0.15f);
    }

    private void desenharTitulo(GuiGraphics g, long t, long tt, long agora) {
        g.fill(0, 0, width, height, 0xFF000000);
        campo.desenharCampo(g, width, height, t / 1000f, 0.12f, 1f);
        olhos.desenharOlhosRabiscados(g, width, height, t - FIM_QUEDA, 0.22f, width / 2f, height / 2f, false);
        granulacao(g, width, height, random, 220, 0.5f);
        titulo.desenhar(g, font, width, height, tt, agora, FIM_TITULO - FIM_COLAPSO);
        if (tt < 200) {
            g.fill(0, 0, width, height, argb(0.85f * (1f - tt / 200f), 0xFF1020));
        }
        linhasVarredura(g, width, height, 0.1f);
        vinheta(g, width, height, 1f, COR_VINHETA);
    }

    private void desenharEscuridao(GuiGraphics g, long te) {
        g.fill(0, 0, width, height, 0xFF000000);
        granulacao(g, width, height, random, 160, 0.6f);

        desenharOlhoFinal(g, te, true);

        float alpha = envelopeFala(te, 700);
        if (random.nextFloat() < 0.03f) alpha *= 0.5f;
        DesenhoFrasesMorte.desenharFraseOndulada(g, font, falaFinal, width / 2f, height * 0.62f,
                escalaFrase(falaFinal, 3f, 0.86f), 0xE0141E, alpha, 2f, 1.5f, te);

        vinheta(g, width, height, 1f, COR_VINHETA);
    }

    private void desenharPentagramaSubliminar(GuiGraphics g) {
        g.fill(0, 0, width, height, 0xFF000000);
        ritual.desenharCirculo(g, font, width, height, DesenhoRitualMorte.DURACAO_DESENHO, 1f, 1f);
    }

    private void desenharRetorno(GuiGraphics g, long tr) {
        float p = progresso(tr, 0, FIM - FIM_ESCURIDAO);
        g.fill(0, 0, width, height, argb(1f - suave(p), 0x000000));
        vinheta(g, width, height, 1f - p, 0x5A0008);
    }

    private long[] gerarBatidas() {
        List<Long> lista = new ArrayList<>();
        float intervalo = 1150f;
        for (float t = 600; t < FIM_RITUAL; t += intervalo) {
            lista.add((long) t);
            intervalo = Math.max(380f, intervalo * 0.965f);
        }
        lista.add(FIM_COLAPSO + 1600);
        lista.add(FIM_COLAPSO + 2500);
        long[] resultado = new long[lista.size()];
        for (int i = 0; i < resultado.length; i++) {
            resultado[i] = lista.get(i);
        }
        return resultado;
    }

    private float pulso(long t) {
        long ultima = -1;
        for (long batida : batidas) {
            if (batida > t) break;
            ultima = batida;
        }
        return ultima < 0 ? 0f : (float) Math.exp(-(t - ultima) / 160f);
    }

    private void atualizarAmbiente(long t) {
        if (vento != null) {
            float volume;
            if (t < FIM_RACHADURA) volume = 0.2f + 0.4f * progresso(t, 0, FIM_RACHADURA);
            else if (t < FIM_RITUAL) volume = 0.8f;
            else if (t < FIM_COLAPSO) volume = 1f;
            else if (t < FIM_TITULO) volume = 0.3f;
            else if (t < FIM_ESCURIDAO) volume = 0.3f * (1f - progresso(t, FIM_TITULO, FIM_ESCURIDAO));
            else volume = 0f;
            vento.definirVolume(volume);
        }

        if (t >= FIM_OLHOS && t < FIM_COLAPSO) {
            if (zumbido == null) {
                zumbido = iniciarAmbiente(SoundEvents.BEACON_AMBIENT, 0.5f);
            }
            zumbido.definirVolume(progresso(t, FIM_OLHOS, FIM_RITUAL));
        } else if (t >= FIM_COLAPSO && zumbido != null) {
            pararAmbiente(zumbido);
            zumbido = null;
        }
    }

    private void tocarSons(long t) {
        while (proximaBatida < batidas.length && t >= batidas[proximaBatida]) {
            float fracao = clamp01((float) batidas[proximaBatida] / FIM_RITUAL);
            tocar(SoundEvents.WARDEN_HEARTBEAT, 0.8f + 0.3f * fracao, 0.6f + 0.4f * fracao);
            proximaBatida++;
        }

        if (t >= FIM_QUEDA && t < FIM_OLHOS) {
            int abertos = olhos.olhosAbertos(t - FIM_QUEDA);
            while (olhosTocados < abertos) {
                tocar(SoundEvents.SCULK_CLICKING, 0.5f + 0.04f * olhosTocados, 0.35f);
                olhosTocados++;
            }
        }

        tocarUmaVez("estilhaco", t >= FIM_RACHADURA, () -> {
            tocar(SoundEvents.GLASS_BREAK, 0.5f, 1f);
            tocar(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 0.55f, 0.8f);
            tocar(SoundEvents.SLIME_BLOCK_BREAK, 0.5f, 1f);
            tocar(SoundEvents.WITHER_BREAK_BLOCK, 0.5f, 0.45f);
        });
        tocarUmaVez("sussurro", t >= FIM_QUEDA + 100, () -> tocar(SoundEvents.AMBIENT_CAVE.value(), 0.8f, 1f));
        tocarUmaVez("encarar", t >= FIM_QUEDA + INICIO_ENCARAR, () -> tocar(SoundEvents.ENDERMAN_STARE, 0.6f, 0.55f));
        tocarUmaVez("sino1", t >= FIM_OLHOS, () -> tocar(SoundEvents.BELL_BLOCK, 0.5f, 1f));
        tocarUmaVez("portal", t >= FIM_OLHOS + 200, () -> tocar(SoundEvents.PORTAL_TRIGGER, 0.6f, 0.5f));
        tocarUmaVez("glifos1", t >= FIM_OLHOS + 700, () -> tocar(SoundEvents.ENCHANTMENT_TABLE_USE, 0.5f, 1f));
        tocarUmaVez("sino2", t >= FIM_OLHOS + INTERVALO_FALAS_RITUAL, () -> tocar(SoundEvents.BELL_BLOCK, 0.5f, 0.9f));
        tocarUmaVez("glifos2", t >= FIM_OLHOS + INTERVALO_FALAS_RITUAL + 700, () -> tocar(SoundEvents.ENCHANTMENT_TABLE_USE, 0.6f, 1f));
        tocarUmaVez("sino3", t >= FIM_OLHOS + INICIO_REVELACAO, () -> {
            tocar(SoundEvents.BELL_BLOCK, 0.5f, 1f);
            tocar(SoundEvents.ELDER_GUARDIAN_CURSE, 0.6f, 0.9f);
        });
        tocarUmaVez("portal2", t >= FIM_OLHOS + INICIO_REVELACAO + 200, () -> tocar(SoundEvents.PORTAL_TRIGGER, 0.5f, 0.6f));
        tocarUmaVez("colapso", t >= FIM_RITUAL, () -> {
            tocar(SoundEvents.SCULK_SHRIEKER_SHRIEK, 0.6f, 0.7f);
            tocar(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.5f, 0.6f);
        });
        tocarUmaVez("estatica", t >= FIM_RITUAL + 400, () -> tocar(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7f, 0.6f));
        tocarUmaVez("estrondo", t >= FIM_COLAPSO, () -> tocar(SoundEvents.WARDEN_SONIC_BOOM, 0.6f, 1f));
        tocarUmaVez("sinoFinal", t >= FIM_COLAPSO + 500, () -> {
            tocar(SoundEvents.BELL_BLOCK, 0.5f, 0.8f);
            tocar(SoundEvents.BELL_RESONATE, 0.5f, 0.6f);
        });
        tocarUmaVez("olhoFinal", t >= FIM_TITULO + ABERTURA_OLHO_FINAL, () -> tocar(SoundEvents.SOUL_ESCAPE.value(), 0.5f, 1f));
        tocarUmaVez("falaFinal", t >= FIM_TITULO + 700, () -> tocar(SoundEvents.WARDEN_HEARTBEAT, 0.6f, 1f));
        tocarUmaVez("retorno", t >= FIM_ESCURIDAO, () -> tocar(SoundEvents.BEACON_DEACTIVATE, 0.5f, 0.6f));
    }
}
