package net.aelysium.aelysiummod.sistemas.social.maozinha.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacotePuloMaozinha() implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "maozinha_pulo");

    public static final Type<PacotePuloMaozinha> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacotePuloMaozinha> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {},
                    buf -> new PacotePuloMaozinha()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
