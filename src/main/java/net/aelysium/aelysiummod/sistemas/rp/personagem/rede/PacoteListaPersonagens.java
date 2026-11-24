package net.aelysium.aelysiummod.sistemas.rp.personagem.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.aelysium.aelysiummod.sistemas.rp.personagem.Personagem;

import java.util.ArrayList;
import java.util.List;

public record PacoteListaPersonagens(List<Personagem> personagens, String atual) implements CustomPacketPayload {

    public static final Type<PacoteListaPersonagens> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "personagens_lista"));

    public static final StreamCodec<FriendlyByteBuf, PacoteListaPersonagens> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeVarInt(pkt.personagens().size());
                for (Personagem p : pkt.personagens()) p.escrever(buf);
                buf.writeUtf(pkt.atual(), 64);
            },
            buf -> {
                int total = buf.readVarInt();
                List<Personagem> lista = new ArrayList<>(total);
                for (int i = 0; i < total; i++) lista.add(Personagem.ler(buf));
                return new PacoteListaPersonagens(lista, buf.readUtf(64));
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
