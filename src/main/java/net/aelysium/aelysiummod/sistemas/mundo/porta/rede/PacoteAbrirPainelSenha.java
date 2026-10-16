package net.aelysium.aelysiummod.sistemas.mundo.porta.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAbrirPainelSenha(
        BlockPos pos,
        boolean definindo
) implements CustomPacketPayload {

    public static final Type<PacoteAbrirPainelSenha> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "porta_senha_abrir_painel"));

    public static final StreamCodec<FriendlyByteBuf, PacoteAbrirPainelSenha> CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeBlockPos(p.pos); buf.writeBoolean(p.definindo); },
                    buf -> new PacoteAbrirPainelSenha(buf.readBlockPos(), buf.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
