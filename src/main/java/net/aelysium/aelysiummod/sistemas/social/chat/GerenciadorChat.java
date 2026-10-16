package net.aelysium.aelysiummod.sistemas.social.chat;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.neoforge.common.NeoForge;
import net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoChat;
import net.aelysium.aelysiummod.sistemas.social.chat.GerenciadorEspiaoSocial;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.nucleo.evento.ChatEnviadoEvento;
import net.aelysium.aelysiummod.sistemas.social.chat.rede.RedeChat;

public class GerenciadorChat {

    private static final Logger LOGGER = LoggerFactory.getLogger(AelysiumMod.MOD_ID);

    public static void enviarMensagemLocal(ServerPlayer remetente, String mensagem) {
        if (PerfilJogador.habilidades().estaMudo(remetente.getUUID())) {
            remetente.sendSystemMessage(Component.translatable("mensagem.aelysiummod.chat.voce_esta_silenciado_e_nao_pode_enviar"));
            return;
        }

        if (!GerenciadorAntiSpam.podeEnviar(remetente.getUUID(), mensagem)) {
            remetente.sendSystemMessage(Component.translatable("mensagem.aelysiummod.chat.aguarde_antes_de_enviar_a_mesma"));
            return;
        }

        List<ServerPlayer> receptores = getJogadoresNoAlcance(remetente, ConfiguracaoChat.getLocalChatRadius());

        MutableComponent msgFormatada = Component.translatable("mensagem.aelysiummod.chat.local_2");
        MutableComponent badge = PerfilJogador.insignia().componente(remetente);
        if (badge != null) msgFormatada.append(badge).append(" ");
        msgFormatada.append(getStyledName(remetente)).append(Component.literal("§r: " + mensagem));

        for (ServerPlayer receptor : receptores) {
            receptor.sendSystemMessage(msgFormatada);
        }

        RedeChat.enviarBalloonParaProximos(remetente, mensagem);

        GerenciadorEspiaoSocial.enviarParaSocialspy(remetente, mensagem, "Local", receptores);

        LOGGER.info("[Chat-Local] {}: {}", remetente.getGameProfile().getName(), mensagem);
        NeoForge.EVENT_BUS.post(new ChatEnviadoEvento(remetente, mensagem, "Local"));
    }

    public static void enviarMensagemGlobal(ServerPlayer remetente, String mensagem) {
        if (PerfilJogador.habilidades().estaMudo(remetente.getUUID())) {
            remetente.sendSystemMessage(Component.translatable("mensagem.aelysiummod.chat.voce_esta_silenciado_e_nao_pode_enviar"));
            return;
        }

        if (!GerenciadorAntiSpam.podeEnviar(remetente.getUUID(), mensagem)) {
            remetente.sendSystemMessage(Component.translatable("mensagem.aelysiummod.chat.aguarde_antes_de_enviar_a_mesma"));
            return;
        }

        List<ServerPlayer> receptores = new ArrayList<>(remetente.getServer().getPlayerList().getPlayers());

        MutableComponent msgFormatada = Component.translatable("mensagem.aelysiummod.chat.global_2");
        MutableComponent badge = PerfilJogador.insignia().componente(remetente);
        if (badge != null) msgFormatada.append(badge).append(" ");
        msgFormatada.append(getStyledName(remetente)).append(Component.literal("§r: " + mensagem));

        for (ServerPlayer receptor : receptores) {
            receptor.sendSystemMessage(msgFormatada);
        }

        GerenciadorEspiaoSocial.enviarParaSocialspy(remetente, mensagem, "Global", receptores);

        LOGGER.info("[Chat-Global] {}: {}", remetente.getGameProfile().getName(), mensagem);
        NeoForge.EVENT_BUS.post(new ChatEnviadoEvento(remetente, mensagem, "Global"));
    }

    private static Component getStyledName(ServerPlayer player) {
        if (player.getServer() != null) {
            MutableComponent styled = PerfilJogador.apelido().nomeCompletoComPronomes(player.getServer(), player.getUUID());
            if (styled != null) return styled;
        }
        return Component.literal(player.getScoreboardName());
    }

    private static String getNomeExibido(ServerPlayer player) {
        if (player.getServer() != null) {
            String apelido = PerfilJogador.apelido().apelido(player.getServer(), player.getUUID());
            if (apelido != null && !apelido.isEmpty()) return apelido;
        }
        return player.getScoreboardName();
    }

    public static List<ServerPlayer> getJogadoresNoAlcance(ServerPlayer remetente, double distancia) {
        List<ServerPlayer> resultado = new ArrayList<>();

        for (ServerPlayer player : remetente.serverLevel().players()) {
            if (player.distanceTo(remetente) <= distancia) {
                resultado.add(player);
            }
        }

        return resultado;
    }
}