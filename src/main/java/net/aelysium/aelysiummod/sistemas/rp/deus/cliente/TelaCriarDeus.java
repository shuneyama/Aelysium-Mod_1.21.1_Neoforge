package net.aelysium.aelysiummod.sistemas.rp.deus.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente;
import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.nucleo.jogador.ConsultaInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.aelysium.aelysiummod.sistemas.rp.deus.RegistroDeuses;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteCriarDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteEditarDeus;
import net.aelysium.aelysiummod.sistemas.rp.personagem.cliente.TelaSeletorCor;
import net.aelysium.aelysiummod.sistemas.social.insignia.cliente.TelaSeletorInsignia;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.aelysium.aelysiummod.sistemas.social.apelido.Pronome;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.neoforged.neoforge.network.PacketDistributor;

public class TelaCriarDeus extends Screen {
    private static final int LARGURA = 216;
    private static final int ALTURA = 220;
    private static final int COR_TEXTO = 0xFF404040;

    private final String idEdicao;
    private String nome;
    private int cor;
    private String badge;
    private int corParticula;
    private String pronomes;
    private int poderes;
    private int x0;
    private int y0;
    private EditBox campoNome;
    private Button botaoCriar;

    public TelaCriarDeus() {
        this(null, "", 0x7FDBFF, TipoInsignia.DEUS.id, -1, "", 0);
    }

    public TelaCriarDeus(String idEdicao, String nome, int cor, String badge, int corParticula, String pronomes, int poderes) {
        super(Component.translatable(idEdicao == null ? "gui.aelysiummod.deuses.criar_titulo" : "gui.aelysiummod.deuses.editar_titulo"));
        this.idEdicao = idEdicao;
        this.poderes = poderes;
        this.nome = nome;
        this.cor = cor & 0xFFFFFF;
        this.pronomes = pronomes == null ? "" : pronomes;
        this.corParticula = corParticula < 0 ? this.cor : corParticula & 0xFFFFFF;
        this.badge = badge == null || ConsultaInsignia.AUTOMATICA.equals(badge) ? TipoInsignia.DEUS.id : badge;
    }

    @Override
    protected void init() {
        x0 = (width - LARGURA) / 2;
        y0 = (height - ALTURA) / 2;
        campoNome = new EditBox(font, x0 + 20, y0 + 32, 150, 18, Component.translatable("gui.aelysiummod.deuses.nome"));
        campoNome.setMaxLength(RegistroDeuses.TAMANHO_MAXIMO_NOME);
        campoNome.setFilter(texto -> texto.chars().noneMatch(Character::isWhitespace));
        campoNome.setValue(nome);
        campoNome.setResponder(texto -> {
            nome = texto;
            validar();
        });
        addRenderableWidget(campoNome);

        amostra(x0 + 20 + 150 + 8, y0 + 32);

        addRenderableWidget(Button.builder(textoBadge(), b ->
                minecraft.setScreen(new TelaSeletorInsignia(this, selecionada -> badge = selecionada, false))
        ).bounds(x0 + 20, y0 + 60, 176, 20).build());

        addRenderableWidget(Button.builder(textoPronomes(), b -> {
            pronomes = Pronome.proximoId(pronomes);
            b.setMessage(textoPronomes());
        }).bounds(x0 + 20, y0 + 84, 176, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.poderes.botao"),
                b -> minecraft.setScreen(new TelaPoderes(this, poderes, valor -> poderes = valor)))
                .bounds(x0 + 20, y0 + 108, 176, 20).build());

        Button botaoSkin = addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.skin.botao"),
                b -> minecraft.setScreen(new net.aelysium.aelysiummod.sistemas.rp.skin.cliente.TelaSkin(this,
                        net.aelysium.aelysiummod.sistemas.rp.skin.RegistroSkins.TIPO_DEUS, idEdicao)))
                .bounds(x0 + 20, y0 + 132, 176, 20).build());
        botaoSkin.active = idEdicao != null;
        if (idEdicao == null) botaoSkin.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.aelysiummod.skin.salvar_antes")));

        int larguraBotao = 100;
        int y = y0 + ALTURA - 30;
        botaoCriar = addRenderableWidget(Button.builder(Component.translatable(idEdicao == null ? "gui.aelysiummod.deuses.confirmar" : "gui.aelysiummod.deuses.salvar"), b -> criar())
                .bounds(x0 + LARGURA / 2 - 102, y, larguraBotao, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.deuses.cancelar"), b -> onClose())
                .bounds(x0 + LARGURA / 2 + 2, y, larguraBotao, 20).build());
        setInitialFocus(campoNome);
        validar();
    }

    private void amostra(int x, int y) {
        addRenderableWidget(new Button(x, y, 18, 18, Component.empty(), b ->
                minecraft.setScreen(new TelaSeletorCorDupla(this, cor, corParticula, nome, (poderes & TipoDeus.PARTICULA) != 0, (c1, c2) -> {
                    cor = c1;
                    corParticula = c2;
                })), supplier -> supplier.get()) {
            @Override
            protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float parcial) {
                g.fill(getX(), getY(), getX() + 18, getY() + 18, isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000);
                g.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, 0xFF000000);
                g.fill(getX() + 2, getY() + 2, getX() + 16, getY() + 16, 0xFF000000 | cor);
            }
        });
    }

    private Component textoPronomes() {
        Pronome p = Pronome.deId(pronomes);
        Component valor = p == null
                ? Component.translatable("gui.aelysiummod.apelido.badge_nenhum")
                : Component.literal(p.texto).withStyle(e -> e.withColor(TextColor.fromRgb(p.cor)));
        return Component.translatableEscape("gui.aelysiummod.apelido.pronomes_botao", valor);
    }

    private Component textoBadge() {
        Component valor;
        if (badge == null || badge.isEmpty() || badge.equals(ConsultaInsignia.AUTOMATICA)) {
            valor = Component.translatable("gui.aelysiummod.apelido.badge_nenhum");
        } else {
            TipoInsignia tipo = TipoInsignia.fromId(badge);
            var personalizada = InsigniasPersonalizadasCliente.porId(badge);
            if (tipo != null) valor = Component.literal(tipo.id);
            else if (personalizada != null) valor = Component.literal(personalizada.dados().texto());
            else valor = Component.translatable("gui.aelysiummod.apelido.badge_nenhum");
        }
        return Component.translatableEscape("gui.aelysiummod.personagens.badge", valor);
    }

    private void validar() {
        if (botaoCriar == null) return;
        botaoCriar.active = !nome.trim().isEmpty() && !RegistroDeuses.gerarId(nome).isEmpty();
    }

    private void criar() {
        String limpo = nome.trim();
        String insignia = badge == null ? "" : badge;
        if (idEdicao == null) {
            PacketDistributor.sendToServer(new PacoteCriarDeus(limpo, cor, insignia, corParticula, pronomes, poderes));
        } else {
            PacketDistributor.sendToServer(new PacoteEditarDeus(idEdicao, limpo, cor, insignia, corParticula, pronomes, poderes));
        }
        minecraft.setScreen(new TelaDeuses());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.renderBackground(g, mouseX, mouseY, parcial);
        PainelGui.painel(g, x0, y0, LARGURA, ALTURA);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.render(g, mouseX, mouseY, parcial);
        g.drawString(font, title, x0 + (LARGURA - font.width(title)) / 2, y0 + 8, COR_TEXTO, false);
        g.drawString(font, Component.translatable("gui.aelysiummod.deuses.nome"), x0 + 20, y0 + 22, COR_TEXTO, false);
        Component rotuloCor = Component.translatable("gui.aelysiummod.personagens.cor");
        g.drawString(font, rotuloCor, x0 + 20 + 150 + 8, y0 + 22, COR_TEXTO, false);
        String previa = nome.trim().isEmpty() ? Component.translatable("gui.aelysiummod.deuses.previa").getString() : nome.trim();
        g.drawString(font, previa, x0 + (LARGURA - font.width(previa)) / 2, y0 + 164, 0xFF000000 | cor, true);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new TelaDeuses());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
