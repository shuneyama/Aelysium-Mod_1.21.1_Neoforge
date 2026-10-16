package net.aelysium.aelysiummod.sistemas.mundo.porta.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteResultadoSenha(
        BlockPos pos,
        boolean sucesso
) implements CustomPacketPayload {

    public static final Type<PacoteResultadoSenha> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "porta_senha_resultado"));

    public static final StreamCodec<FriendlyByteBuf, PacoteResultadoSenha> CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeBlockPos(p.pos); buf.writeBoolean(p.sucesso); },
                    buf -> new PacoteResultadoSenha(buf.readBlockPos(), buf.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
