package net.aelysium.aelysiummod.sistemas.rp.skin.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.rp.skin.GerenciadorSkins;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteEnviarSkin(String tipo, String id, int indice, int total, String modelo, byte[] parte) implements CustomPacketPayload {

    public static final Type<PacoteEnviarSkin> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "skin_rp_enviar"));

    public static final StreamCodec<FriendlyByteBuf, PacoteEnviarSkin> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.tipo(), 16);
                buf.writeUtf(pkt.id(), 64);
                buf.writeVarInt(pkt.indice());
                buf.writeVarInt(pkt.total());
                buf.writeUtf(pkt.modelo(), 8);
                buf.writeByteArray(pkt.parte());
            },
            buf -> new PacoteEnviarSkin(buf.readUtf(16), buf.readUtf(64), buf.readVarInt(), buf.readVarInt(), buf.readUtf(8),
                    buf.readByteArray(GerenciadorSkins.TAMANHO_PARTE)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
