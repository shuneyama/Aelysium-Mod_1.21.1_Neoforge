package net.aelysium.aelysiummod.sistemas.admin.discord;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.evento.AnotacaoAdicionadaEvento;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OuvinteAnotacaoDiscord {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @SubscribeEvent
    public static void aoAdicionarAnotacao(AnotacaoAdicionadaEvento evento) {
        String webhook = ConfiguracaoDiscord.get().webhookAnotacoes;
        if (webhook == null || webhook.isBlank()) return;
        String horario = LocalDateTime.ofInstant(Instant.ofEpochMilli(evento.getTimestamp()), ZoneId.systemDefault()).format(FMT);
        String conteudo = "📋 **Nova Anotação** — **" + evento.getAlvoNome() + "**\n"
                + "**Tipo:** " + evento.getTipo() + "\n"
                + "**Responsável:** " + evento.getResponsavelNome() + "\n"
                + "**Comentário:** " + evento.getComentario() + "\n"
                + "**Horário:** " + horario;
        WebhookDiscord.enviar(webhook, "Aelysium — Anotações", "", conteudo);
    }
}
