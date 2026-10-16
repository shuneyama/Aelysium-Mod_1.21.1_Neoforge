package net.aelysium.aelysiummod.conteudo.item.carteira;

import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredItem;
import net.aelysium.aelysiummod.registro.ModMenus;

public class CarteiraMenu extends AbstractContainerMenu {

    public static final int SLOTS_AEDA = 7;
    private static final int SLOTS_AEDA_GRADE = 6;
    private static final String TAG_CONTAGENS = "CarteiraContagens";

    @SuppressWarnings("unchecked")
    private static final DeferredItem<Item>[] AEDAS = new DeferredItem[]{
            ModItens.AEDA_COBRE, ModItens.AEDA_PRATA, ModItens.AEDA_OURO,
            ModItens.AEDA_RUBI, ModItens.AEDA_CIANE, ModItens.AEDA_AMETISTA,
            ModItens.VINEDA
    };

    private final CarteiraContainer carteiraContainer;
    private final ItemStack carteiraStack;

    public CarteiraMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ItemStack.EMPTY);
    }

    public CarteiraMenu(int containerId, Inventory playerInventory, ItemStack carteiraStack) {
        super(ModMenus.CARTEIRA_MENU.get(), containerId);
        this.carteiraStack = carteiraStack;
        this.carteiraContainer = criarContainer(carteiraStack);

        for (int i = 0; i < SLOTS_AEDA_GRADE; i++) {
            int coluna = i % 3;
            int linha = i / 3;
            int deslocX = coluna == 0 ? -1 : coluna == 2 ? 1 : 0;
            int deslocY = linha == 0 ? -1 : 0;
            this.addSlot(new CarteiraSlot(carteiraContainer, i, 62 + coluna * 18 + deslocX, 19 + linha * 18 + deslocY, AEDAS[i]));
        }
        this.addSlot(new CarteiraSlot(carteiraContainer, SLOTS_AEDA_GRADE, 134, 28, AEDAS[SLOTS_AEDA_GRADE]));

        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                this.addSlot(new Slot(playerInventory, coluna + linha * 9 + 9, 8 + coluna * 18, 95 + linha * 18));
            }
        }
        for (int coluna = 0; coluna < 9; coluna++) {
            this.addSlot(new Slot(playerInventory, coluna, 8 + coluna * 18, 153));
        }
    }

    private static CarteiraContainer criarContainer(ItemStack carteiraStack) {
        CarteiraContainer container = new CarteiraContainer(SLOTS_AEDA);
        if (carteiraStack.isEmpty()) return container;

        CustomData customData = carteiraStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(TAG_CONTAGENS)) return container;

        int[] contagens = tag.getIntArray(TAG_CONTAGENS);
        for (int i = 0; i < SLOTS_AEDA && i < contagens.length; i++) {
            if (contagens[i] > 0) {
                container.setItem(i, new ItemStack(AEDAS[i].get(), contagens[i]));
            }
        }
        return container;
    }

    private void salvarNoItem() {
        if (carteiraStack.isEmpty()) return;

        int[] contagens = new int[SLOTS_AEDA];
        for (int i = 0; i < SLOTS_AEDA; i++) {
            contagens[i] = carteiraContainer.getItem(i).getCount();
        }

        CompoundTag tag = new CompoundTag();
        tag.putIntArray(TAG_CONTAGENS, contagens);
        carteiraStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        salvarNoItem();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        salvarNoItem();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack resultado = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return resultado;

        ItemStack stackNoSlot = slot.getItem();
        resultado = stackNoSlot.copy();

        if (index < SLOTS_AEDA) {
            if (!this.moveItemStackTo(stackNoSlot, SLOTS_AEDA, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int slotAeda = indiceSlotParaItem(stackNoSlot);
            if (slotAeda >= 0) {
                if (!this.moveItemStackTo(stackNoSlot, slotAeda, slotAeda + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < SLOTS_AEDA + 27) {
                if (!this.moveItemStackTo(stackNoSlot, SLOTS_AEDA + 27, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackNoSlot, SLOTS_AEDA, SLOTS_AEDA + 27, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackNoSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackNoSlot.getCount() == resultado.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackNoSlot);
        return resultado;
    }

    private int indiceSlotParaItem(ItemStack stack) {
        for (int i = 0; i < SLOTS_AEDA; i++) {
            if (stack.is(AEDAS[i].get())) return i;
        }
        return -1;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
