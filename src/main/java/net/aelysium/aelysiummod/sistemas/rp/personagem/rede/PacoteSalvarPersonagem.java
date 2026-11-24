package net.aelysium.aelysiummod.sistemas.rp.personagem.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.aelysium.aelysiummod.sistemas.rp.personagem.Personagem;

public record PacoteSalvarPersonagem(Personagem personagem) implements CustomPacketPayload {

    public static final Type<PacoteSalvarPersonagem> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "personagens_salvar"));

    public static final StreamCodec<FriendlyByteBuf, PacoteSalvarPersonagem> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> pkt.personagem().escrever(buf),
            buf -> new PacoteSalvarPersonagem(Personagem.ler(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
