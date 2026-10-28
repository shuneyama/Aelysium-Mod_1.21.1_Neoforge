package net.aelysium.aelysiummod.sistemas.social.maozinha.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteVooMaozinha(boolean voando) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "maozinha_voo");

    public static final Type<PacoteVooMaozinha> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteVooMaozinha> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeBoolean(pkt.voando()),
                    buf -> new PacoteVooMaozinha(buf.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
