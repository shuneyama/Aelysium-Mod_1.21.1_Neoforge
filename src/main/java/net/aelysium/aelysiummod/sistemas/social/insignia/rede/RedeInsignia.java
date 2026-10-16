package net.aelysium.aelysiummod.sistemas.social.insignia.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.jogador.InsigniaPersonalizada;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.sistemas.social.insignia.DadosInsignia;
import net.aelysium.aelysiummod.sistemas.social.insignia.GerenciadorInsigniasPersonalizadas;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class RedeInsignia {

    public static final ResourceLocation CRIAR_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "insignia_criar");
    public static final ResourceLocation EXCLUIR_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "insignia_excluir");
    public static final ResourceLocation SINC_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "insignia_sinc");

    public record PacoteCriarInsignia(String texto, int cor) implements CustomPacketPayload {
        public static final Type<PacoteCriarInsignia> TYPE = new Type<>(CRIAR_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteCriarInsignia> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteCriarInsignia decode(FriendlyByteBuf buf) { return new PacoteCriarInsignia(buf.readUtf(64), buf.readInt()); }
            @Override public void encode(FriendlyByteBuf buf, PacoteCriarInsignia p) { buf.writeUtf(p.texto, 64); buf.writeInt(p.cor); }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteExcluirInsignia(String id) implements CustomPacketPayload {
        public static final Type<PacoteExcluirInsignia> TYPE = new Type<>(EXCLUIR_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteExcluirInsignia> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteExcluirInsignia decode(FriendlyByteBuf buf) { return new PacoteExcluirInsignia(buf.readUtf(128)); }
            @Override public void encode(FriendlyByteBuf buf, PacoteExcluirInsignia p) { buf.writeUtf(p.id, 128); }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteSincInsignias(List<InsigniaPersonalizada> insignias) implements CustomPacketPayload {
        public static final Type<PacoteSincInsignias> TYPE = new Type<>(SINC_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteSincInsignias> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteSincInsignias decode(FriendlyByteBuf buf) {
                int n = buf.readVarInt();
                List<InsigniaPersonalizada> lista = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    lista.add(new InsigniaPersonalizada(buf.readUtf(128), buf.readUtf(64), buf.readInt(), buf.readVarInt()));
                }
                return new PacoteSincInsignias(lista);
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteSincInsignias p) {
                buf.writeVarInt(p.insignias.size());
                for (InsigniaPersonalizada i : p.insignias) {
                    buf.writeUtf(i.id(), 128);
                    buf.writeUtf(i.texto(), 64);
                    buf.writeInt(i.cor());
                    buf.writeVarInt(i.indice());
                }
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void sincronizarPara(ServerPlayer jogador) {
        PacketDistributor.sendToPlayer(jogador, new PacoteSincInsignias(new ArrayList<>(GerenciadorInsigniasPersonalizadas.todas())));
    }

    public static void sincronizarTodos() {
        PacketDistributor.sendToAllPlayers(new PacoteSincInsignias(new ArrayList<>(GerenciadorInsigniasPersonalizadas.todas())));
    }

    public static void handleCriar(PacoteCriarInsignia payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer executor)) return;
            if (!executor.hasPermissions(2)) return;
            InsigniaPersonalizada criada = GerenciadorInsigniasPersonalizadas.criar(executor.getServer(), payload.texto(), payload.cor());
            if (criada == null) {
                executor.sendSystemMessage(Component.translatable("mensagem.aelysiummod.insignia.invalida"));
                return;
            }
            sincronizarTodos();
            executor.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.insignia.criada", criada.texto()));
        });
    }

    public static void handleExcluir(PacoteExcluirInsignia payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer executor)) return;
            if (!executor.hasPermissions(2)) return;
            MinecraftServer servidor = executor.getServer();
            if (!GerenciadorInsigniasPersonalizadas.excluir(servidor, payload.id())) return;
            DadosInsignia.get(servidor).limparPorId(payload.id());
            sincronizarTodos();
            for (ServerPlayer jogador : servidor.getPlayerList().getPlayers()) {
                PerfilJogador.apelido().atualizarListaJogadores(jogador);
            }
            executor.sendSystemMessage(Component.translatable("mensagem.aelysiummod.insignia.excluida"));
        });
    }
}
