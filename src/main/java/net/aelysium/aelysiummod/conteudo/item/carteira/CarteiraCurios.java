package net.aelysium.aelysiummod.conteudo.item.carteira;

import net.aelysium.aelysiummod.sistemas.admin.banimentos.CompatibilidadeCurios;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class CarteiraCurios {

    public static ItemStack encontrar(ServerPlayer jogador) {
        if (CompatibilidadeCurios.isCuriosLoaded()) {
            ItemStack equipada = procurarEquipada(jogador);
            if (!equipada.isEmpty()) return equipada;
        }

        Inventory inventario = jogador.getInventory();
        for (int i = 0; i < inventario.getContainerSize(); i++) {
            ItemStack stack = inventario.getItem(i);
            if (stack.getItem() instanceof CarteiraItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack procurarEquipada(ServerPlayer jogador) {
        return CuriosApi.getCuriosInventory(jogador)
                .flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof CarteiraItem))
                .map(resultado -> resultado.stack())
                .orElse(ItemStack.EMPTY);
    }
}
