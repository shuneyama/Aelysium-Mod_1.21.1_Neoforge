package net.aelysium.aelysiummod.sistemas.rp.morte.rede;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteImpactoMorte(boolean habilitando) implements CustomPacketPayload {

    public static final Type<PacoteImpactoMorte> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "morte_impacto"));

    public static final StreamCodec<FriendlyByteBuf, PacoteImpactoMorte> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PacoteImpactoMorte decode(FriendlyByteBuf buf) {
            return new PacoteImpactoMorte(buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, PacoteImpactoMorte p) {
            buf.writeBoolean(p.habilitando());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
