package net.aelysium.aelysiummod.conteudo.filhotes.clima;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteClimaAnimal(int entidade, byte clima) implements CustomPacketPayload {

    public static final Type<PacoteClimaAnimal> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "clima_animal"));

    public static final StreamCodec<FriendlyByteBuf, PacoteClimaAnimal> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PacoteClimaAnimal decode(FriendlyByteBuf buf) {
            return new PacoteClimaAnimal(buf.readVarInt(), buf.readByte());
        }

        @Override
        public void encode(FriendlyByteBuf buf, PacoteClimaAnimal pacote) {
            buf.writeVarInt(pacote.entidade());
            buf.writeByte(pacote.clima());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
