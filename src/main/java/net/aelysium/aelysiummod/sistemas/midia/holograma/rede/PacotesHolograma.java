package net.aelysium.aelysiummod.sistemas.midia.holograma.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PacotesHolograma {

    public record CriarHologramaS2C(
            String nome,
            double x, double y, double z,
            List<String> linhas,
            List<Double> offsets
    ) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<CriarHologramaS2C> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "holo_spawn"));

        public static final StreamCodec<FriendlyByteBuf, CriarHologramaS2C> STREAM_CODEC =
                StreamCodec.of(CriarHologramaS2C::encode, CriarHologramaS2C::decode);

        private static void encode(FriendlyByteBuf buf, CriarHologramaS2C pkt) {
            buf.writeUtf(pkt.nome);
            buf.writeDouble(pkt.x);
            buf.writeDouble(pkt.y);
            buf.writeDouble(pkt.z);
            buf.writeVarInt(pkt.linhas.size());
            for (int i = 0; i < pkt.linhas.size(); i++) {
                buf.writeUtf(pkt.linhas.get(i));
                buf.writeDouble(pkt.offsets.get(i));
            }
        }

        private static CriarHologramaS2C decode(FriendlyByteBuf buf) {
            String nome = buf.readUtf();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            int count = buf.readVarInt();
            List<String> linhas = new ArrayList<>(count);
            List<Double> offsets = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                linhas.add(buf.readUtf());
                offsets.add(buf.readDouble());
            }
            return new CriarHologramaS2C(nome, x, y, z, linhas, offsets);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DestruirHologramaS2C(String nome) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<DestruirHologramaS2C> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "holo_destroy"));

        public static final StreamCodec<FriendlyByteBuf, DestruirHologramaS2C> STREAM_CODEC =
                StreamCodec.of(
                        (buf, pkt) -> buf.writeUtf(pkt.nome),
                        buf -> new DestruirHologramaS2C(buf.readUtf())
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record AtualizarHologramaS2C(
            String nome,
            double x, double y, double z,
            List<String> linhas,
            List<Double> offsets
    ) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<AtualizarHologramaS2C> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "holo_update"));

        public static final StreamCodec<FriendlyByteBuf, AtualizarHologramaS2C> STREAM_CODEC =
                StreamCodec.of(AtualizarHologramaS2C::encode, AtualizarHologramaS2C::decode);

        private static void encode(FriendlyByteBuf buf, AtualizarHologramaS2C pkt) {
            buf.writeUtf(pkt.nome);
            buf.writeDouble(pkt.x);
            buf.writeDouble(pkt.y);
            buf.writeDouble(pkt.z);
            buf.writeVarInt(pkt.linhas.size());
            for (int i = 0; i < pkt.linhas.size(); i++) {
                buf.writeUtf(pkt.linhas.get(i));
                buf.writeDouble(pkt.offsets.get(i));
            }
        }

        private static AtualizarHologramaS2C decode(FriendlyByteBuf buf) {
            String nome = buf.readUtf();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            int count = buf.readVarInt();
            List<String> linhas = new ArrayList<>(count);
            List<Double> offsets = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                linhas.add(buf.readUtf());
                offsets.add(buf.readDouble());
            }
            return new AtualizarHologramaS2C(nome, x, y, z, linhas, offsets);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}