package net.aelysium.aelysiummod.sistemas.mundo.mochila.rede;

import net.aelysium.aelysiummod.sistemas.mundo.mochila.ConteudoMochila;
import net.aelysium.aelysiummod.sistemas.mundo.mochila.ModMochilas;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeMochila {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToServer(PacoteSelecionarMochila.TYPE, PacoteSelecionarMochila.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx.player() instanceof ServerPlayer jogador)) return;
                    var menu = jogador.containerMenu;
                    if (pkt.slot() < 0 || pkt.slot() >= menu.slots.size()) return;
                    Slot slot = menu.getSlot(pkt.slot());
                    ItemStack pilha = slot.getItem();
                    if (!ModMochilas.ehMochila(pilha)) return;
                    if (pkt.indice() == -1) {
                        pilha.remove(ModMochilas.SELECIONADO.get());
                    } else if (pkt.indice() < ConteudoMochila.quantidadeMostrar(pilha)) {
                        ConteudoMochila.Editor editor = new ConteudoMochila.Editor(pilha);
                        editor.definir(pkt.indice());
                        editor.aplicar(pilha);
                    }
                    menu.broadcastChanges();
                }));
    }
}
