package net.aelysium.aelysiummod.sistemas.mundo.porta.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.porta.TrancavelPorSenha;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteEnviarSenha(
        BlockPos pos,
        String senha
) implements CustomPacketPayload {

    public static final Type<PacoteEnviarSenha> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "porta_senha_enviar"));

    public static final StreamCodec<FriendlyByteBuf, PacoteEnviarSenha> CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeBlockPos(p.pos); buf.writeUtf(p.senha, 6); },
                    buf -> new PacoteEnviarSenha(buf.readBlockPos(), buf.readUtf(6))
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteEnviarSenha packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Level level = player.level();
            if (!(level.getBlockEntity(packet.pos()) instanceof TrancavelPorSenha be)) return;

            String senha = packet.senha();
            if (senha.isEmpty() || senha.length() > 6 || !senha.chars().allMatch(Character::isDigit)) return;

            boolean sucesso;
            if (be.getSenha().isEmpty()) {
                be.definirSenha(senha);
                sucesso = true;
            } else {
                sucesso = be.senhaCorreta(senha);
                if (sucesso) be.onAcessoConcedido(level, packet.pos(), player);
            }

            PacketDistributor.sendToPlayer(player, new PacoteResultadoSenha(packet.pos(), sucesso));
        });
    }
}
