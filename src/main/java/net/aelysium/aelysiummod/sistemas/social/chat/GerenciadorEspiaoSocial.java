package net.aelysium.aelysiummod.sistemas.social.chat;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GerenciadorEspiaoSocial {

    private static final Logger LOGGER = LoggerFactory.getLogger(AelysiumMod.MOD_ID);

    public static final int DESLIGADO = 0;
    public static final int PRIVADO = 1;
    public static final int TUDO = 2;

    private static final Map<UUID, Integer> niveis = new ConcurrentHashMap<>();

    public static int nivel(UUID uuid) {
        return niveis.getOrDefault(uuid, DESLIGADO);
    }

    public static boolean isAtivo(UUID uuid) {
        return nivel(uuid) > DESLIGADO;
    }

    public static int alternar(UUID uuid) {
        int novo = (nivel(uuid) + 1) % 3;
        if (novo == DESLIGADO) {
            niveis.remove(uuid);
        } else {
            niveis.put(uuid, novo);
        }
        return novo;
    }

    private static Component rotulo(String tipoChat) {
        ChatFormatting cor = switch (tipoChat) {
            case "Regional" -> ChatFormatting.YELLOW;
            case "Solaris" -> ChatFormatting.DARK_PURPLE;
            default -> ChatFormatting.GREEN;
        };
        return Component.literal(tipoChat).withStyle(cor);
    }

    public static void enviarParaSocialspy(ServerPlayer remetente, String mensagem, String tipoChat, List<ServerPlayer> receptoresOriginais) {
        if (remetente.getServer() == null) return;

        String nomeRemetente = remetente.getScoreboardName();

        Component componenteSpy = Component.translatableEscape("mensagem.aelysiummod.chat.spy_local",
                rotulo(tipoChat),
                nomeRemetente,
                mensagem);

        for (ServerPlayer player : remetente.getServer().getPlayerList().getPlayers()) {
            if (receptoresOriginais.contains(player)) continue;
            if (player.getUUID().equals(remetente.getUUID())) continue;

            if (nivel(player.getUUID()) >= TUDO && player.hasPermissions(2)) {
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

            if (nivel(player.getUUID()) >= PRIVADO && player.hasPermissions(2)) {
                player.sendSystemMessage(componenteSpy);
            }
        }
    }
}
