package net.aelysium.aelysiummod.sistemas.rp.personagem.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteSolicitarPersonagens() implements CustomPacketPayload {

    public static final Type<PacoteSolicitarPersonagens> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "personagens_solicitar"));

    public static final StreamCodec<FriendlyByteBuf, PacoteSolicitarPersonagens> STREAM_CODEC = StreamCodec.of((buf, pkt) -> {}, buf -> new PacoteSolicitarPersonagens());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
