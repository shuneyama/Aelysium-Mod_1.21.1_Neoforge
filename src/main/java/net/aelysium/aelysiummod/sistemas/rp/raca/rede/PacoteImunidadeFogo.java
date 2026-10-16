package net.aelysium.aelysiummod.sistemas.rp.raca.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteImunidadeFogo(boolean ativa) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "imunidade_fogo");

    public static final Type<PacoteImunidadeFogo> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteImunidadeFogo> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeBoolean(pkt.ativa()),
                    buf -> new PacoteImunidadeFogo(buf.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
