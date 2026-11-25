package net.aelysium.aelysiummod.sistemas.admin.banimentos.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.DadosBanimentoCliente;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteAcaoBanimento;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteEstadoAlvo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TelaItensBanidos extends Screen implements net.aelysium.aelysiummod.nucleo.cliente.AreaMantemFoco {
    private static final int LARGURA = 248;
    private static final int ALTURA = 252;
    private static final int COLUNAS = 9;
    private static final int LINHAS = 5;
    private static final int CELULA = 18;
    private static final int GRADE_L = COLUNAS * CELULA;
    private static final int GRADE_A = LINHAS * CELULA;
    private static final int BARRA_L = 8;
    private static final int COR_TEXTO = 0xFF404040;
    private static final int MOD_MAXIMO = 15;

    private record Entrada(ResourceLocation id, ItemStack pilha, String busca) {
    }

    private static List<Entrada> todas;
    private static List<String> mods;
    private static PacoteEstadoAlvo estadoRecebido;

    private final List<Entrada> filtradas = new ArrayList<>();
    private EditBox campoBusca;
    private Button botaoExibir;
    private Button botaoMod;
    private EditBox campoAlvo;
    private int consultaEm = -1;
    private PacoteEstadoAlvo estado;
    private final List<String> sugestoes = new ArrayList<>();
    private int sugestaoIndice;
    private Button botaoBanirMod;
    private Button botaoDesbanirMod;
    private int x0;
    private int y0;
    private int rolagem;
    private boolean arrastando;
    private int exibir;
    private int modIndice = -1;
    private int chaveAnterior = Integer.MIN_VALUE;

    public TelaItensBanidos() {
        super(Component.translatable("gui.aelysiummod.itens.titulo"));
        carregarCatalogo();
    }

    private static synchronized void carregarCatalogo() {
        if (todas != null) return;
        List<Entrada> lista = new ArrayList<>();
        Set<String> namespaces = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            ItemStack pilha = new ItemStack(item);
            lista.add(new Entrada(id, pilha, (id + " " + pilha.getHoverName().getString()).toLowerCase(Locale.ROOT)));
            namespaces.add(id.getNamespace());
        }
        lista.sort(Comparator.comparing((Entrada e) -> e.id().getNamespace().equals("minecraft") ? "" : e.id().getNamespace())
                .thenComparing(e -> e.id().getPath()));
        todas = lista;
        mods = new ArrayList<>(namespaces);
        mods.sort(Comparator.comparing((String m) -> !m.equals("minecraft")).thenComparing(m -> m));
    }

    public static void receber(PacoteEstadoAlvo pacote) {
        if (Minecraft.getInstance().screen instanceof TelaItensBanidos tela) tela.aceitarEstado(pacote);
    }

    private void aceitarEstado(PacoteEstadoAlvo pacote) {
        if (!pacote.alvoTexto().equals(textoAlvo())) return;
        estado = pacote;
        filtrar();
        atualizarBotoes();
    }

    private String textoAlvo() {
        return campoAlvo == null ? "" : campoAlvo.getValue().trim();
    }

    private void atualizarSugestoes() {
        sugestoes.clear();
        sugestaoIndice = 0;
        String t = campoAlvo == null ? "" : campoAlvo.getValue();
        if (t.isEmpty()) return;
        if (t.startsWith("@")) {
            if (t.length() == 1) sugestoes.addAll(List.of("@a", "@p", "@r", "@s"));
            return;
        }
        var conexao = Minecraft.getInstance().getConnection();
        if (conexao == null) return;
        String minusculo = t.toLowerCase(Locale.ROOT);
        List<String> nomes = new ArrayList<>();
        for (PlayerInfo info : conexao.getOnlinePlayers()) {
            String nome = info.getProfile().getName();
            if (nome.toLowerCase(Locale.ROOT).startsWith(minusculo) && !nome.equals(t)) nomes.add(nome);
        }
        for (String nick : net.aelysium.aelysiummod.sistemas.social.apelido.cliente.ManipuladorApelidoCliente.getAllNicknames()) {
            if (nick.toLowerCase(Locale.ROOT).startsWith(minusculo) && !nick.equals(t) && !nomes.contains(nick)) nomes.add(nick);
        }
        nomes.sort(Comparator.comparing(n -> n.toLowerCase(Locale.ROOT)));
        for (int i = 0; i < nomes.size() && i < 8; i++) sugestoes.add(nomes.get(i));
    }

    private void aceitarSugestao(int indice) {
        if (indice < 0 || indice >= sugestoes.size()) return;
        campoAlvo.setValue(sugestoes.get(indice));
        campoAlvo.moveCursorToEnd(false);
    }

    private int sugestaoX() {
        return x0 + 10;
    }

    private int sugestaoY() {
        return y0 + 94;
    }

    private int sugestaoLargura() {
        return LARGURA - 20;
    }

    @Override
    public boolean mantemFoco(double mx, double my) {
        int x = sugestaoX();
        int y = sugestaoY();
        return sugestoesVisiveis() && mx >= x && mx < x + sugestaoLargura() && my >= y && my < y + sugestoes.size() * 11 + 2;
    }

    private boolean sugestoesVisiveis() {
        return campoAlvo != null && campoAlvo.isFocused() && !sugestoes.isEmpty();
    }

    private void desenharSugestoes(GuiGraphics g, int mouseX, int mouseY) {
        int altura = sugestoes.size() * 11 + 2;
        int x = sugestaoX();
        int y = sugestaoY();
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        g.fill(x - 1, y - 1, x + sugestaoLargura() + 1, y + altura + 1, 0xFF000000);
        g.fill(x, y, x + sugestaoLargura(), y + altura, 0xFF202020);
        for (int i = 0; i < sugestoes.size(); i++) {
            int ly = y + 1 + i * 11;
            boolean sobre = mouseX >= x && mouseX < x + sugestaoLargura() && mouseY >= ly && mouseY < ly + 11;
            if (sobre || i == sugestaoIndice) g.fill(x, ly, x + sugestaoLargura(), ly + 11, 0xFF404060);
            g.drawString(font, sugestoes.get(i), x + 3, ly + 2, i == sugestaoIndice ? 0xFFFFFF55 : 0xFFE0E0E0, false);
        }
        g.pose().popPose();
    }

    private boolean modoAlvo() {
        return !textoAlvo().isEmpty();
    }

    private boolean alvoValido() {
        return modoAlvo() && estado != null && estado.alvoTexto().equals(textoAlvo()) && estado.tipo() != PacoteEstadoAlvo.NENHUM;
    }

    private int quantidadeLiberada(ResourceLocation id) {
        if (!alvoValido()) return 0;
        for (PacoteEstadoAlvo.Item i : estado.itens()) {
            if (i.item().equals(id)) return i.quantidade();
        }
        return 0;
    }

    private PacoteEstadoAlvo.Item detalhe(ResourceLocation id) {
        if (!alvoValido()) return null;
        for (PacoteEstadoAlvo.Item i : estado.itens()) {
            if (i.item().equals(id)) return i;
        }
        return null;
    }

    private boolean liberadoParaTodos(ResourceLocation id) {
        return alvoValido() && quantidadeLiberada(id) >= estado.total();
    }

    private Component descricaoAlvo() {
        if (!alvoValido()) return Component.literal(textoAlvo());
        return switch (estado.tipo()) {
            case PacoteEstadoAlvo.VARIOS -> Component.translatable("gui.aelysiummod.itens.alvo_varios", estado.total());
            default -> Component.literal(estado.nome());
        };
    }

    private String modSelecionado() {
        return modIndice < 0 ? null : mods.get(modIndice);
    }

    @Override
    protected void init() {
        x0 = (width - LARGURA) / 2;
        y0 = (height - ALTURA) / 2;
        int largura = LARGURA - 20;
        int meia = (largura - 4) / 2;
        int larguraExibir = 100;
        int larguraMod = largura - larguraExibir - 4;

        String texto = campoBusca == null ? "" : campoBusca.getValue();
        campoBusca = new EditBox(font, x0 + 10, y0 + 20, largura, 18, Component.translatable("gui.aelysiummod.itens.buscar"));
        campoBusca.setMaxLength(48);
        campoBusca.setHint(Component.translatable("gui.aelysiummod.itens.buscar"));
        campoBusca.setValue(texto);
        campoBusca.setResponder(t -> {
            rolagem = 0;
            filtrar();
        });
        addRenderableWidget(campoBusca);

        botaoExibir = addRenderableWidget(Button.builder(textoExibir(), b -> {
            exibir = (exibir + 1) % (modoAlvo() ? 3 : 2);
            rolagem = 0;
            filtrar();
            atualizarBotoes();
        }).bounds(x0 + 10, y0 + 42, larguraExibir, 20).build());

        botaoMod = addRenderableWidget(Button.builder(textoMod(), b -> {
            modIndice = modIndice + 1 >= mods.size() ? -1 : modIndice + 1;
            rolagem = 0;
            filtrar();
            atualizarBotoes();
        }).bounds(x0 + 10 + larguraExibir + 4, y0 + 42, larguraMod, 20).build());

        String textoAnterior = campoAlvo == null ? "" : campoAlvo.getValue();
        campoAlvo = new EditBox(font, x0 + 10, y0 + 76, largura, 18, Component.translatable("gui.aelysiummod.itens.alvo"));
        campoAlvo.setMaxLength(100);
        campoAlvo.setHint(Component.translatable("gui.aelysiummod.itens.alvo_dica").withStyle(estilo -> estilo.withColor(0x777777)));
        campoAlvo.setValue(textoAnterior);
        campoAlvo.setResponder(t -> {
            atualizarSugestoes();
            rolagem = 0;
            estado = null;
            if (t.trim().isEmpty()) {
                consultaEm = -1;
                if (exibir == 2) exibir = 0;
                filtrar();
                atualizarBotoes();
            } else {
                consultaEm = 8;
                atualizarBotoes();
            }
        });
        addRenderableWidget(campoAlvo);

        botaoBanirMod = addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.itens.banir_mod"),
                b -> confirmarMod(true)).bounds(x0 + 10, y0 + 206, meia, 20).build());
        botaoDesbanirMod = addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.itens.desbanir_mod"),
                b -> confirmarMod(false)).bounds(x0 + 10 + meia + 4, y0 + 206, meia, 20).build());

        filtrar();
        atualizarBotoes();
    }

    private int gradeX() {
        return x0 + (LARGURA - (GRADE_L + 3 + BARRA_L)) / 2;
    }

    private int gradeY() {
        return y0 + 102;
    }

    private Component textoExibir() {
        String chave = switch (exibir) {
            case 1 -> "banidos";
            case 2 -> "liberados";
            default -> "todos";
        };
        return Component.translatable("gui.aelysiummod.itens.exibir", Component.translatable("gui.aelysiummod.itens.exibir." + chave));
    }

    private Component textoMod() {
        String mod = modSelecionado();
        String nome = mod == null ? Component.translatable("gui.aelysiummod.itens.exibir.todos").getString() : mod;
        return Component.translatable("gui.aelysiummod.itens.mod", nome.length() > MOD_MAXIMO ? nome.substring(0, MOD_MAXIMO) : nome);
    }

    private void atualizarBotoes() {
        if (botaoExibir != null) botaoExibir.setMessage(textoExibir());
        if (botaoMod != null) botaoMod.setMessage(textoMod());
        boolean podeMod = modSelecionado() != null && (!modoAlvo() || alvoValido());
        String sufixo = modoAlvo() ? "_alvo" : "";
        if (botaoBanirMod != null) {
            botaoBanirMod.active = podeMod;
            botaoBanirMod.setMessage(Component.translatable("gui.aelysiummod.itens.banir_mod" + sufixo));
        }
        if (botaoDesbanirMod != null) {
            botaoDesbanirMod.active = podeMod;
            botaoDesbanirMod.setMessage(Component.translatable("gui.aelysiummod.itens.desbanir_mod" + sufixo));
        }
    }

    private void confirmarMod(boolean banir) {
        String mod = modSelecionado();
        if (mod == null) return;
        boolean global = !modoAlvo();
        if (!global && !alvoValido()) return;
        String acao = global ? (banir ? "banir_mod" : "desbanir_mod") : (banir ? "liberar_mod" : "revogar_mod");
        String alvoTexto = global ? "" : textoAlvo();
        long total = global
                ? todas.stream().filter(e -> e.id().getNamespace().equals(mod)).count()
                : todas.stream().filter(e -> e.id().getNamespace().equals(mod) && (banir ? banido(e) && !liberadoParaTodos(e.id()) : quantidadeLiberada(e.id()) > 0)).count();
        String chave = global ? (banir ? "banir_mod" : "desbanir_mod") : (banir ? "liberar_mod" : "revogar_mod");
        Component mensagem = global
                ? Component.translatable("gui.aelysiummod.itens." + chave + "_mensagem", mod, total)
                : Component.translatable("gui.aelysiummod.itens." + chave + "_mensagem", mod, descricaoAlvo(), total);
        minecraft.setScreen(new ConfirmScreen(confirmado -> {
            if (confirmado) PacketDistributor.sendToServer(new PacoteAcaoBanimento(acao, alvoTexto, mod));
            minecraft.setScreen(this);
        }, Component.translatable("gui.aelysiummod.itens." + chave + "_titulo"), mensagem));
    }

    private boolean banido(Entrada e) {
        return DadosBanimentoCliente.isBanned(e.id());
    }

    private void filtrar() {
        filtradas.clear();
        String busca = campoBusca == null ? "" : campoBusca.getValue().trim().toLowerCase(Locale.ROOT);
        String mod = modSelecionado();
        if (!modoAlvo() && exibir == 2) exibir = 0;
        for (Entrada e : todas) {
            if (mod != null && !e.id().getNamespace().equals(mod)) continue;
            if (!busca.isEmpty() && !e.busca().contains(busca)) continue;
            if (exibir == 1 && (!banido(e) || (alvoValido() && liberadoParaTodos(e.id())))) continue;
            if (exibir == 2 && !(banido(e) && liberadoParaTodos(e.id()))) continue;
            filtradas.add(e);
        }
        rolagem = Mth.clamp(rolagem, 0, rolagemMaxima());
        chaveAnterior = chaveEstado();
    }

    private int chaveEstado() {
        return DadosBanimentoCliente.getBannedItems().hashCode() * 31 + (alvoValido() ? estado.hashCode() : 0);
    }

    @Override
    public void tick() {
        if (consultaEm >= 0 && --consultaEm < 0) {
            String t = textoAlvo();
            if (!t.isEmpty()) PacketDistributor.sendToServer(new PacoteAcaoBanimento("consultar", t, ""));
        }
        if (chaveEstado() != chaveAnterior) filtrar();
    }

    private int totalLinhas() {
        return (filtradas.size() + COLUNAS - 1) / COLUNAS;
    }

    private int rolagemMaxima() {
        return Math.max(0, totalLinhas() - LINHAS);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.renderBackground(g, mouseX, mouseY, parcial);
        PainelGui.painel(g, x0, y0, LARGURA, ALTURA);
        PainelGui.rebaixado(g, gradeX() - 1, gradeY() - 1, GRADE_L + 2, GRADE_A + 2);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.render(g, mouseX, mouseY, parcial);
        g.drawString(font, title, x0 + (LARGURA - font.width(title)) / 2, y0 + 8, COR_TEXTO, false);
        Component rotuloAlvo = Component.translatable("gui.aelysiummod.itens.alvo");
        g.drawString(font, rotuloAlvo, x0 + 10, y0 + 66, COR_TEXTO, false);
        if (modoAlvo() && consultaEm < 0) {
            Component situacao = estado == null || estado.tipo() == PacoteEstadoAlvo.NENHUM
                    ? Component.translatable("gui.aelysiummod.itens.alvo_nenhum")
                    : switch (estado.tipo()) {
                        case PacoteEstadoAlvo.OFFLINE -> Component.translatable("gui.aelysiummod.itens.alvo_offline", estado.nome());
                        case PacoteEstadoAlvo.VARIOS -> Component.translatable("gui.aelysiummod.itens.alvo_varios", estado.total());
                        default -> Component.translatable("gui.aelysiummod.itens.alvo_online", estado.nome());
                    };
            int corSituacao = estado == null || estado.tipo() == PacoteEstadoAlvo.NENHUM ? 0xFFAA2020 : 0xFF207020;
            g.drawString(font, situacao, x0 + LARGURA - 10 - font.width(situacao), y0 + 66, corSituacao, false);
        }

        Entrada sob = null;
        for (int i = 0; i < COLUNAS * LINHAS; i++) {
            int indice = rolagem * COLUNAS + i;
            if (indice >= filtradas.size()) break;
            Entrada e = filtradas.get(indice);
            int cx = gradeX() + (i % COLUNAS) * CELULA;
            int cy = gradeY() + (i / COLUNAS) * CELULA;
            boolean b = banido(e);
            if (b) {
                int qtd = quantidadeLiberada(e.id());
                int cor = !alvoValido() || qtd == 0 ? 0x90FF3030 : qtd >= estado.total() ? 0x9055FF55 : 0x90FFD040;
                g.fill(cx, cy, cx + CELULA, cy + CELULA, cor);
            }
            g.renderItem(e.pilha(), cx + 1, cy + 1);
            if (mouseX >= cx && mouseX < cx + CELULA && mouseY >= cy && mouseY < cy + CELULA) {
                g.fill(cx, cy, cx + CELULA, cy + CELULA, 0x50FFFFFF);
                sob = e;
            }
        }
        desenharBarra(g);

        String resumo = Component.translatable("gui.aelysiummod.itens.contagem", filtradas.size(), DadosBanimentoCliente.getBannedItems().size()).getString();
        g.drawString(font, resumo, x0 + (LARGURA - font.width(resumo)) / 2, y0 + ALTURA - 14, 0xFF707070, false);

        if (sugestoesVisiveis()) desenharSugestoes(g, mouseX, mouseY);
        else if (sob != null) desenharDica(g, sob, mouseX, mouseY);
    }

    private void desenharDica(GuiGraphics g, Entrada e, int mouseX, int mouseY) {
        List<Component> linhas = new ArrayList<>(Screen.getTooltipFromItem(minecraft, e.pilha()));
        boolean b = banido(e);
        if (!modoAlvo()) {
            linhas.add(Component.translatable(b ? "gui.aelysiummod.itens.dica_banido" : "gui.aelysiummod.itens.dica_livre"));
            linhas.add(Component.translatable(b ? "gui.aelysiummod.itens.dica_clique_desbanir" : "gui.aelysiummod.itens.dica_clique_banir"));
        } else if (!alvoValido()) {
            linhas.add(Component.translatable("gui.aelysiummod.itens.alvo_nenhum"));
        } else if (!b) {
            linhas.add(Component.translatable("gui.aelysiummod.itens.dica_livre"));
            linhas.add(Component.translatable("gui.aelysiummod.itens.dica_nao_banido_alvo"));
        } else {
            int qtd = quantidadeLiberada(e.id());
            PacoteEstadoAlvo.Item d = detalhe(e.id());
            if (qtd >= estado.total()) {
                linhas.add(Component.translatable("gui.aelysiummod.itens.dica_liberado_para", descricaoAlvo()));
                if (d != null && estado.total() == 1 && !d.por().isEmpty()) {
                    linhas.add(Component.translatable("gui.aelysiummod.itens.dica_liberado_por", d.por(), d.em().isEmpty() ? "?" : d.em()));
                }
                linhas.add(Component.translatable("gui.aelysiummod.itens.dica_clique_revogar"));
            } else if (qtd > 0) {
                linhas.add(Component.translatable("gui.aelysiummod.itens.dica_parcial", qtd, estado.total()));
                if (d != null) {
                    List<String> sem = new ArrayList<>(estado.todos());
                    sem.removeAll(d.nomes());
                    linhas.add(Component.translatable("gui.aelysiummod.itens.dica_com", String.join(", ", d.nomes())));
                    linhas.add(Component.translatable("gui.aelysiummod.itens.dica_sem", String.join(", ", sem)));
                }
                linhas.add(Component.translatable("gui.aelysiummod.itens.dica_clique_liberar"));
            } else {
                linhas.add(Component.translatable("gui.aelysiummod.itens.dica_bloqueado_para", descricaoAlvo()));
                linhas.add(Component.translatable("gui.aelysiummod.itens.dica_clique_liberar"));
            }
        }
        g.renderComponentTooltip(font, linhas, mouseX, mouseY);
    }

    private void desenharBarra(GuiGraphics g) {
        int max = rolagemMaxima();
        if (max <= 0) return;
        int bx = gradeX() + GRADE_L + 3;
        int by = gradeY();
        PainelGui.rebaixado(g, bx, by, BARRA_L, GRADE_A);
        int altura = Math.max(12, (GRADE_A - 2) * LINHAS / totalLinhas());
        int topo = by + 1 + (GRADE_A - 2 - altura) * rolagem / max;
        PainelGui.painel(g, bx + 1, topo, BARRA_L - 2, altura);
    }

    private boolean sobBarra(double mx, double my) {
        int bx = gradeX() + GRADE_L + 3;
        return rolagemMaxima() > 0 && mx >= bx && mx < bx + BARRA_L && my >= gradeY() && my < gradeY() + GRADE_A;
    }

    private void rolarPara(double my) {
        int max = rolagemMaxima();
        if (max <= 0) return;
        double fracao = (my - gradeY()) / GRADE_A;
        rolagem = Mth.clamp((int) Math.round(fracao * max), 0, max);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        rolagem = Mth.clamp(rolagem - (int) Math.signum(sy), 0, rolagemMaxima());
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int botao) {
        if (botao == 0 && sugestoesVisiveis()) {
            int x = sugestaoX();
            int y = sugestaoY();
            if (mx >= x && mx < x + sugestaoLargura() && my >= y && my < y + sugestoes.size() * 11 + 2) {
                aceitarSugestao((int) ((my - y - 1) / 11));
                return true;
            }
        }
        if (botao == 0 && sobBarra(mx, my)) {
            arrastando = true;
            rolarPara(my);
            return true;
        }
        if (botao == 0 && mx >= gradeX() && mx < gradeX() + GRADE_L && my >= gradeY() && my < gradeY() + GRADE_A) {
            int coluna = (int) ((mx - gradeX()) / CELULA);
            int linha = (int) ((my - gradeY()) / CELULA);
            int indice = (rolagem + linha) * COLUNAS + coluna;
            if (indice >= 0 && indice < filtradas.size()) {
                clicarItem(filtradas.get(indice));
                return true;
            }
        }
        return super.mouseClicked(mx, my, botao);
    }

    private void clicarItem(Entrada e) {
        boolean b = banido(e);
        if (!modoAlvo()) {
            PacketDistributor.sendToServer(new PacoteAcaoBanimento(b ? "desbanir" : "banir", "", e.id().toString()));
        } else {
            if (!b || !alvoValido()) return;
            boolean revogar = liberadoParaTodos(e.id());
            PacketDistributor.sendToServer(new PacoteAcaoBanimento(revogar ? "revogar" : "liberar", textoAlvo(), e.id().toString()));
        }
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int modificadores) {
        if (sugestoesVisiveis()) {
            if (tecla == GLFW.GLFW_KEY_TAB || tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER) {
                aceitarSugestao(sugestaoIndice);
                return true;
            }
            if (tecla == GLFW.GLFW_KEY_DOWN) {
                sugestaoIndice = (sugestaoIndice + 1) % sugestoes.size();
                return true;
            }
            if (tecla == GLFW.GLFW_KEY_UP) {
                sugestaoIndice = (sugestaoIndice + sugestoes.size() - 1) % sugestoes.size();
                return true;
            }
        }
        return super.keyPressed(tecla, scan, modificadores);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int botao, double dx, double dy) {
        if (arrastando) {
            rolarPara(my);
            return true;
        }
        return super.mouseDragged(mx, my, botao, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int botao) {
        arrastando = false;
        return super.mouseReleased(mx, my, botao);
    }

    @Override
    public void onClose() {
        if (minecraft.player != null) minecraft.setScreen(new InventoryScreen(minecraft.player));
        else super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
