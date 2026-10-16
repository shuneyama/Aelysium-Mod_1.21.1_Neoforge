package net.aelysium.aelysiummod.conteudo.item.carteira.rede;

import net.aelysium.aelysiummod.conteudo.item.carteira.CarteiraCurios;
import net.aelysium.aelysiummod.conteudo.item.carteira.CarteiraItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeCarteira {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToServer(PacoteAbrirCarteira.TYPE, PacoteAbrirCarteira.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx.player() instanceof ServerPlayer player)) return;
                    ItemStack stack = CarteiraCurios.encontrar(player);
                    if (!stack.isEmpty()) CarteiraItem.abrir(player, stack);
                }));
    }
}
