package net.aelysium.aelysiummod.sistemas.mundo.porta;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;
import net.aelysium.aelysiummod.registro.ModEntidadesBloco;

public class EntidadeBlocoPortaSenha extends BlockEntity implements TrancavelPorSenha {
    private String senha = "";
    private int ticksParaFechar = 0;
    private UUID id = UUID.randomUUID();

    public EntidadeBlocoPortaSenha(BlockPos pos, BlockState state) {
        super(ModEntidadesBloco.PORTA_SENHA.get(), pos, state);
    }

    public UUID getId() {
        return id;
    }

    public String getSenha() {
        return senha;
    }

    public void definirSenha(String senha) {
        this.senha = senha;
        setChanged();
    }

    public boolean senhaCorreta(String tentativa) {
        return !senha.isEmpty() && senha.equals(tentativa);
    }

    public void iniciarFechamento(int ticks) {
        this.ticksParaFechar = ticks;
    }

    @Override
    public void onAcessoConcedido(Level level, BlockPos pos, ServerPlayer player) {
        if (level.getBlockState(pos).getBlock() instanceof BlocoPortaSenha porta) {
            porta.abrirPorta(level, pos, level.getBlockState(pos));
        }
    }

    @Override
    public String getTipoNome() {
        return "porta";
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Senha", senha);
        tag.putUUID("Id", id);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        senha = tag.getString("Senha");
        if (tag.hasUUID("Id")) id = tag.getUUID("Id");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EntidadeBlocoPortaSenha be) {
        if (be.ticksParaFechar <= 0) return;
        be.ticksParaFechar--;
        if (be.ticksParaFechar == 0 && state.getBlock() instanceof BlocoPortaSenha porta) {
            porta.fecharPorta(level, pos, state);
        }
    }
}
