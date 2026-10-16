package net.aelysium.aelysiummod.sistemas.mundo.porta.cofre;

import net.aelysium.aelysiummod.sistemas.mundo.porta.TrancavelPorSenha;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;
import net.aelysium.aelysiummod.registro.ModEntidadesBlocoCofre;

public class EntidadeBlocoCofreComPainel extends BlockEntity implements Container, MenuProvider, TrancavelPorSenha {

    private static final int TAMANHO = 27;

    private NonNullList<ItemStack> itens = NonNullList.withSize(TAMANHO, ItemStack.EMPTY);
    private String senha = "";
    private UUID id = UUID.randomUUID();

    public EntidadeBlocoCofreComPainel(BlockPos pos, BlockState state) {
        super(ModEntidadesBlocoCofre.COFRE_COM_PAINEL.get(), pos, state);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getSenha() {
        return senha;
    }

    @Override
    public void definirSenha(String senha) {
        this.senha = senha;
        setChanged();
    }

    @Override
    public boolean senhaCorreta(String tentativa) {
        return !senha.isEmpty() && senha.equals(tentativa);
    }

    @Override
    public void onAcessoConcedido(Level level, BlockPos pos, ServerPlayer player) {
        player.openMenu(this);
    }

    @Override
    public String getTipoNome() {
        return "cofre";
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Senha", senha);
        tag.putUUID("Id", id);
        ContainerHelper.saveAllItems(tag, itens, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        senha = tag.getString("Senha");
        if (tag.hasUUID("Id")) id = tag.getUUID("Id");
        itens = NonNullList.withSize(TAMANHO, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, itens, registries);
    }

    @Override
    public int getContainerSize() {
        return TAMANHO;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : itens) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return itens.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack resultado = ContainerHelper.removeItem(itens, slot, amount);
        if (!resultado.isEmpty()) setChanged();
        return resultado;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(itens, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itens.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        itens.clear();
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.aelysiummod.cofre_com_painel");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }
}
