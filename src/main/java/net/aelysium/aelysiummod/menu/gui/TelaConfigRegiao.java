package net.aelysium.aelysiummod.menu.gui;

import net.aelysium.aelysiummod.protecao.network.PacoteCriarRegiao;
import net.aelysium.aelysiummod.protecao.network.PacoteEditarRegiao;
import net.aelysium.aelysiummod.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.protecao.regiao.Regiao;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class TelaConfigRegiao extends Screen {

    private static final int COR_FUNDO = 0xF0232323;
    private static final int COR_BORDA = 0xFF555555;
    private static final int COR_TITULO = 0xFFFFDD55;
    private static final int COR_HINT = 0xFF888888;
    private static final int RAIO_CANTO = 6;
    private static final int LARGURA_TELA = 340;
    private static final int PADDING = 20;
    private static final int PADDING_TOPO = 35;
    private static final int ESPACAMENTO_Y = 26;

    private static final ResourceLocation TEXTURA_TERRA =
            ResourceLocation.withDefaultNamespace("textures/block/cobblestone.png");

    private final BlockPos posicaoMinima;
    private final BlockPos posicaoMaxima;
    private final String nomeRegiaoEditando;

    private EditBox campoNome;
    private EditBox campoDonos;
    private EditBox campoMensagemEntrada;
    private EditBox campoMensagemSaida;

    private String valorNomePreservado = "";
    private String valorDonosPreservado = "";
    private String valorEntradaPreservado = "";
    private String valorSaidaPreservado = "";

    private final Map<FlagRegiao, Boolean> flags;

    private List<String> sugestoesDonos = new ArrayList<>();
    private int sugestaoSelecionada = -1;
    private static final int MAX_SUGESTOES = 5;

    private int posX, posY, alturaTelaCalculada;

    public TelaConfigRegiao(BlockPos posicaoMinima, BlockPos posicaoMaxima) {
        super(Component.literal("Criar Região Protegida"));
        this.posicaoMinima = posicaoMinima;
        this.posicaoMaxima = posicaoMaxima;
        this.nomeRegiaoEditando = null;
        this.flags = new HashMap<>();
        for (FlagRegiao flag : FlagRegiao.values()) flags.put(flag, false);
    }

    public TelaConfigRegiao(Regiao regiao) {
        super(Component.literal("Editar Região: " + regiao.getNome()));
        this.posicaoMinima = regiao.getPosicaoMinima();
        this.posicaoMaxima = regiao.getPosicaoMaxima();
        this.nomeRegiaoEditando = regiao.getNome();
        this.flags = new HashMap<>();

        this.valorNomePreservado = regiao.getNome();
        this.valorEntradaPreservado = regiao.getMensagemEntrada();
        this.valorSaidaPreservado = regiao.getMensagemSaida();

        if (Minecraft.getInstance().getConnection() != null) {
            StringBuilder sb = new StringBuilder();
            for (UUID uuid : regiao.getDonos()) {
                var info = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
                String nome = info != null ? info.getProfile().getName() : uuid.toString();
                if (sb.length() > 0) sb.append(", ");
                sb.append(nome);
            }
            this.valorDonosPreservado = sb.toString();
        }

        for (FlagRegiao flag : FlagRegiao.values()) flags.put(flag, regiao.getFlagValor(flag));
    }

    public TelaConfigRegiao(BlockPos posicaoMinima, BlockPos posicaoMaxima,
                            String nomeRegiaoEditando, Map<FlagRegiao, Boolean> flags,
                            String nome, String donos, String entrada, String saida) {
        super(Component.literal(nomeRegiaoEditando != null
                ? "Editar Região: " + nomeRegiaoEditando : "Criar Região Protegida"));
        this.posicaoMinima = posicaoMinima;
        this.posicaoMaxima = posicaoMaxima;
        this.nomeRegiaoEditando = nomeRegiaoEditando;
        this.flags = flags;
        this.valorNomePreservado = nome;
        this.valorDonosPreservado = donos;
        this.valorEntradaPreservado = entrada;
        this.valorSaidaPreservado = saida;
    }

    @Override
    protected void init() {
        int larguraCampo = LARGURA_TELA - PADDING * 2;
        alturaTelaCalculada = PADDING_TOPO + ESPACAMENTO_Y * 4 + 5 + ESPACAMENTO_Y + ESPACAMENTO_Y + PADDING;
        this.posX = (this.width - LARGURA_TELA) / 2;
        this.posY = (this.height - alturaTelaCalculada) / 2;

        int offsetY = posY + PADDING_TOPO;

        campoNome = new EditBox(this.font, posX + PADDING, offsetY, larguraCampo, 20, Component.literal("Nome"));
        campoNome.setMaxLength(32);
        campoNome.setHint(Component.literal("Nome da região...").withStyle(s -> s.withColor(COR_HINT)));
        campoNome.setValue(valorNomePreservado);
        this.addRenderableWidget(campoNome);
        offsetY += ESPACAMENTO_Y;

        campoDonos = new EditBox(this.font, posX + PADDING, offsetY, larguraCampo, 20, Component.literal("Donos"));
        campoDonos.setMaxLength(500);
        campoDonos.setHint(Component.literal("Nomes dos donos separados por vírgula...").withStyle(s -> s.withColor(COR_HINT)));
        campoDonos.setValue(valorDonosPreservado);
        campoDonos.setResponder(this::atualizarSugestoesDonos);
        this.addRenderableWidget(campoDonos);
        offsetY += ESPACAMENTO_Y;

        campoMensagemEntrada = new EditBox(this.font, posX + PADDING, offsetY, larguraCampo, 20, Component.literal("Entrada"));
        campoMensagemEntrada.setMaxLength(200);
        campoMensagemEntrada.setHint(Component.literal("Mensagem ao entrar (opcional, use &a para cores)").withStyle(s -> s.withColor(COR_HINT)));
        campoMensagemEntrada.setValue(valorEntradaPreservado);
        this.addRenderableWidget(campoMensagemEntrada);
        offsetY += ESPACAMENTO_Y;

        campoMensagemSaida = new EditBox(this.font, posX + PADDING, offsetY, larguraCampo, 20, Component.literal("Saída"));
        campoMensagemSaida.setMaxLength(200);
        campoMensagemSaida.setHint(Component.literal("Mensagem ao sair (opcional, use &c para cores)").withStyle(s -> s.withColor(COR_HINT)));
        campoMensagemSaida.setValue(valorSaidaPreservado);
        this.addRenderableWidget(campoMensagemSaida);
        offsetY += ESPACAMENTO_Y + 5;

        this.addRenderableWidget(Button.builder(
                Component.literal("⚙ Configurar Flags"), btn -> abrirMenuFlags()
        ).bounds(posX + PADDING, offsetY, larguraCampo, 20).build());
        offsetY += ESPACAMENTO_Y;

        int larguraBotao = (larguraCampo - 10) / 2;
        String labelConfirmar = nomeRegiaoEditando != null ? "✔ Salvar Alterações" : "✔ Criar Região";

        this.addRenderableWidget(Button.builder(
                Component.literal(labelConfirmar), btn -> confirmarRegiao()
        ).bounds(posX + PADDING, offsetY, larguraBotao, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("✖ Cancelar"), btn -> this.onClose()
        ).bounds(posX + PADDING + larguraBotao + 10, offsetY, larguraBotao, 20).build());
    }

    private void atualizarSugestoesDonos(String texto) {
        sugestoesDonos.clear();
        sugestaoSelecionada = -1;
        if (minecraft == null || minecraft.getConnection() == null) return;
        String ultimoNome = obterUltimoNomeDigitado(texto).toLowerCase();
        if (ultimoNome.isEmpty()) return;
        for (var info : minecraft.getConnection().getOnlinePlayers()) {
            String nome = info.getProfile().getName();
            if (nome.toLowerCase().startsWith(ultimoNome) && sugestoesDonos.size() < MAX_SUGESTOES) {
                sugestoesDonos.add(nome);
            }
        }
    }

    private String obterUltimoNomeDigitado(String texto) {
        if (texto.isEmpty()) return "";
        String[] partes = texto.split(",");
        return partes[partes.length - 1].trim();
    }

    private void aplicarSugestao(String sugestao) {
        String texto = campoDonos.getValue();
        int ultimaVirgula = texto.lastIndexOf(',');
        String prefixo = ultimaVirgula >= 0 ? texto.substring(0, ultimaVirgula + 1) + " " : "";
        campoDonos.setValue(prefixo + sugestao + ", ");
        sugestoesDonos.clear();
        sugestaoSelecionada = -1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!sugestoesDonos.isEmpty()) {
            if (keyCode == 264) { sugestaoSelecionada = Math.min(sugestaoSelecionada + 1, sugestoesDonos.size() - 1); return true; }
            if (keyCode == 265) { sugestaoSelecionada = Math.max(sugestaoSelecionada - 1, 0); return true; }
            if (keyCode == 258 || keyCode == 257) {
                int idx = sugestaoSelecionada >= 0 ? sugestaoSelecionada : 0;
                if (idx < sugestoesDonos.size()) { aplicarSugestao(sugestoesDonos.get(idx)); return true; }
            }
            if (keyCode == 256) { sugestoesDonos.clear(); return true; }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!sugestoesDonos.isEmpty() && campoDonos.isFocused()) {
            int boxX = campoDonos.getX();
            int boxY = campoDonos.getY() + campoDonos.getHeight();
            int boxW = campoDonos.getWidth();
            int linhaH = 12;
            for (int i = 0; i < sugestoesDonos.size(); i++) {
                int ly = boxY + i * linhaH;
                if (mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= ly && mouseY <= ly + linhaH) {
                    aplicarSugestao(sugestoesDonos.get(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void abrirMenuFlags() {
        TelaConfigRegiao telaRetorno = new TelaConfigRegiao(
                posicaoMinima, posicaoMaxima, nomeRegiaoEditando, flags,
                campoNome.getValue(), campoDonos.getValue(),
                campoMensagemEntrada.getValue(), campoMensagemSaida.getValue()
        );
        Minecraft.getInstance().setScreen(new TelaFlags(telaRetorno, flags));
    }

    private void confirmarRegiao() {
        String nome = campoNome.getValue().trim();
        if (nome.isEmpty()) {
            if (minecraft != null && minecraft.player != null)
                minecraft.player.sendSystemMessage(Component.literal("§cVocê precisa definir um nome para a região!"));
            return;
        }

        String donosTexto = campoDonos.getValue().trim();
        List<String> donos = new ArrayList<>();
        if (!donosTexto.isEmpty()) {
            for (String dono : donosTexto.split(",")) {
                String donoLimpo = dono.trim();
                if (!donoLimpo.isEmpty()) donos.add(donoLimpo);
            }
        }

        String msgEntrada = campoMensagemEntrada.getValue().trim();
        String msgSaida = campoMensagemSaida.getValue().trim();

        if (nomeRegiaoEditando != null) {
            PacketDistributor.sendToServer(new PacoteEditarRegiao(
                    nomeRegiaoEditando, nome, posicaoMinima, posicaoMaxima,
                    flags, donos, msgEntrada, msgSaida));
        } else {
            PacketDistributor.sendToServer(new PacoteCriarRegiao(
                    nome, posicaoMinima, posicaoMaxima, flags, donos, msgEntrada, msgSaida));
        }
        this.onClose();
    }

    @Override
    public void renderBlurredBackground(float partialTick) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);
        renderizarPainelComTerra(graphics);

        String titulo = nomeRegiaoEditando != null
                ? "Editar Região: " + nomeRegiaoEditando : "Criar Região Protegida";
        graphics.drawCenteredString(this.font, titulo, posX + LARGURA_TELA / 2, posY + 12, COR_TITULO);

        super.render(graphics, mouseX, mouseY, partialTick);
        renderizarSugestoesDonos(graphics, mouseX, mouseY);
    }

    private void renderizarPainelComTerra(GuiGraphics graphics) {
        int x1 = posX, y1 = posY, x2 = posX + LARGURA_TELA, y2 = posY + alturaTelaCalculada;
        int tam = 16;
        graphics.enableScissor(x1, y1, x2, y2);
        for (int ix = 0; ix <= LARGURA_TELA / tam + 1; ix++)
            for (int iy = 0; iy <= alturaTelaCalculada / tam + 1; iy++)
                graphics.blit(TEXTURA_TERRA, x1 + ix * tam, y1 + iy * tam, 0, 0, tam, tam, tam, tam);
        graphics.disableScissor();
        graphics.fill(x1, y1, x2, y2, 0xD0232323);
        renderizarBordaArredondada(graphics, x1, y1, x2, y2, RAIO_CANTO);
    }

    private void renderizarBordaArredondada(GuiGraphics graphics, int x1, int y1, int x2, int y2, int r) {
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                double dist = Math.sqrt((r - 1 - i) * (r - 1 - i) + (r - 1 - j) * (r - 1 - j));
                if (dist >= r) {
                    graphics.fill(x1 + i, y1 + j, x1 + i + 1, y1 + j + 1, 0xFF000000);
                    graphics.fill(x2 - i - 1, y1 + j, x2 - i, y1 + j + 1, 0xFF000000);
                    graphics.fill(x1 + i, y2 - j - 1, x1 + i + 1, y2 - j, 0xFF000000);
                    graphics.fill(x2 - i - 1, y2 - j - 1, x2 - i, y2 - j, 0xFF000000);
                } else if (dist >= r - 1) {
                    graphics.fill(x1 + i, y1 + j, x1 + i + 1, y1 + j + 1, COR_BORDA);
                    graphics.fill(x2 - i - 1, y1 + j, x2 - i, y1 + j + 1, COR_BORDA);
                    graphics.fill(x1 + i, y2 - j - 1, x1 + i + 1, y2 - j, COR_BORDA);
                    graphics.fill(x2 - i - 1, y2 - j - 1, x2 - i, y2 - j, COR_BORDA);
                }
            }
        }
        graphics.fill(x1 + r, y1, x2 - r, y1 + 2, COR_BORDA);
        graphics.fill(x1 + r, y2 - 2, x2 - r, y2, COR_BORDA);
        graphics.fill(x1, y1 + r, x1 + 2, y2 - r, COR_BORDA);
        graphics.fill(x2 - 2, y1 + r, x2, y2 - r, COR_BORDA);
    }

    private void renderizarSugestoesDonos(GuiGraphics graphics, int mouseX, int mouseY) {
        if (sugestoesDonos.isEmpty() || !campoDonos.isFocused()) return;
        int boxX = campoDonos.getX();
        int boxY = campoDonos.getY() + campoDonos.getHeight() + 1;
        int boxW = campoDonos.getWidth();
        int linhaH = 12;
        int totalAltura = sugestoesDonos.size() * linhaH + 4;

        graphics.fill(boxX, boxY, boxX + boxW, boxY + totalAltura, 0xFF1A1A1A);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + 1, 0xFF666666);
        graphics.fill(boxX, boxY + totalAltura - 1, boxX + boxW, boxY + totalAltura, 0xFF666666);
        graphics.fill(boxX, boxY, boxX + 1, boxY + totalAltura, 0xFF666666);
        graphics.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + totalAltura, 0xFF666666);

        for (int i = 0; i < sugestoesDonos.size(); i++) {
            int ly = boxY + 2 + i * linhaH;
            boolean hover = mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= ly && mouseY <= ly + linhaH;
            if (hover || i == sugestaoSelecionada) {
                graphics.fill(boxX + 1, ly, boxX + boxW - 1, ly + linhaH, 0xFF3A6BC4);
            }
            graphics.drawString(this.font, sugestoesDonos.get(i), boxX + 4, ly + 2, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
