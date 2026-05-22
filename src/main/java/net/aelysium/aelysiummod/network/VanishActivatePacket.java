package net.aelysium.aelysiummod.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VanishActivatePacket() implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "vanish_activate");

    public static final Type<VanishActivatePacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, VanishActivatePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {},
                    buf -> new VanishActivatePacket()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
