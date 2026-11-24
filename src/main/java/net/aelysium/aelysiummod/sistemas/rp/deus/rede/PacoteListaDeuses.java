package net.aelysium.aelysiummod.sistemas.rp.deus.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record PacoteListaDeuses(List<Entrada> deuses, String atual) implements CustomPacketPayload {

    public record Entrada(String id, String nome, int cor, boolean personalizado, String badge, int corParticula, String pronomes, int poderes) {
    }

    public static final Type<PacoteListaDeuses> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "deuses_lista"));

    public static final StreamCodec<FriendlyByteBuf, PacoteListaDeuses> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeVarInt(pkt.deuses().size());
                for (Entrada entrada : pkt.deuses()) {
                    buf.writeUtf(entrada.id(), 64);
                    buf.writeUtf(entrada.nome(), 64);
                    buf.writeInt(entrada.cor());
                    buf.writeBoolean(entrada.personalizado());
                    buf.writeUtf(entrada.badge(), 64);
                    buf.writeInt(entrada.corParticula());
                    buf.writeUtf(entrada.pronomes(), 64);
                    buf.writeInt(entrada.poderes());
                }
                buf.writeUtf(pkt.atual(), 64);
            },
            buf -> {
                int total = buf.readVarInt();
                List<Entrada> entradas = new ArrayList<>(total);
                for (int i = 0; i < total; i++) {
                    entradas.add(new Entrada(buf.readUtf(64), buf.readUtf(64), buf.readInt(), buf.readBoolean(), buf.readUtf(64), buf.readInt(), buf.readUtf(64), buf.readInt()));
                }
                return new PacoteListaDeuses(entradas, buf.readUtf(64));
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
