package net.aelysium.aelysiummod.sistemas.admin.cabeca.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAbrirCabeca(BlockPos pos, String atual) implements CustomPacketPayload {

    public static final Type<PacoteAbrirCabeca> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "cabeca_abrir"));

    public static final StreamCodec<FriendlyByteBuf, PacoteAbrirCabeca> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeUtf(p.atual, 16);
            },
            buf -> new PacoteAbrirCabeca(buf.readBlockPos(), buf.readUtf(16)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
