package net.aelysium.aelysiummod.sistemas.rp.skin.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteRemoverSkin(String tipo, String id) implements CustomPacketPayload {

    public static final Type<PacoteRemoverSkin> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "skin_rp_remover"));

    public static final StreamCodec<FriendlyByteBuf, PacoteRemoverSkin> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.tipo(), 16);
                buf.writeUtf(pkt.id(), 64);
            },
            buf -> new PacoteRemoverSkin(buf.readUtf(16), buf.readUtf(64)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
