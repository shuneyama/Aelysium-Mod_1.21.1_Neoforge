package net.aelysium.aelysiummod.sistemas.social.apelido.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.apelido.GerenciadorPredefinicoesApelido;
import net.aelysium.aelysiummod.sistemas.social.apelido.PredefinicaoApelido;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class RedePredefinicoesApelido {

    public static final ResourceLocation SALVAR_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_predef_salvar");
    public static final ResourceLocation EXCLUIR_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_predef_excluir");
    public static final ResourceLocation LISTAR_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_predef_listar");
    public static final ResourceLocation LISTA_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_predef_lista");

    private static void escrever(FriendlyByteBuf buf, PredefinicaoApelido p) {
        buf.writeUtf(p.nome(), 64);
        buf.writeUtf(p.prefixo(), 64); buf.writeInt(p.prefixoCor1()); buf.writeInt(p.prefixoCor2()); buf.writeInt(p.prefixoFormato());
        buf.writeUtf(p.nick(), 64); buf.writeInt(p.nickCor()); buf.writeInt(p.nickFormato());
        buf.writeUtf(p.sufixo(), 64); buf.writeInt(p.sufixoCor1()); buf.writeInt(p.sufixoCor2()); buf.writeInt(p.sufixoFormato());
        buf.writeUtf(p.pronomes(), 32); buf.writeUtf(p.badge(), 128);
    }

    private static PredefinicaoApelido ler(FriendlyByteBuf buf) {
        return new PredefinicaoApelido(
                buf.readUtf(64),
                buf.readUtf(64), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readUtf(64), buf.readInt(), buf.readInt(),
                buf.readUtf(64), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readUtf(32), buf.readUtf(128));
    }

    public record PacoteSalvarPredefinicao(PredefinicaoApelido predefinicao) implements CustomPacketPayload {
        public static final Type<PacoteSalvarPredefinicao> TYPE = new Type<>(SALVAR_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteSalvarPredefinicao> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteSalvarPredefinicao decode(FriendlyByteBuf buf) { return new PacoteSalvarPredefinicao(ler(buf)); }
            @Override public void encode(FriendlyByteBuf buf, PacoteSalvarPredefinicao p) { escrever(buf, p.predefinicao); }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteExcluirPredefinicao(String nome) implements CustomPacketPayload {
        public static final Type<PacoteExcluirPredefinicao> TYPE = new Type<>(EXCLUIR_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteExcluirPredefinicao> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteExcluirPredefinicao decode(FriendlyByteBuf buf) { return new PacoteExcluirPredefinicao(buf.readUtf(64)); }
            @Override public void encode(FriendlyByteBuf buf, PacoteExcluirPredefinicao p) { buf.writeUtf(p.nome, 64); }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteListarPredefinicoes() implements CustomPacketPayload {
        public static final Type<PacoteListarPredefinicoes> TYPE = new Type<>(LISTAR_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteListarPredefinicoes> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteListarPredefinicoes decode(FriendlyByteBuf buf) { return new PacoteListarPredefinicoes(); }
            @Override public void encode(FriendlyByteBuf buf, PacoteListarPredefinicoes p) { }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteListaPredefinicoes(List<PredefinicaoApelido> predefinicoes) implements CustomPacketPayload {
        public static final Type<PacoteListaPredefinicoes> TYPE = new Type<>(LISTA_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteListaPredefinicoes> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteListaPredefinicoes decode(FriendlyByteBuf buf) {
                int n = buf.readVarInt();
                List<PredefinicaoApelido> lista = new ArrayList<>();
                for (int i = 0; i < n; i++) lista.add(ler(buf));
                return new PacoteListaPredefinicoes(lista);
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteListaPredefinicoes p) {
                buf.writeVarInt(p.predefinicoes.size());
                for (PredefinicaoApelido pr : p.predefinicoes) escrever(buf, pr);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private static void enviarLista(ServerPlayer jogador) {
        PacketDistributor.sendToPlayer(jogador, new PacoteListaPredefinicoes(GerenciadorPredefinicoesApelido.listar(jogador.getServer())));
    }

    public static void handleSalvar(PacoteSalvarPredefinicao payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer executor)) return;
            if (!executor.hasPermissions(2)) return;
            String pronomes = net.aelysium.aelysiummod.sistemas.social.apelido.DadosApelido.normalizarPronomes(payload.predefinicao().pronomes());
            if (pronomes == null) return;
            if (!GerenciadorPredefinicoesApelido.salvar(executor.getServer(), payload.predefinicao())) {
                executor.sendSystemMessage(Component.translatable("mensagem.aelysiummod.apelido.predefinicao_invalida"));
                return;
            }
            executor.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.apelido.predefinicao_salva",
                    GerenciadorPredefinicoesApelido.normalizarNome(payload.predefinicao().nome())));
        });
    }

    public static void handleExcluir(PacoteExcluirPredefinicao payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer executor)) return;
            if (!executor.hasPermissions(2)) return;
            GerenciadorPredefinicoesApelido.excluir(executor.getServer(), payload.nome());
            enviarLista(executor);
        });
    }

    public static void handleListar(PacoteListarPredefinicoes payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer executor)) return;
            if (!executor.hasPermissions(2)) return;
            enviarLista(executor);
        });
    }
}
