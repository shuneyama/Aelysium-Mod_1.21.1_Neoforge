package net.aelysium.aelysiummod.sistemas.admin.vip;

import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.aelysium.aelysiummod.registro.ModMenus;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

public class MenuSacolaVip extends AbstractContainerMenu {

    public static final int LINHAS = 3;
    public static final int SLOTS_SACOLA = LINHAS * 9;

    private final SimpleContainer container;
    private final ItemStack sacola;
    private boolean carregando = true;

    public MenuSacolaVip(int idContainer, Inventory inventarioJogador) {
        this(idContainer, inventarioJogador, ItemStack.EMPTY);
    }

    public MenuSacolaVip(int idContainer, Inventory inventarioJogador, ItemStack sacola) {
        super(ModMenus.SACOLA_VIP.get(), idContainer);
        this.sacola = sacola;
        this.container = new SimpleContainer(SLOTS_SACOLA);

        if (!sacola.isEmpty()) {
            List<ItemStack> itens = sacola.getOrDefault(ModComponentesDados.SACOLA_VIP_CONTEUDO.get(), ItemContainerContents.EMPTY).stream().toList();
            for (int i = 0; i < SLOTS_SACOLA && i < itens.size(); i++) {
                container.setItem(i, itens.get(i).copy());
            }
        }

        for (int linha = 0; linha < LINHAS; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                this.addSlot(new SlotSacolaVip(container, coluna + linha * 9, 8 + coluna * 18, 18 + linha * 18));
            }
        }

        int deslocamento = (LINHAS - 4) * 18;
        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                this.addSlot(criarSlotInventario(inventarioJogador, coluna + linha * 9 + 9, 8 + coluna * 18, 103 + linha * 18 + deslocamento));
            }
        }
        for (int coluna = 0; coluna < 9; coluna++) {
            this.addSlot(criarSlotInventario(inventarioJogador, coluna, 8 + coluna * 18, 161 + deslocamento));
        }

        container.addListener(c -> salvar());
        carregando = false;
    }

    private Slot criarSlotInventario(Inventory inventario, int indice, int x, int y) {
        if (!sacola.isEmpty() && inventario.getItem(indice) == sacola) {
            return new SlotBloqueadoSacolaVip(inventario, indice, x, y);
        }
        return new Slot(inventario, indice, x, y);
    }

    private void salvar() {
        if (carregando || sacola.isEmpty()) return;
        List<ItemStack> itens = new ArrayList<>();
        boolean vazia = true;
        for (int i = 0; i < SLOTS_SACOLA; i++) {
            ItemStack item = container.getItem(i);
            if (!item.isEmpty()) vazia = false;
            itens.add(item.copy());
        }
        if (vazia) {
            sacola.shrink(1);
            return;
        }
        sacola.set(ModComponentesDados.SACOLA_VIP_CONTEUDO.get(), ItemContainerContents.fromItems(itens));
    }

    @Override
    public ItemStack quickMoveStack(Player jogador, int indice) {
        Slot slot = this.slots.get(indice);
        if (slot == null || !slot.hasItem() || indice >= SLOTS_SACOLA) return ItemStack.EMPTY;

        ItemStack stackNoSlot = slot.getItem();
        ItemStack resultado = stackNoSlot.copy();

        if (!this.moveItemStackTo(stackNoSlot, SLOTS_SACOLA, this.slots.size(), true)) {
            return ItemStack.EMPTY;
        }

        if (stackNoSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return resultado;
    }

    @Override
    public boolean stillValid(Player jogador) {
        if (sacola.isEmpty()) return jogador.level().isClientSide();
        Inventory inventario = jogador.getInventory();
        for (int i = 0; i < inventario.getContainerSize(); i++) {
            if (inventario.getItem(i) == sacola) return true;
        }
        return false;
    }
}
