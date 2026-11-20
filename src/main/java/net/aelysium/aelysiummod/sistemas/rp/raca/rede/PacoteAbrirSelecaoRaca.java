package net.aelysium.aelysiummod.sistemas.rp.raca.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAbrirSelecaoRaca(String atual) implements CustomPacketPayload {

    public static final Type<PacoteAbrirSelecaoRaca> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "selecao_raca_abrir"));

    public static final StreamCodec<FriendlyByteBuf, PacoteAbrirSelecaoRaca> CODEC = StreamCodec.of(
            (buf, p) -> buf.writeUtf(p.atual, 32),
            buf -> new PacoteAbrirSelecaoRaca(buf.readUtf(32)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
