package net.aelysium.aelysiummod.sistemas.admin.atalhos.cliente;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.atalhos.EstadoEspiao;
import net.aelysium.aelysiummod.sistemas.admin.atalhos.rede.PacoteAlternarEspiao;
import net.aelysium.aelysiummod.sistemas.rp.deus.cliente.TelaDeuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class BotoesAdminCliente {
    private static final int PERMISSAO = 2;
    private static final int TAMANHO = 16;
    private static final int ESPACO = 2;
    private static final int FOLGA = 4;
    private static final int BRILHO_HOVER = 22;
    private static final int MARGEM_PAINEL = 3;
    private static final int MARGEM_CIMA = 2;
    private static final int MARGEM_BAIXO = 2;
    private static final int DISTANCIA_EXTRA_PASTA = 1;
    private static final int COR_BORDA_PAINEL = 0xFF0E0E0E;
    private static final int COR_FUNDO_PAINEL = 0xF0252525;

    private record Atalho(ResourceLocation normal, ResourceLocation pressionado, Supplier<Component> dica, Runnable acao, IntSupplier nivel, ResourceLocation total, boolean pasta) {
    }

    private record Posicionado(Atalho atalho, int x, int y, boolean filho) {
    }

    private static boolean expandida = false;
    private static Atalho pressionado = null;

    private static final Atalho PASTA = new Atalho(textura("botao_pasta_fechada"), textura("botao_pasta"),
            () -> Component.translatable("gui.aelysiummod.admin.botao_pasta"),
            () -> expandida = !expandida, () -> expandida ? 1 : 0, null, true);

    private static final Atalho DEUSES = new Atalho(textura("botao_deus"), textura("botao_deus_pressionado"),
            () -> Component.translatable("gui.aelysiummod.admin.botao_deuses"),
            BotoesAdminCliente::abrirDeuses, () -> 0, null, false);

    private static final Atalho PERSONAGENS = new Atalho(textura("botao_personagem"), textura("botao_personagem_pressionado"),
            () -> Component.translatable("gui.aelysiummod.admin.botao_personagens"),
            BotoesAdminCliente::abrirPersonagens, () -> 0, null, false);

    private static final Atalho ITENS = new Atalho(textura("botao_itens"), textura("botao_itens_pressionado"),
            () -> Component.translatable("gui.aelysiummod.admin.botao_itens"),
            BotoesAdminCliente::abrirItens, () -> 0, null, false);

    private static final Atalho ESPIAR = new Atalho(textura("botao_espiar"), textura("botao_espiar_pressionado"),
            () -> Component.translatable("gui.aelysiummod.admin.botao_espiar_" + switch (EstadoEspiao.nivel()) {
                case 1 -> "privado";
                case 2 -> "tudo";
                default -> "desligado";
            }),
            BotoesAdminCliente::alternarEspiao, EstadoEspiao::nivel, textura("botao_espiar_total"), false);

    private static final List<Atalho> COLUNA = List.of(PASTA, ITENS, ESPIAR);
    private static final List<Atalho> PASTA_CONTEUDO = List.of(DEUSES, PERSONAGENS);

    private BotoesAdminCliente() {
    }

    private static ResourceLocation textura(String nome) {
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/admin/" + nome + ".png");
    }

    private static void abrirDeuses() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.closeContainer();
        mc.setScreen(new TelaDeuses());
    }

    private static void abrirPersonagens() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.closeContainer();
        mc.setScreen(new net.aelysium.aelysiummod.sistemas.rp.personagem.cliente.TelaPersonagens());
    }

    private static void abrirItens() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.closeContainer();
        mc.setScreen(new net.aelysium.aelysiummod.sistemas.admin.banimentos.cliente.TelaItensBanidos());
    }

    private static void alternarEspiao() {
        PacketDistributor.sendToServer(new PacoteAlternarEspiao());
    }

    private static boolean ativo(Screen tela) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasPermissions(PERMISSAO)) return false;
        if (tela instanceof InventoryScreen inventario) {
            return !inventario.getRecipeBookComponent().isVisible();
        }
        return tela instanceof CreativeModeInventoryScreen;
    }

    private static int colunaX(AbstractContainerScreen<?> tela) {
        return tela.getGuiLeft() - FOLGA - TAMANHO;
    }

    private static int colunaY(AbstractContainerScreen<?> tela, int indice) {
        return tela.getGuiTop() + FOLGA + indice * (TAMANHO + ESPACO) + (indice > 0 ? DISTANCIA_EXTRA_PASTA : 0);
    }

    private static int filhosLargura() {
        return PASTA_CONTEUDO.size() * TAMANHO + (PASTA_CONTEUDO.size() - 1) * ESPACO;
    }

    private static int painelLargura() {
        return MARGEM_PAINEL + filhosLargura() + ESPACO + TAMANHO + MARGEM_PAINEL;
    }

    private static int painelX(AbstractContainerScreen<?> tela) {
        return colunaX(tela) - ESPACO - filhosLargura() - MARGEM_PAINEL;
    }

    private static int painelY(AbstractContainerScreen<?> tela) {
        return colunaY(tela, 0) - MARGEM_CIMA;
    }

    private static List<Posicionado> layout(AbstractContainerScreen<?> tela) {
        List<Posicionado> lista = new ArrayList<>();
        for (int i = 0; i < COLUNA.size(); i++) {
            lista.add(new Posicionado(COLUNA.get(i), colunaX(tela), colunaY(tela, i), false));
        }
        if (expandida) {
            int base = painelX(tela) + MARGEM_PAINEL;
            for (int i = 0; i < PASTA_CONTEUDO.size(); i++) {
                lista.add(new Posicionado(PASTA_CONTEUDO.get(i), base + i * (TAMANHO + ESPACO), colunaY(tela, 0), true));
            }
        }
        return lista;
    }

    private static Posicionado sob(List<Posicionado> lista, double mouseX, double mouseY) {
        for (Posicionado item : lista) {
            if (mouseX >= item.x() && mouseX < item.x() + TAMANHO && mouseY >= item.y() && mouseY < item.y() + TAMANHO) return item;
        }
        return null;
    }

    @SubscribeEvent
    public static void aoSair(ClientPlayerNetworkEvent.LoggingOut evento) {
        EstadoEspiao.definir(0);
        pressionado = null;
        expandida = false;
    }

    private static void brilho(GuiGraphics g, ResourceLocation textura, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        g.setColor(1.0F, 1.0F, 1.0F, BRILHO_HOVER / 100.0F);
        g.blit(textura, x, y, 0, 0, TAMANHO, TAMANHO, TAMANHO, TAMANHO);
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void aoRenderizar(ScreenEvent.Render.Post evento) {
        if (!ativo(evento.getScreen()) || !(evento.getScreen() instanceof AbstractContainerScreen<?> tela)) return;
        GuiGraphics g = evento.getGuiGraphics();
        List<Posicionado> lista = layout(tela);
        Posicionado alvo = sob(lista, evento.getMouseX(), evento.getMouseY());
        if (expandida) {
            int px = painelX(tela);
            int py = painelY(tela);
            int largura = painelLargura();
            int altura = MARGEM_CIMA + TAMANHO + MARGEM_BAIXO;
            g.fill(px + 1, py, px + largura - 1, py + altura, COR_BORDA_PAINEL);
            g.fill(px, py + 1, px + largura, py + altura - 1, COR_BORDA_PAINEL);
            g.fill(px + 1, py + 1, px + largura - 1, py + altura - 1, COR_FUNDO_PAINEL);
        }
        for (Posicionado item : lista) {
            Atalho atalho = item.atalho();
            int nivel = atalho.nivel().getAsInt();
            boolean segurando = pressionado == atalho && alvo == item;
            boolean afundado = nivel > 0 || segurando;
            ResourceLocation textura = nivel >= 2 && atalho.total() != null ? atalho.total() : afundado ? atalho.pressionado() : atalho.normal();
            if (atalho.pasta() && segurando) {
                g.setColor(0.7F, 0.7F, 0.7F, 1.0F);
                g.blit(textura, item.x(), item.y(), 0, 0, TAMANHO, TAMANHO, TAMANHO, TAMANHO);
                g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                g.blit(textura, item.x(), item.y(), 0, 0, TAMANHO, TAMANHO, TAMANHO, TAMANHO);
            }
            if (alvo == item && !segurando && (!afundado || atalho.pasta())) {
                brilho(g, textura, item.x(), item.y());
            }
        }
        if (alvo != null && !(alvo.atalho().pasta() && expandida)) {
            Font fonte = Minecraft.getInstance().font;
            Component dica = alvo.atalho().dica().get();
            if (alvo.filho()) {
                g.renderTooltip(fonte, dica, evento.getMouseX(), evento.getMouseY());
            } else {
                g.renderTooltip(fonte, dica, alvo.x() - 16 - fonte.width(dica) - 4, alvo.y() + TAMANHO / 2 + 8);
            }
        }
    }

    @SubscribeEvent
    public static void aoClicar(ScreenEvent.MouseButtonPressed.Pre evento) {
        if (evento.getButton() != 0 || !ativo(evento.getScreen()) || !(evento.getScreen() instanceof AbstractContainerScreen<?> tela)) return;
        Posicionado alvo = sob(layout(tela), evento.getMouseX(), evento.getMouseY());
        if (alvo == null) return;
        pressionado = alvo.atalho();
        if (alvo.atalho().pasta()) {
            alvo.atalho().acao().run();
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        evento.setCanceled(true);
    }

    @SubscribeEvent
    public static void aoSoltar(ScreenEvent.MouseButtonReleased.Pre evento) {
        if (evento.getButton() != 0 || pressionado == null) return;
        Atalho marcado = pressionado;
        pressionado = null;
        if (!ativo(evento.getScreen()) || !(evento.getScreen() instanceof AbstractContainerScreen<?> tela)) return;
        Posicionado alvo = sob(layout(tela), evento.getMouseX(), evento.getMouseY());
        if (alvo != null && alvo.atalho() == marcado && !marcado.pasta()) {
            marcado.acao().run();
        }
        evento.setCanceled(true);
    }
}
