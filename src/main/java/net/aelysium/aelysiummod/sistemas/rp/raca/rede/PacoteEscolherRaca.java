package net.aelysium.aelysiummod.sistemas.rp.raca.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.rp.raca.SelecaoRaca;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteEscolherRaca(String raca) implements CustomPacketPayload {

    public static final Type<PacoteEscolherRaca> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "selecao_raca_escolher"));

    public static final StreamCodec<FriendlyByteBuf, PacoteEscolherRaca> CODEC = StreamCodec.of(
            (buf, p) -> buf.writeUtf(p.raca, 32),
            buf -> new PacoteEscolherRaca(buf.readUtf(32)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteEscolherRaca pacote, IPayloadContext contexto) {
        contexto.enqueueWork(() -> {
            if (contexto.player() instanceof ServerPlayer jogador) {
                SelecaoRaca.escolher(jogador, pacote.raca());
            }
        });
    }
}
