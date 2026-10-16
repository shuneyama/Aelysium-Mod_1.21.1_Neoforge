package net.aelysium.aelysiummod.sistemas.social.chat;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GerenciadorEspiaoSocial {

    private static final Logger LOGGER = LoggerFactory.getLogger(AelysiumMod.MOD_ID);

    private static final Set<UUID> socialspyAtivos = new HashSet<>();

    public static boolean isAtivo(UUID uuid) {
        return socialspyAtivos.contains(uuid);
    }

    public static boolean toggle(UUID uuid) {
        if (socialspyAtivos.contains(uuid)) {
            socialspyAtivos.remove(uuid);
            return false;
        } else {
            socialspyAtivos.add(uuid);
            return true;
        }
    }

    public static void enviarParaSocialspy(ServerPlayer remetente, String mensagem, String tipoChat, List<ServerPlayer> receptoresOriginais) {
        if (remetente.getServer() == null) return;

        String nomeRemetente = remetente.getScoreboardName();

        Component componenteSpy = Component.translatableEscape("mensagem.aelysiummod.chat.spy_local",
                tipoChat,
                nomeRemetente,
                mensagem,
                receptoresOriginais.size());

        for (ServerPlayer player : remetente.getServer().getPlayerList().getPlayers()) {
            if (receptoresOriginais.contains(player)) continue;
            if (player.getUUID().equals(remetente.getUUID())) continue;

            if (isAtivo(player.getUUID()) && player.hasPermissions(2)) {
                player.sendSystemMessage(componenteSpy);
            }
        }
    }

    public static void enviarParaSocialspyPrivado(ServerPlayer remetente, ServerPlayer destinatario, String mensagem) {
        if (remetente.getServer() == null) return;

        String nomeRemetente = remetente.getScoreboardName();
        String nomeDestinatario = destinatario.getScoreboardName();

        LOGGER.info("[Tell] {} -> {}: {}", nomeRemetente, nomeDestinatario, mensagem);

        Component componenteSpy = Component.translatableEscape("mensagem.aelysiummod.chat.spy_privado",
                nomeRemetente,
                nomeDestinatario,
                mensagem);

        for (ServerPlayer player : remetente.getServer().getPlayerList().getPlayers()) {
            if (player.getUUID().equals(remetente.getUUID())) continue;
            if (player.getUUID().equals(destinatario.getUUID())) continue;

            if (isAtivo(player.getUUID()) && player.hasPermissions(2)) {
                player.sendSystemMessage(componenteSpy);
            }
        }
    }
}
