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

    private record Atalho(ResourceLocation normal, ResourceLocation pressionado, Supplier<Component> dica, Runnable acao, IntSupplier nivel, ResourceLocation total) {
    }

    private static final List<Atalho> ATALHOS = List.of(
            new Atalho(textura("botao_deus"), textura("botao_deus_pressionado"), () -> Component.translatable("gui.aelysiummod.admin.botao_deuses"),
                    BotoesAdminCliente::abrirDeuses, () -> 0, null),
            new Atalho(textura("botao_personagem"), textura("botao_personagem_pressionado"), () -> Component.translatable("gui.aelysiummod.admin.botao_personagens"),
                    BotoesAdminCliente::abrirPersonagens, () -> 0, null),
            new Atalho(textura("botao_espiar"), textura("botao_espiar_pressionado"),
                    () -> Component.translatable("gui.aelysiummod.admin.botao_espiar_" + switch (EstadoEspiao.nivel()) {
                        case 1 -> "privado";
                        case 2 -> "tudo";
                        default -> "desligado";
                    }),
                    BotoesAdminCliente::alternarEspiao, EstadoEspiao::nivel, textura("botao_espiar_total"))
    );

    private static int pressionado = -1;

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

    private static int posX(AbstractContainerScreen<?> tela) {
        return tela.getGuiLeft() - FOLGA - TAMANHO;
    }

    private static int posY(AbstractContainerScreen<?> tela, int indice) {
        return tela.getGuiTop() + FOLGA + indice * (TAMANHO + ESPACO);
    }

    private static int indiceSob(AbstractContainerScreen<?> tela, double mouseX, double mouseY) {
        for (int i = 0; i < ATALHOS.size(); i++) {
            int x = posX(tela);
            int y = posY(tela, i);
            if (mouseX >= x && mouseX < x + TAMANHO && mouseY >= y && mouseY < y + TAMANHO) return i;
        }
        return -1;
    }

    @SubscribeEvent
    public static void aoSair(ClientPlayerNetworkEvent.LoggingOut evento) {
        EstadoEspiao.definir(0);
        pressionado = -1;
    }

    @SubscribeEvent
    public static void aoRenderizar(ScreenEvent.Render.Post evento) {
        if (!ativo(evento.getScreen()) || !(evento.getScreen() instanceof AbstractContainerScreen<?> tela)) return;
        GuiGraphics g = evento.getGuiGraphics();
        int sob = indiceSob(tela, evento.getMouseX(), evento.getMouseY());
        for (int i = 0; i < ATALHOS.size(); i++) {
            Atalho atalho = ATALHOS.get(i);
            int x = posX(tela);
            int y = posY(tela, i);
            int nivel = atalho.nivel().getAsInt();
            boolean afundado = nivel > 0 || (pressionado == i && sob == i);
            ResourceLocation textura = nivel >= 2 && atalho.total() != null ? atalho.total() : afundado ? atalho.pressionado() : atalho.normal();
            g.blit(textura, x, y, 0, 0, TAMANHO, TAMANHO, TAMANHO, TAMANHO);
            if (sob == i && !afundado) {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                g.setColor(1.0F, 1.0F, 1.0F, BRILHO_HOVER / 100.0F);
                g.blit(atalho.normal(), x, y, 0, 0, TAMANHO, TAMANHO, TAMANHO, TAMANHO);
                g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }
        }
        if (sob >= 0) {
            Font fonte = Minecraft.getInstance().font;
            Component dica = ATALHOS.get(sob).dica().get();
            int x = posX(tela);
            int y = posY(tela, sob);
            g.renderTooltip(fonte, dica, x - 16 - fonte.width(dica) - 4, y + TAMANHO / 2 + 8);
        }
    }

    @SubscribeEvent
    public static void aoClicar(ScreenEvent.MouseButtonPressed.Pre evento) {
        if (evento.getButton() != 0 || !ativo(evento.getScreen()) || !(evento.getScreen() instanceof AbstractContainerScreen<?> tela)) return;
        int indice = indiceSob(tela, evento.getMouseX(), evento.getMouseY());
        if (indice < 0) return;
        pressionado = indice;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        evento.setCanceled(true);
    }

    @SubscribeEvent
    public static void aoSoltar(ScreenEvent.MouseButtonReleased.Pre evento) {
        if (evento.getButton() != 0 || pressionado < 0) return;
        int marcado = pressionado;
        pressionado = -1;
        if (!ativo(evento.getScreen()) || !(evento.getScreen() instanceof AbstractContainerScreen<?> tela)) return;
        if (indiceSob(tela, evento.getMouseX(), evento.getMouseY()) == marcado) {
            ATALHOS.get(marcado).acao().run();
        }
        evento.setCanceled(true);
    }
}
