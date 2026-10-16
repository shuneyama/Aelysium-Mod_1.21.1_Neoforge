package net.aelysium.aelysiummod.conteudo.item.carteira.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAbrirCarteira() implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "carteira_abrir");

    public static final Type<PacoteAbrirCarteira> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteAbrirCarteira> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {},
                    buf -> new PacoteAbrirCarteira()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
