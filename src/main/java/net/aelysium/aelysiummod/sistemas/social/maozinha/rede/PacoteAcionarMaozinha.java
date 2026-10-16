package net.aelysium.aelysiummod.sistemas.social.maozinha.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAcionarMaozinha(int alvoId) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "maozinha_acionar");

    public static final Type<PacoteAcionarMaozinha> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteAcionarMaozinha> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeVarInt(pkt.alvoId() + 1),
                    buf -> new PacoteAcionarMaozinha(buf.readVarInt() - 1)
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
