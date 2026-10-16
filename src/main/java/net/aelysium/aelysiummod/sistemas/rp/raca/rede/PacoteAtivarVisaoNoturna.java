package net.aelysium.aelysiummod.sistemas.rp.raca.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAtivarVisaoNoturna() implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "visao_noturna_activate");

    public static final Type<PacoteAtivarVisaoNoturna> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacoteAtivarVisaoNoturna> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {},
                    buf -> new PacoteAtivarVisaoNoturna()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
