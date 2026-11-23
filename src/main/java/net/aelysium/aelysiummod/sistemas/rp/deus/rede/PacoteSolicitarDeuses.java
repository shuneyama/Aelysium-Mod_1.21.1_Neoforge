package net.aelysium.aelysiummod.sistemas.rp.deus.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteSolicitarDeuses() implements CustomPacketPayload {

    public static final Type<PacoteSolicitarDeuses> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "deuses_solicitar"));

    public static final StreamCodec<FriendlyByteBuf, PacoteSolicitarDeuses> STREAM_CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PacoteSolicitarDeuses());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
