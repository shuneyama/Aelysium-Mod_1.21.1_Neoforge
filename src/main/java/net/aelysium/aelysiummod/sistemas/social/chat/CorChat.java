package net.aelysium.aelysiummod.sistemas.social.chat;

import java.util.List;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
import net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoChat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.sistemas.social.chat.rede.RedeChat;

@EventBusSubscriber(modid = "aelysiummod")
public class CorChat {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerChat(ServerChatEvent event) {
        if (!AelysiumGuarda.isAutorizado()) return;

        ServerPlayer player = event.getPlayer();

        if (PerfilJogador.habilidades().estaMudo(player.getUUID())) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.translatable("mensagem.aelysiummod.chat.voce_esta_silenciado_e_nao_pode_enviar"));
            return;
        }

        PlayerTeam team = player.getTeam();

        event.setCanceled(true);

        String textoCru = event.getRawText();
        Component originalMessage = textoCru.indexOf('&') >= 0 ? CodigosEstilo.componente(textoCru) : event.getMessage();
        List<ServerPlayer> recipients = getRecipients(player);
        MutableComponent finalMessage = buildChatMessage(player, team, originalMessage);

        for (ServerPlayer recipient : recipients) {
            recipient.sendSystemMessage(finalMessage);
        }

        RedeChat.enviarBalloonParaProximos(player, event.getRawText());
    }

    private static List<ServerPlayer> getRecipients(ServerPlayer sender) {
        if (!ConfiguracaoChat.isLocalChatEnabled()) {
            return sender.getServer().getPlayerList().getPlayers();
        }

        double radius = ConfiguracaoChat.getLocalChatRadius();
        return sender.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> {
                    if (player.level().dimension() != sender.level().dimension()) {
                        return false;
                    }
                    double distance = player.distanceTo(sender);
                    return distance <= radius;
                })
                .toList();
    }

    private static MutableComponent buildChatMessage(ServerPlayer player, PlayerTeam team, Component originalMessage) {
        MutableComponent badge = PerfilJogador.insignia().componente(player);

        MutableComponent tag = Component.literal("§7| ");
        tag.append(ConfiguracaoChat.isLocalChatEnabled()
                ? Component.translatable("mensagem.aelysiummod.chat.local")
                : Component.translatable("mensagem.aelysiummod.chat.global"));
        if (badge != null) {
            tag.append(badge).append(" ");
        }

        if (player.getServer() != null) {
            MutableComponent styledName = PerfilJogador.apelido().nomeCompletoComPronomes(player.getServer(), player.getUUID());
            if (styledName != null) {
                return tag.append(styledName)
                        .append("§r: ")
                        .append(originalMessage);
            }
        }

        if (team != null) {
            return tag.append(team.getPlayerPrefix())
                    .append(player.getDisplayName())
                    .append(team.getPlayerSuffix())
                    .append("§r: ")
                    .append(originalMessage);
        }

        return tag.append(player.getDisplayName())
                .append("§r: ")
                .append(originalMessage);
    }
}