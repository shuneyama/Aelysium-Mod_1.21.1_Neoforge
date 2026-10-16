package net.aelysium.aelysiummod.sistemas.social.maozinha.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record PacoteDeslocamentoMaozinha(UUID seguidor, float x, float y, float z) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "maozinha_deslocamento");

    public static final Type<PacoteDeslocamentoMaozinha> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteDeslocamentoMaozinha> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUUID(pkt.seguidor());
                        buf.writeFloat(pkt.x());
                        buf.writeFloat(pkt.y());
                        buf.writeFloat(pkt.z());
                    },
                    buf -> new PacoteDeslocamentoMaozinha(
                            buf.readUUID(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readFloat()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
