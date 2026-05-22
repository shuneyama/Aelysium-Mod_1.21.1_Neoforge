package net.aelysium.aelysiummod.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record FormaDivinaPacket(UUID uuid, boolean ativa, String deusId, int cor) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "forma_divina");

    public static final Type<FormaDivinaPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, FormaDivinaPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUUID(pkt.uuid());
                        buf.writeBoolean(pkt.ativa());
                        buf.writeUtf(pkt.deusId());
                        buf.writeInt(pkt.cor());
                    },
                    buf -> new FormaDivinaPacket(
                            buf.readUUID(),
                            buf.readBoolean(),
                            buf.readUtf(64),
                            buf.readInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}