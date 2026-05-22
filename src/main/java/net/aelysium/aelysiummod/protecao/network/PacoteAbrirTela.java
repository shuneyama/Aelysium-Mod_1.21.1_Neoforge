package net.aelysium.aelysiummod.protecao.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

    public static void handle(PacoteAbrirTela packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> h = Class.forName("net.aelysium.aelysiummod.protecao.network.ProtecaoClientHandlers");
                h.getMethod("abrirTelaCriacao", BlockPos.class, BlockPos.class)
                        .invoke(null, packet.posicaoMinima, packet.posicaoMaxima);
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
