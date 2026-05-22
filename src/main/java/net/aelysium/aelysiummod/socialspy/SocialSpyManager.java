package net.aelysium.aelysiummod.socialspy;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SocialSpyManager {

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

        String mensagemSpy = String.format(
                "§8[§cSPY§8] §7[%s] §f<%s>§7 %s §8(para %d jogadores)",
                tipoChat,
                nomeRemetente,
                mensagem,
                receptoresOriginais.size()
        );

        Component componenteSpy = Component.literal(mensagemSpy);

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

        String mensagemSpy = String.format(
                "§8[§cSPY§8] §d[MSG] §f%s §7-> §f%s§7: §f%s",
                nomeRemetente,
                nomeDestinatario,
                mensagem
        );

        Component componenteSpy = Component.literal(mensagemSpy);

        for (ServerPlayer player : remetente.getServer().getPlayerList().getPlayers()) {
            if (player.getUUID().equals(remetente.getUUID())) continue;
            if (player.getUUID().equals(destinatario.getUUID())) continue;

            if (isAtivo(player.getUUID()) && player.hasPermissions(2)) {
                player.sendSystemMessage(componenteSpy);
            }
        }
    }
}
