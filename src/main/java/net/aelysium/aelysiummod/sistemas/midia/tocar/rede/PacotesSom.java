package net.aelysium.aelysiummod.sistemas.midia.tocar.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class PacotesSom {

    public static final ResourceLocation SOM_INICIAR_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "som_iniciar");
    public static final ResourceLocation SOM_CHUNK_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "som_chunk");
    public static final ResourceLocation SOM_TOCAR_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "som_tocar");
    public static final ResourceLocation SOM_PARAR_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "som_parar");

    public record PacoteIniciarSom(String nome, int totalSize) implements CustomPacketPayload {
        public static final Type<PacoteIniciarSom> TYPE = new Type<>(SOM_INICIAR_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteIniciarSom> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteIniciarSom decode(FriendlyByteBuf buf) {
                return new PacoteIniciarSom(buf.readUtf(), buf.readInt());
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteIniciarSom p) {
                buf.writeUtf(p.nome()); buf.writeInt(p.totalSize());
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteChunkSom(String nome, int chunkIndex, byte[] data) implements CustomPacketPayload {
        public static final Type<PacoteChunkSom> TYPE = new Type<>(SOM_CHUNK_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteChunkSom> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteChunkSom decode(FriendlyByteBuf buf) {
                String nome = buf.readUtf();
                int idx = buf.readInt();
                int len = buf.readInt();
                byte[] data = new byte[len];
                buf.readBytes(data);
                return new PacoteChunkSom(nome, idx, data);
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteChunkSom p) {
                buf.writeUtf(p.nome()); buf.writeInt(p.chunkIndex()); buf.writeInt(p.data().length); buf.writeBytes(p.data());
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteTocarSom(String nome, float volume, boolean loop, float transicaoSegundos) implements CustomPacketPayload {
        public static final Type<PacoteTocarSom> TYPE = new Type<>(SOM_TOCAR_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteTocarSom> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteTocarSom decode(FriendlyByteBuf buf) {
                return new PacoteTocarSom(buf.readUtf(), buf.readFloat(), buf.readBoolean(), buf.readFloat());
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteTocarSom p) {
                buf.writeUtf(p.nome()); buf.writeFloat(p.volume()); buf.writeBoolean(p.loop()); buf.writeFloat(p.transicaoSegundos());
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteParar() implements CustomPacketPayload {
        public static final Type<PacoteParar> TYPE = new Type<>(SOM_PARAR_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteParar> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteParar decode(FriendlyByteBuf buf) { return new PacoteParar(); }
            @Override public void encode(FriendlyByteBuf buf, PacoteParar p) {}
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
