package net.aelysium.aelysiummod.sistemas.admin.discord;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class RetransmissorChatDiscord {

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) return;

        relay(player, event.getRawText(), null);
    }

    @SubscribeEvent
    public static void aoEnviarChat(net.aelysium.aelysiummod.nucleo.evento.ChatEnviadoEvento evento) {
        relay(evento.getRemetente(), evento.getMensagem(), evento.getTag());
    }

    public static void relay(ServerPlayer player, String mensagem, String tag) {
        ConfiguracaoDiscord.Dados cfg = ConfiguracaoDiscord.get();
        if (!cfg.ativo) return;
        if (cfg.webhookChat == null || cfg.webhookChat.isBlank()) return;

        String nomeJogador = player.getGameProfile().getName();

        ConfiguracaoDiscord.SubstituicaoJogador override = ConfiguracaoDiscord.getOverridePorNome(nomeJogador);
        if (override == null) override = ConfiguracaoDiscord.getOverridePorUuid(player.getUUID());

        String nome = cfg.nomePadrao;
        String avatar = cfg.avatarPadrao;
        String formato = cfg.formatoPadrao;

        if (override != null) {
            if (override.nome != null && !override.nome.isBlank()) nome = override.nome;
            if (override.avatarUrl != null && !override.avatarUrl.isBlank()) avatar = override.avatarUrl;
            if (override.formato != null && !override.formato.isBlank()) formato = override.formato;
        }

        nome = nome.replace("{player}", nomeJogador);
        avatar = avatar.replace("{player}", nomeJogador);
        String mensagemComTag = (tag != null && !tag.isBlank()) ? "**[" + tag + "]** " + mensagem : mensagem;
        String conteudo = formato
                .replace("{player}", nomeJogador)
                .replace("{message}", mensagemComTag);

        WebhookDiscord.enviar(cfg.webhookChat, nome, avatar, conteudo);
    }
}