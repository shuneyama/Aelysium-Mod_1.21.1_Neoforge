package net.aelysium.aelysiummod.conteudo.item.glock.rede;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteRecuoGlock() implements CustomPacketPayload {

    public static final Type<PacoteRecuoGlock> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "glock_recuo"));

    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, PacoteRecuoGlock> STREAM_CODEC =
            StreamCodec.unit(new PacoteRecuoGlock());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
