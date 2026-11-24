package net.aelysium.aelysiummod.sistemas.admin.atalhos.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteEstadoEspiao(int nivel) implements CustomPacketPayload {

    public static final Type<PacoteEstadoEspiao> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "espiao_estado"));

    public static final StreamCodec<FriendlyByteBuf, PacoteEstadoEspiao> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeVarInt(pkt.nivel()),
            buf -> new PacoteEstadoEspiao(buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
