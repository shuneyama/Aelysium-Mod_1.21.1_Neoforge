package net.aelysium.aelysiummod.sistemas.admin.protecao.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAbrirTela(
        BlockPos posicaoMinima,
        BlockPos posicaoMaxima
) implements CustomPacketPayload {

    public static final Type<PacoteAbrirTela> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "protecao_abrir_tela"));

    public static final StreamCodec<FriendlyByteBuf, PacoteAbrirTela> CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeBlockPos(p.posicaoMinima); buf.writeBlockPos(p.posicaoMaxima); },
                    buf -> new PacoteAbrirTela(buf.readBlockPos(), buf.readBlockPos())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
