package net.aelysium.aelysiummod.conteudo.item.glock.rede;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteRecarregarGlock() implements CustomPacketPayload {

    public static final Type<PacoteRecarregarGlock> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "glock_recarregar"));

    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, PacoteRecarregarGlock> STREAM_CODEC =
            StreamCodec.unit(new PacoteRecarregarGlock());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
