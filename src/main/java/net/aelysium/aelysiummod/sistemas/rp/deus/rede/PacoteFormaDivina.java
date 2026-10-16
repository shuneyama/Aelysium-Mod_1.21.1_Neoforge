package net.aelysium.aelysiummod.sistemas.rp.deus.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record PacoteFormaDivina(UUID uuid, boolean ativa, String deusId, int cor) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "forma_divina");

    public static final Type<PacoteFormaDivina> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteFormaDivina> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUUID(pkt.uuid());
                        buf.writeBoolean(pkt.ativa());
                        buf.writeUtf(pkt.deusId());
                        buf.writeInt(pkt.cor());
                    },
                    buf -> new PacoteFormaDivina(
                            buf.readUUID(),
                            buf.readBoolean(),
                            buf.readUtf(64),
                            buf.readInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}