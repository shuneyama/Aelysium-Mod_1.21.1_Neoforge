package net.aelysium.aelysiummod.sistemas.mundo.minifarol.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAbrirMiniFarol(BlockPos pos, boolean ativo, int cor) implements CustomPacketPayload {

    public static final Type<PacoteAbrirMiniFarol> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "mini_farol_abrir"));

    public static final StreamCodec<FriendlyByteBuf, PacoteAbrirMiniFarol> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeBoolean(p.ativo);
                buf.writeInt(p.cor);
            },
            buf -> new PacoteAbrirMiniFarol(buf.readBlockPos(), buf.readBoolean(), buf.readInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
