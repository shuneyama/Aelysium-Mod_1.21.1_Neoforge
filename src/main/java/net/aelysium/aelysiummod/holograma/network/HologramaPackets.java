package net.aelysium.aelysiummod.holograma.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class HologramaPackets {

    public record SpawnHologramaS2C(
            String nome,
            double x, double y, double z,
            List<String> linhas,
            List<Double> offsets
    ) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<SpawnHologramaS2C> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "holo_spawn"));

        public static final StreamCodec<FriendlyByteBuf, SpawnHologramaS2C> STREAM_CODEC =
                StreamCodec.of(SpawnHologramaS2C::encode, SpawnHologramaS2C::decode);

        private static void encode(FriendlyByteBuf buf, SpawnHologramaS2C pkt) {
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

        private static SpawnHologramaS2C decode(FriendlyByteBuf buf) {
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
            return new SpawnHologramaS2C(nome, x, y, z, linhas, offsets);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DestroyHologramaS2C(String nome) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<DestroyHologramaS2C> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "holo_destroy"));

        public static final StreamCodec<FriendlyByteBuf, DestroyHologramaS2C> STREAM_CODEC =
                StreamCodec.of(
                        (buf, pkt) -> buf.writeUtf(pkt.nome),
                        buf -> new DestroyHologramaS2C(buf.readUtf())
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record UpdateHologramaS2C(
            String nome,
            double x, double y, double z,
            List<String> linhas,
            List<Double> offsets
    ) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<UpdateHologramaS2C> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "holo_update"));

        public static final StreamCodec<FriendlyByteBuf, UpdateHologramaS2C> STREAM_CODEC =
                StreamCodec.of(UpdateHologramaS2C::encode, UpdateHologramaS2C::decode);

        private static void encode(FriendlyByteBuf buf, UpdateHologramaS2C pkt) {
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

        private static UpdateHologramaS2C decode(FriendlyByteBuf buf) {
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
            return new UpdateHologramaS2C(nome, x, y, z, linhas, offsets);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}