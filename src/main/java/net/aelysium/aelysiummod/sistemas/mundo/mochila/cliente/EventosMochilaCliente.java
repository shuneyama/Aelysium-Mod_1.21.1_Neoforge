package net.aelysium.aelysiummod.sistemas.mundo.mochila.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.mochila.ConteudoMochila;
import net.aelysium.aelysiummod.sistemas.mundo.mochila.ModMochilas;
import net.aelysium.aelysiummod.sistemas.mundo.mochila.rede.PacoteSelecionarMochila;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class EventosMochilaCliente {

    private static Slot ultimo;
    private static double acumuladoX;
    private static double acumuladoY;

    private static Slot sobMouse(Screen tela) {
        if (!(tela instanceof AbstractContainerScreen<?> recipiente) || tela instanceof CreativeModeInventoryScreen) return null;
        return recipiente.getSlotUnderMouse();
    }

    private static void desselecionar(Slot slot) {
        if (slot == null) return;
        ItemStack pilha = slot.getItem();
        if (!ModMochilas.ehMochila(pilha) || ConteudoMochila.selecionado(pilha) == -1) return;
        pilha.remove(ModMochilas.SELECIONADO.get());
        PacketDistributor.sendToServer(new PacoteSelecionarMochila(slot.index, -1));
    }

    @SubscribeEvent
    public static void rolar(ScreenEvent.MouseScrolled.Pre evento) {
        Slot slot = sobMouse(evento.getScreen());
        if (slot == null) return;
        ItemStack pilha = slot.getItem();
        if (!ModMochilas.ehMochila(pilha)) return;
        int mostrar = ConteudoMochila.quantidadeMostrar(pilha);
        if (mostrar == 0) return;
        evento.setCanceled(true);
        if (acumuladoX != 0.0D && Math.signum(evento.getScrollDeltaX()) != Math.signum(acumuladoX)) acumuladoX = 0.0D;
        if (acumuladoY != 0.0D && Math.signum(evento.getScrollDeltaY()) != Math.signum(acumuladoY)) acumuladoY = 0.0D;
        acumuladoX += evento.getScrollDeltaX();
        acumuladoY += evento.getScrollDeltaY();
        int rodaX = (int) acumuladoX;
        int rodaY = (int) acumuladoY;
        if (rodaX == 0 && rodaY == 0) return;
        acumuladoX -= rodaX;
        acumuladoY -= rodaY;
        int roda = rodaY == 0 ? -rodaX : rodaY;
        int atual = ConteudoMochila.selecionado(pilha);
        int novo = proximo(roda, atual, mostrar);
        if (novo != atual) {
            ConteudoMochila.definirSelecao(pilha, novo);
            PacketDistributor.sendToServer(new PacoteSelecionarMochila(slot.index, novo));
        }
    }

    private static int proximo(int roda, int atual, int limite) {
        atual -= (int) Math.signum(roda);
        atual = Math.max(-1, atual);
        while (atual < 0) atual += limite;
        while (atual >= limite) atual -= limite;
        return atual;
    }

    @SubscribeEvent
    public static void desenhar(ScreenEvent.Render.Pre evento) {
        Slot atual = sobMouse(evento.getScreen());
        if (ultimo != null && ultimo != atual) desselecionar(ultimo);
        ultimo = atual;
    }

    @SubscribeEvent
    public static void fechar(ScreenEvent.Closing evento) {
        desselecionar(ultimo);
        ultimo = null;
    }

    @SubscribeEvent
    public static void clicar(ScreenEvent.MouseButtonPressed.Pre evento) {
        if (Screen.hasShiftDown()) desselecionar(sobMouse(evento.getScreen()));
    }

    @SubscribeEvent
    public static void tecla(ScreenEvent.KeyPressed.Pre evento) {
        desselecionar(sobMouse(evento.getScreen()));
    }
}
