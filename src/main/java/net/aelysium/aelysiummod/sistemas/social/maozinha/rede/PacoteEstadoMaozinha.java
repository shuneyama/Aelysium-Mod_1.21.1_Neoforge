package net.aelysium.aelysiummod.sistemas.social.maozinha.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.maozinha.FaseMaozinha;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record PacoteEstadoMaozinha(UUID lider, UUID alvo, FaseMaozinha fase) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "maozinha_estado");

    public static final Type<PacoteEstadoMaozinha> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteEstadoMaozinha> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUUID(pkt.lider());
                        buf.writeUUID(pkt.alvo());
                        buf.writeByte(pkt.fase().ordinal());
                    },
                    buf -> new PacoteEstadoMaozinha(
                            buf.readUUID(),
                            buf.readUUID(),
                            FaseMaozinha.porOrdem(buf.readByte())
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
