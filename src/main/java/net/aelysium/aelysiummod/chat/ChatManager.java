package net.aelysium.aelysiummod.chat;

import net.aelysium.aelysiummod.util.ChatConfig;
import net.aelysium.aelysiummod.habilidade.HabilidadeManager;
import net.aelysium.aelysiummod.network.AelysiumNetwork;
import net.aelysium.aelysiummod.nickname.NicknameData;
import net.aelysium.aelysiummod.socialspy.SocialSpyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class ChatManager {

    public static void enviarMensagemLocal(ServerPlayer remetente, String mensagem) {
        if (HabilidadeManager.estaMudo(remetente.getUUID())) {
            remetente.sendSystemMessage(Component.literal("§cVocê está silenciado e não pode enviar mensagens no chat."));
            return;
        }

        if (!AntiSpamManager.podeEnviar(remetente.getUUID(), mensagem)) {
            remetente.sendSystemMessage(Component.literal("§cAguarde antes de enviar a mesma mensagem!"));
            return;
        }

        List<ServerPlayer> receptores = getJogadoresNoAlcance(remetente, ChatConfig.getLocalChatRadius());

        Component msgFormatada = Component.literal("§7[§aLocal§7] §f").append(getStyledName(remetente)).append(Component.literal("§r: " + mensagem));

        for (ServerPlayer receptor : receptores) {
            receptor.sendSystemMessage(msgFormatada);
        }

        AelysiumNetwork.enviarBalloonParaProximos(remetente, mensagem);

        SocialSpyManager.enviarParaSocialspy(remetente, mensagem, "Local", receptores);
    }

    public static void enviarMensagemGlobal(ServerPlayer remetente, String mensagem) {
        if (HabilidadeManager.estaMudo(remetente.getUUID())) {
            remetente.sendSystemMessage(Component.literal("§cVocê está silenciado e não pode enviar mensagens no chat."));
            return;
        }

        if (!AntiSpamManager.podeEnviar(remetente.getUUID(), mensagem)) {
            remetente.sendSystemMessage(Component.literal("§cAguarde antes de enviar a mesma mensagem!"));
            return;
        }

        List<ServerPlayer> receptores = new ArrayList<>(remetente.getServer().getPlayerList().getPlayers());

        Component msgFormatada = Component.literal("§7[§eGlobal§7] §f").append(getStyledName(remetente)).append(Component.literal("§r: " + mensagem));

        for (ServerPlayer receptor : receptores) {
            receptor.sendSystemMessage(msgFormatada);
        }

        SocialSpyManager.enviarParaSocialspy(remetente, mensagem, "Global", receptores);
    }

    private static Component getStyledName(ServerPlayer player) {
        if (player.getServer() != null) {
            NicknameData data = NicknameData.get(player.getServer());
            MutableComponent styled = data.getStyledFullName(player.getUUID());
            if (styled != null) return styled;
        }
        return Component.literal(player.getScoreboardName());
    }

    private static String getNomeExibido(ServerPlayer player) {
        if (player.getServer() != null) {
            NicknameData data = NicknameData.get(player.getServer());
            NicknameData.NicknameEntry entry = data.getNickname(player.getUUID());
            if (entry != null && !entry.nick().isEmpty()) return entry.nick();
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