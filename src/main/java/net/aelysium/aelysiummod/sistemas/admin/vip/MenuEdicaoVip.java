package net.aelysium.aelysiummod.sistemas.admin.vip;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MenuEdicaoVip extends ChestMenu {

    private final String nomeVip;
    private final SimpleContainer container;

    public MenuEdicaoVip(int idContainer, Inventory inventarioJogador, SimpleContainer container, String nomeVip) {
        super(MenuType.GENERIC_9x3, idContainer, inventarioJogador, container, 3);
        this.container = container;
        this.nomeVip = nomeVip;
    }

    @Override
    public void removed(Player jogador) {
        super.removed(jogador);
        if (!(jogador instanceof ServerPlayer servidorJogador)) return;
        List<ItemStack> itens = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            itens.add(container.getItem(i).copy());
        }
        GerenciadorVips.salvar(servidorJogador.server, nomeVip, itens);
    }
}
