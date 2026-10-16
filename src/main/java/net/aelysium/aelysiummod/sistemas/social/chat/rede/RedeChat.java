package net.aelysium.aelysiummod.sistemas.social.chat.rede;

import net.aelysium.aelysiummod.sistemas.social.chat.ConfiguracaoBalao;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.chat.EstiloBalao;
import net.aelysium.aelysiummod.sistemas.social.chat.DadosChatJogador;
import net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoChat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.UUID;

public class RedeChat {

    public static final ResourceLocation BALLOON_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "balloon");
    public static final ResourceLocation BALLOON_CONFIG_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "balloon_config");
    public static final ResourceLocation OPEN_BALLOON_GUI_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "open_balloon_gui");

    public static void handleBalloonConfig(PacoteConfiguracaoBalao payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                DadosChatJogador data = DadosChatJogador.get(player.getUUID());
                data.corTexto = payload.corTexto;
                data.corFundo = payload.corFundo;
                data.corBorda = payload.corBorda;
                data.estilo = payload.estilo;
                if (Float.isFinite(payload.altura)) {
                    data.altura = Math.max(ConfiguracaoBalao.ALTURA_MINIMA_BLOCOS, Math.min(ConfiguracaoBalao.ALTURA_MAXIMA_BLOCOS, payload.altura));
                }
                DadosChatJogador.save(player.getUUID());
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("mensagem.aelysiummod.chat.configuracoes_do_balao_salvas"));
            }
        });
    }

    public static void enviarBalloonParaProximos(ServerPlayer remetente, String mensagem) {
        DadosChatJogador data = DadosChatJogador.get(remetente.getUUID());
        PacoteBalao packet = new PacoteBalao(remetente.getUUID(), mensagem, data.corTexto, data.corFundo, data.corBorda, data.altura, data.estilo);
        for (ServerPlayer player : remetente.serverLevel().players()) {
            if (player.distanceTo(remetente) <= ConfiguracaoChat.getLocalChatRadius()) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    public static void enviarAbrirBalloonGui(ServerPlayer player) {
        DadosChatJogador data = DadosChatJogador.get(player.getUUID());
        PacketDistributor.sendToPlayer(player, new PacoteAbrirTelaBalao(data.corTexto, data.corFundo, data.corBorda, data.altura, data.estilo));
    }

    public static void enviarBalloonConfigParaServidor(int corTexto, int corFundo, int corBorda, float altura, EstiloBalao estilo) {
        PacketDistributor.sendToServer(new PacoteConfiguracaoBalao(corTexto, corFundo, corBorda, altura, estilo));
    }

    public record PacoteBalao(UUID playerUUID, String message, int corTexto, int corFundo, int corBorda, float altura, EstiloBalao estilo) implements CustomPacketPayload {
        public static final Type<PacoteBalao> TYPE = new Type<>(BALLOON_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteBalao> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteBalao decode(FriendlyByteBuf buf) {
                return new PacoteBalao(UUID.fromString(buf.readUtf()), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(), EstiloBalao.valueOf(buf.readUtf()));
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteBalao p) {
                buf.writeUtf(p.playerUUID.toString()); buf.writeUtf(p.message); buf.writeInt(p.corTexto); buf.writeInt(p.corFundo); buf.writeInt(p.corBorda); buf.writeFloat(p.altura); buf.writeUtf(p.estilo.name());
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteAbrirTelaBalao(int corTexto, int corFundo, int corBorda, float altura, EstiloBalao estilo) implements CustomPacketPayload {
        public static final Type<PacoteAbrirTelaBalao> TYPE = new Type<>(OPEN_BALLOON_GUI_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteAbrirTelaBalao> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteAbrirTelaBalao decode(FriendlyByteBuf buf) {
                return new PacoteAbrirTelaBalao(buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(), EstiloBalao.valueOf(buf.readUtf()));
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteAbrirTelaBalao p) {
                buf.writeInt(p.corTexto); buf.writeInt(p.corFundo); buf.writeInt(p.corBorda); buf.writeFloat(p.altura); buf.writeUtf(p.estilo.name());
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteConfiguracaoBalao(int corTexto, int corFundo, int corBorda, float altura, EstiloBalao estilo) implements CustomPacketPayload {
        public static final Type<PacoteConfiguracaoBalao> TYPE = new Type<>(BALLOON_CONFIG_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteConfiguracaoBalao> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteConfiguracaoBalao decode(FriendlyByteBuf buf) {
                return new PacoteConfiguracaoBalao(buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(), EstiloBalao.valueOf(buf.readUtf()));
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteConfiguracaoBalao p) {
                buf.writeInt(p.corTexto); buf.writeInt(p.corFundo); buf.writeInt(p.corBorda); buf.writeFloat(p.altura); buf.writeUtf(p.estilo.name());
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
