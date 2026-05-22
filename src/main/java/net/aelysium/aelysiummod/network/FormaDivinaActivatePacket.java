package net.aelysium.aelysiummod.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FormaDivinaActivatePacket() implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "forma_divina_activate");

    public static final Type<FormaDivinaActivatePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, FormaDivinaActivatePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {},
                    buf -> new FormaDivinaActivatePacket()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}