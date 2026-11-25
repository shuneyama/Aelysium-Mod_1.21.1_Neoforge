package net.aelysium.aelysiummod.sistemas.admin.banimentos.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAcaoBanimento(String acao, String alvo, String item) implements CustomPacketPayload {

    public static final Type<PacoteAcaoBanimento> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "banimentos_acao"));

    public static final StreamCodec<FriendlyByteBuf, PacoteAcaoBanimento> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.acao(), 24);
                buf.writeUtf(pkt.alvo(), 48);
                buf.writeUtf(pkt.item(), 128);
            },
            buf -> new PacoteAcaoBanimento(buf.readUtf(24), buf.readUtf(48), buf.readUtf(128)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
