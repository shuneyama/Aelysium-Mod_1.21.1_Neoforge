package net.aelysium.aelysiummod.sistemas.admin.discord;

import com.mojang.brigadier.context.CommandContextBuilder;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class RetransmissorEspiaoSocialDiscord {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCommand(CommandEvent event) {
        ConfiguracaoDiscord.Dados cfg = ConfiguracaoDiscord.get();
        if (!cfg.ativo) return;
        if (!cfg.socialSpyAtivo) return;
        if (cfg.webhookSocialSpy == null || cfg.webhookSocialSpy.isBlank()) return;

        CommandContextBuilder<CommandSourceStack> ctx = event.getParseResults().getContext();
        if (ctx == null) return;

        String comandoCompleto = event.getParseResults().getReader().getString();
        String comandoSemBarra = comandoCompleto.startsWith("/") ? comandoCompleto.substring(1) : comandoCompleto;

        String[] partes = comandoSemBarra.trim().split("\\s+", 2);
        if (partes.length < 2) return;

        String raiz = partes[0].toLowerCase();
        if (!ConfiguracaoDiscord.isComandoSocialSpy(raiz)) return;

        String resto = partes[1];
        int sp = resto.indexOf(' ');
        if (sp < 0) return;

        String destinatario = resto.substring(0, sp);
        String mensagem = resto.substring(sp + 1);

        CommandSourceStack source = ctx.getSource();
        boolean isCommandBlock = source.getEntity() == null && source.getPlayer() == null;
        String remetente = isCommandBlock ? "CommandBlock" : source.getTextName();

        String texto = aplicar(cfg.formatoSocialSpy, remetente, destinatario, mensagem);
        WebhookDiscord.enviar(cfg.webhookSocialSpy, cfg.nomeSocialSpy, cfg.avatarSocialSpy, texto);
    }

    private static String aplicar(String formato, String remetente, String destinatario, String mensagem) {
        if (formato == null) return "";
        return formato
                .replace("{remetente}", remetente == null ? "" : remetente)
                .replace("{destinatario}", destinatario == null ? "" : destinatario)
                .replace("{mensagem}", mensagem == null ? "" : mensagem);
    }
}