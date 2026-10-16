package net.aelysium.aelysiummod.sistemas.rp.deus.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAtivarFormaDivina() implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "forma_divina_activate");

    public static final Type<PacoteAtivarFormaDivina> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteAtivarFormaDivina> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {},
                    buf -> new PacoteAtivarFormaDivina()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}