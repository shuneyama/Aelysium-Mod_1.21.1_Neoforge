package net.aelysium.aelysiummod.sistemas.admin.discord;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class RetransmissorMortesDiscord {

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        ConfiguracaoDiscord.Dados cfg = ConfiguracaoDiscord.get();
        if (!cfg.ativo || !cfg.relatarMortes) return;
        if (cfg.webhookMortes == null || cfg.webhookMortes.isBlank()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Component mensagemMorte = event.getSource().getLocalizedDeathMessage(player);
        String mensagem = mensagemMorte.getString();
        if (mensagem.isBlank()) return;

        String conteudo = cfg.formatoMorte.replace("{mensagem}", mensagem);
        WebhookDiscord.enviar(cfg.webhookMortes, cfg.nomeMortes, cfg.avatarMortes, conteudo);
    }
}
