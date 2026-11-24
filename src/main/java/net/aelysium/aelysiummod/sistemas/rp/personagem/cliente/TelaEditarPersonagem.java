package net.aelysium.aelysiummod.sistemas.rp.personagem.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.nucleo.cliente.TelasCliente;
import net.aelysium.aelysiummod.nucleo.jogador.ConsultaInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.rp.personagem.Personagem;
import net.aelysium.aelysiummod.sistemas.rp.personagem.rede.PacoteSalvarPersonagem;
import net.aelysium.aelysiummod.sistemas.social.apelido.Pronome;
import net.aelysium.aelysiummod.sistemas.social.apelido.UtilitariosApelido;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class TelaEditarPersonagem extends Screen {
    private static final int LARGURA = 216;
    private static final int ALTURA = 266;
    private static final int COR_TEXTO = 0xFF404040;

    private final Screen pai;
    private final String id;

    private String nome;
    private int nickCor;
    private int nickFormato;
    private String prefixo;
    private int prefixoCor1;
    private int prefixoCor2;
    private int prefixoFormato;
    private String sufixo;
    private int sufixoCor1;
    private int sufixoCor2;
    private int sufixoFormato;
    private String pronomes;
    private String badge;
    private TipoRaca raca;

    private int x0;
    private int y0;
    private Button botaoSalvar;

    public TelaEditarPersonagem(Screen pai, Personagem original) {
        super(Component.translatable(original == null ? "gui.aelysiummod.personagens.novo" : "gui.aelysiummod.personagens.editando"));
        this.pai = pai;
        if (original == null) {
            id = "";
            nome = "";
            nickCor = 0xFFFFFF;
            prefixo = "";
            prefixoCor1 = 0xFFFFFF;
            prefixoCor2 = -1;
            sufixo = "";
            sufixoCor1 = 0xFFFFFF;
            sufixoCor2 = -1;
            pronomes = "";
            badge = ConsultaInsignia.NENHUMA;
            raca = TipoRaca.HUMANO;
        } else {
            id = original.id();
            nome = original.nome();
            nickCor = original.nickCor();
            nickFormato = original.nickFormato();
            prefixo = original.prefixo();
            prefixoCor1 = original.prefixoCor1();
            prefixoCor2 = original.prefixoCor2();
            prefixoFormato = original.prefixoFormato();
            sufixo = original.sufixo();
            sufixoCor1 = original.sufixoCor1();
            sufixoCor2 = original.sufixoCor2();
            sufixoFormato = original.sufixoFormato();
            pronomes = original.pronomes();
            badge = original.badge();
            raca = TipoRaca.fromId(original.raca());
            if (raca == TipoRaca.NONE) raca = TipoRaca.HUMANO;
        }
    }

    private static boolean semEspacos(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isWhitespace(texto.charAt(i))) return false;
        }
        return true;
    }

    private EditBox campoTexto(int x, int y, int largura, int maximo, String valor, Consumer<String> aoMudar) {
        EditBox campo = new EditBox(font, x, y, largura, 18, Component.empty());
        campo.setMaxLength(maximo);
        campo.setFilter(TelaEditarPersonagem::semEspacos);
        campo.setValue(valor);
        campo.setResponder(texto -> {
            aoMudar.accept(texto);
            atualizarSalvar();
        });
        addRenderableWidget(campo);
        return campo;
    }

    private void campoCor(int x, int y, IntSupplier valor, boolean opcional, java.util.function.Supplier<String> previa, IntConsumer aoMudar) {
        addRenderableWidget(new Button(x, y, 18, 18, Component.empty(), btn ->
                minecraft.setScreen(new TelaSeletorCor(this, valor.getAsInt(), opcional, previa.get(), aoMudar)), supplier -> supplier.get()) {
            @Override
            protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float parcial) {
                int v = valor.getAsInt();
                g.fill(getX(), getY(), getX() + 18, getY() + 18, isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000);
                g.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, 0xFF000000);
                if (v >= 0) {
                    g.fill(getX() + 2, getY() + 2, getX() + 16, getY() + 16, 0xFF000000 | v);
                } else {
                    g.fill(getX() + 2, getY() + 2, getX() + 16, getY() + 16, 0xFF555555);
                    g.drawString(font, "-", getX() + 7, getY() + 5, 0xFFAAAAAA, false);
                }
            }
        });
    }

    private final int[] fmtNick = new int[1];
    private final int[] fmtPrefixo = new int[1];
    private final int[] fmtSufixo = new int[1];
    private boolean formatosCarregados;

    @Override
    protected void init() {
        if (!formatosCarregados) {
            fmtNick[0] = nickFormato;
            fmtPrefixo[0] = prefixoFormato;
            fmtSufixo[0] = sufixoFormato;
            formatosCarregados = true;
        }
        x0 = (width - LARGURA) / 2;
        y0 = Math.max(4, (height - ALTURA) / 2);
        int xc1 = x0 + 20 + 150 + 8;
        int y = y0 + 32;

        campoTexto(x0 + 20, y, 150, Personagem.NOME_MAXIMO, nome, t -> nome = t);
        campoCor(xc1, y, () -> nickCor, false, () -> nome, c -> nickCor = c);

        y += 32;
        campoTexto(x0 + 20, y, 150, Personagem.AFIXO_MAXIMO, prefixo, t -> prefixo = t);
        campoCor(xc1, y, () -> prefixoCor1, false, () -> prefixo, c -> prefixoCor1 = c);

        y += 32;
        campoTexto(x0 + 20, y, 150, Personagem.AFIXO_MAXIMO, sufixo, t -> sufixo = t);
        campoCor(xc1, y, () -> sufixoCor1, false, () -> sufixo, c -> sufixoCor1 = c);

        y += 28;
        int lb = 176;
        int bx = x0 + 20;
        addRenderableWidget(Button.builder(textoPronomes(), b -> {
            pronomes = Pronome.proximoId(pronomes);
            b.setMessage(textoPronomes());
        }).bounds(bx, y, lb, 20).build());
        y += 22;
        addRenderableWidget(Button.builder(textoRaca(), b -> {
            TipoRaca[] todas = TipoRaca.values();
            do {
                raca = todas[(raca.ordinal() + 1) % todas.length];
            } while (raca == TipoRaca.NONE);
            b.setMessage(textoRaca());
        }).bounds(bx, y, lb, 20).build());
        y += 22;
        addRenderableWidget(Button.builder(textoBadge(), b ->
                minecraft.setScreen(new net.aelysium.aelysiummod.sistemas.social.insignia.cliente.TelaSeletorInsignia(this, selecionada -> badge = selecionada, false))
        ).bounds(bx, y, lb, 20).build());

        y += 22;
        Button botaoSkin = addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.skin.botao"),
                b -> minecraft.setScreen(new net.aelysium.aelysiummod.sistemas.rp.skin.cliente.TelaSkin(this,
                        net.aelysium.aelysiummod.sistemas.rp.skin.RegistroSkins.TIPO_PERSONAGEM, id)))
                .bounds(bx, y, lb, 20).build());
        botaoSkin.active = !id.isEmpty();
        if (id.isEmpty()) botaoSkin.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.aelysiummod.skin.salvar_antes")));

        y += 44;
        botaoSalvar = addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.apelido.salvar"), b -> salvar())
                .bounds(x0 + LARGURA / 2 - 102, y, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.apelido.cancelar"), b -> onClose())
                .bounds(x0 + LARGURA / 2 + 2, y, 100, 20).build());
        atualizarSalvar();
    }

    private void atualizarSalvar() {
        if (botaoSalvar != null) botaoSalvar.active = !nome.isBlank();
    }

    private Component textoPronomes() {
        Pronome p = Pronome.deId(pronomes);
        Component valor = p == null
                ? Component.translatable("gui.aelysiummod.apelido.badge_nenhum")
                : Component.literal(p.texto).withStyle(e -> e.withColor(TextColor.fromRgb(p.cor)));
        return Component.translatableEscape("gui.aelysiummod.apelido.pronomes_botao", valor);
    }

    private Component textoRaca() {
        return Component.translatable("gui.aelysiummod.personagens.raca", Component.literal(raca.displayName));
    }

    private Component textoBadge() {
        Component valor;
        if (badge == null || badge.equals(ConsultaInsignia.AUTOMATICA) || badge.isEmpty()) {
            valor = Component.translatable("gui.aelysiummod.apelido.badge_nenhum");
        } else {
            TipoInsignia tipo = TipoInsignia.fromId(badge);
            var personalizada = net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente.porId(badge);
            if (tipo != null) valor = Component.literal(tipo.id);
            else if (personalizada != null) valor = Component.literal(personalizada.dados().texto());
            else valor = Component.translatable("gui.aelysiummod.apelido.badge_nenhum");
        }
        return Component.translatableEscape("gui.aelysiummod.personagens.badge", valor);
    }

    private void salvar() {
        Personagem personagem = new Personagem(id, nome.trim(),
                prefixo.trim(), prefixoCor1, prefixoCor2, fmtPrefixo[0],
                nickCor, fmtNick[0],
                sufixo.trim(), sufixoCor1, sufixoCor2, fmtSufixo[0],
                pronomes == null ? "" : pronomes, badge == null || badge.equals(ConsultaInsignia.AUTOMATICA) ? ConsultaInsignia.NENHUMA : badge, raca.id);
        PacketDistributor.sendToServer(new PacoteSalvarPersonagem(personagem));
        minecraft.setScreen(pai);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(pai);
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

        int xc1 = x0 + 20 + 150 + 8;
        int y = y0 + 22;
        rotulo(g, "gui.aelysiummod.personagens.nome", x0 + 20, y);
        rotulo(g, "gui.aelysiummod.personagens.cor", xc1, y);
        y += 32;
        rotulo(g, "gui.aelysiummod.personagens.prefixo", x0 + 20, y);
        rotulo(g, "gui.aelysiummod.personagens.cor", xc1, y);
        y += 32;
        rotulo(g, "gui.aelysiummod.personagens.sufixo", x0 + 20, y);
        rotulo(g, "gui.aelysiummod.personagens.cor", xc1, y);

        int py = y0 + 32 + 32 + 32 + 28 + 44 + 22 + 26;
        MutableComponent previa = montarPrevia();
        g.drawString(font, previa, x0 + (LARGURA - font.width(previa)) / 2, py + 2, 0xFFFFFFFF, true);
    }

    private void rotulo(GuiGraphics g, String chave, int x, int y) {
        g.drawString(font, Component.translatable(chave), x, y, COR_TEXTO, false);
    }

    private MutableComponent montarPrevia() {
        MutableComponent r = Component.empty();
        if (!prefixo.isEmpty()) {
            r.append(UtilitariosApelido.renderDualColor(prefixo, prefixoCor1, prefixoCor2, fmtPrefixo[0]));
            r.append(Component.literal(" "));
        }
        r.append(nome.isEmpty() ? Component.literal("???") : UtilitariosApelido.renderSingleColor(nome, nickCor, fmtNick[0]));
        if (!sufixo.isEmpty()) {
            r.append(Component.literal(" "));
            r.append(UtilitariosApelido.renderDualColor(sufixo, sufixoCor1, sufixoCor2, fmtSufixo[0]));
        }
        Pronome p = Pronome.deId(pronomes);
        if (p != null) {
            r.append(Component.literal(" "));
            r.append(Component.literal("(" + p.texto + ")").withStyle(e -> e.withColor(TextColor.fromRgb(p.cor))));
        }
        return r;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
