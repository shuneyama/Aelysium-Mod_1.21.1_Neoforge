package net.aelysium.aelysiummod.sistemas.mundo.minifarol;

import net.aelysium.aelysiummod.registro.ModEntidadesBloco;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EntidadeMiniFarol extends BlockEntity {

    public static final int COR_PADRAO = 0xFFFFFF;

    private boolean ativo = true;
    private int cor = COR_PADRAO;

    public EntidadeMiniFarol(BlockPos pos, BlockState state) {
        super(ModEntidadesBloco.MINI_FAROL.get(), pos, state);
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getCor() {
        return cor;
    }

    public void configurar(boolean ativo, int cor) {
        this.ativo = ativo;
        this.cor = cor & 0xFFFFFF;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("ativo", ativo);
        tag.putInt("cor", cor);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ativo = !tag.contains("ativo") || tag.getBoolean("ativo");
        cor = tag.contains("cor") ? tag.getInt("cor") & 0xFFFFFF : COR_PADRAO;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
