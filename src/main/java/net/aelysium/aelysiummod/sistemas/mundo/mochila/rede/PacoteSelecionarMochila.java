package net.aelysium.aelysiummod.sistemas.mundo.mochila.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteSelecionarMochila(int slot, int indice) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "mochila_selecionar");

    public static final Type<PacoteSelecionarMochila> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteSelecionarMochila> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeVarInt(pkt.slot());
                        buf.writeVarInt(pkt.indice() + 1);
                    },
                    buf -> new PacoteSelecionarMochila(buf.readVarInt(), buf.readVarInt() - 1)
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
