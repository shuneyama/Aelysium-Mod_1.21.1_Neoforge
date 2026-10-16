package net.aelysium.aelysiummod.sistemas.mundo.porta;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface TrancavelPorSenha {
    UUID getId();

    String getSenha();

    void definirSenha(String senha);

    boolean senhaCorreta(String tentativa);

    void onAcessoConcedido(Level level, BlockPos pos, ServerPlayer player);

    String getTipoNome();
}
