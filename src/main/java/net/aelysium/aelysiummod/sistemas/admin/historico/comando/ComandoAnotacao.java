package net.aelysium.aelysiummod.sistemas.admin.historico.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.UserBanListEntry;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.time.*;
import net.aelysium.aelysiummod.nucleo.evento.AnotacaoAdicionadaEvento;
import net.neoforged.neoforge.common.NeoForge;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.aelysium.aelysiummod.sistemas.admin.historico.ConfiguracaoAnotacao;
import net.aelysium.aelysiummod.sistemas.admin.historico.DadosAnotacao;
import net.aelysium.aelysiummod.sistemas.admin.historico.DadosHistorico;

public class ComandoAnotacao {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        SuggestionProvider<CommandSourceStack> tipoSugg = (ctx, builder) -> {
            for (String t : ConfiguracaoAnotacao.getTipos()) builder.suggest(t);
            return builder.buildFuture();
        };

        SuggestionProvider<CommandSourceStack> jogadorSugg = (ctx, builder) -> {
            MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
            if (srv != null) {
                for (ServerPlayer p : srv.getPlayerList().getPlayers()) builder.suggest(p.getName().getString());
            }
            return builder.buildFuture();
        };

        return Commands.literal("anotacao")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("jogador", StringArgumentType.string())
                        .suggests(jogadorSugg)
                        .then(Commands.argument("tipo", StringArgumentType.word())
                                .suggests(tipoSugg)
                                .then(Commands.argument("comentario", StringArgumentType.greedyString())
                                        .executes(ctx -> adicionarAnotacao(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "jogador"),
                                                StringArgumentType.getString(ctx, "tipo"),
                                                StringArgumentType.getString(ctx, "comentario")
                                        ))
                                )
                        )
                        .then(Commands.literal("remover")
                                .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                        .executes(ctx -> removerAnotacao(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "jogador"),
                                                IntegerArgumentType.getInteger(ctx, "indice")
                                        ))
                                )
                        )
                );
    }

    private static int adicionarAnotacao(CommandSourceStack source, String jogadorId, String tipo, String comentario) {
        boolean tipoValido = ConfiguracaoAnotacao.getTipos().stream().anyMatch(t -> t.equalsIgnoreCase(tipo));
        if (!tipoValido) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.historico.tipo_invalido_disponiveis", String.join(", ", ConfiguracaoAnotacao.getTipos())));
            return 0;
        }

        MinecraftServer server = source.getServer();
        UUID alvoUuid = null;
        String alvoNome = null;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(jogadorId)) {
                alvoUuid = p.getUUID();
                alvoNome = p.getName().getString();
                break;
            }
        }
        if (alvoUuid == null) {
            alvoUuid = DadosHistorico.resolverIdentificador(jogadorId);
            if (alvoUuid != null) alvoNome = DadosHistorico.getNome(alvoUuid);
        }
        if (alvoUuid == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.historico.jogador_nao_encontrado", jogadorId));
            return 0;
        }

        UUID respUuid = null;
        String respNome = "Console";
        if (source.getEntity() instanceof ServerPlayer admin) {
            respUuid = admin.getUUID();
            respNome = admin.getName().getString();
        }

        String tipoNorm = tipo.toLowerCase();
        DadosAnotacao.Anotacao anotacao = new DadosAnotacao.Anotacao(
                alvoUuid, alvoNome, tipoNorm, comentario, System.currentTimeMillis(), respUuid, respNome,
                Math.round(DadosHistorico.getTotalHoras(alvoUuid) * 60.0)
        );
        DadosAnotacao.adicionarAnotacao(anotacao);

        String tipoDisplay = Character.toUpperCase(tipoNorm.charAt(0)) + tipoNorm.substring(1);
        final String nomeAlvo = alvoNome;
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.historico.aelysium_anotacao_adicionada_para", tipoDisplay, nomeAlvo), true);

        notificarDiscord(anotacao, tipoDisplay);

        if (ConfiguracaoAnotacao.isAutoBan() && tipoNorm.equalsIgnoreCase(ConfiguracaoAnotacao.getTipoBan())) {
            verificarBanAutomatico(server, alvoUuid, nomeAlvo);
        }

        return 1;
    }

    private static int removerAnotacao(CommandSourceStack source, String jogadorId, int indice) {
        UUID uuid = resolverUuid(source.getServer(), jogadorId);
        if (uuid == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.historico.jogador_nao_encontrado", jogadorId));
            return 0;
        }
        String nome = DadosHistorico.getNome(uuid);
        if (DadosAnotacao.removerAnotacao(uuid, indice - 1)) {
            source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.historico.aelysium_anotacao_de_removida", indice, nome), true);
            return 1;
        }
        source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.historico.indice_invalido_use_debug_historico", jogadorId));
        return 0;
    }

    private static UUID resolverUuid(MinecraftServer server, String id) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(id)) return p.getUUID();
        }
        return DadosHistorico.resolverIdentificador(id);
    }

    private static void notificarDiscord(DadosAnotacao.Anotacao a, String tipoDisplay) {
        NeoForge.EVENT_BUS.post(new AnotacaoAdicionadaEvento(
                a.alvoNome(), tipoDisplay, a.responsavelNome(), a.comentario(), a.timestamp()));
    }

    private static void verificarBanAutomatico(MinecraftServer server, UUID uuid, String nome) {
        int alertas = DadosAnotacao.contarPorTipo(uuid, ConfiguracaoAnotacao.getTipoBan());

        int limIp = ConfiguracaoAnotacao.getAlertasParaBanIp();
        if (limIp > 0 && alertas >= limIp) {
            String ip = obterUltimoIp(uuid);
            if (ip != null && !ip.equals("desconhecido")) {
                server.getPlayerList().getIpBans().add(
                        new IpBanListEntry(ip, new Date(), "Aelysium (automático)", null, "Limite máximo de alertas")
                );
            }
            server.getPlayerList().getBans().add(
                    new UserBanListEntry(new GameProfile(uuid, nome), new Date(), "Aelysium (automático)", null, "Ban permanente — limite de alertas")
            );
            kickSeOnline(server, uuid, "§cVocê foi banido permanentemente.\n§7Motivo: limite de alertas atingido.");
            return;
        }

        int limTemp = ConfiguracaoAnotacao.getAlertasParaBanTemp();
        if (limTemp > 0 && alertas >= limTemp) {
            long duracaoMs = (long) ConfiguracaoAnotacao.getDuracaoBanTempHoras() * 3_600_000L;
            Date expira = new Date(System.currentTimeMillis() + duracaoMs);
            server.getPlayerList().getBans().add(
                    new UserBanListEntry(new GameProfile(uuid, nome), new Date(), "Aelysium (automático)", expira, "Ban temporário — limite de alertas")
            );
            kickSeOnline(server, uuid, "§cVocê foi banido temporariamente.\n§7Motivo: limite de alertas atingido.\n§7Duração: " + ConfiguracaoAnotacao.getDuracaoBanTempHoras() + "h");
        }
    }

    private static String obterUltimoIp(UUID uuid) {
        List<DadosHistorico.Sessao> sessoes = DadosHistorico.getSessoes(uuid);
        if (sessoes.isEmpty()) return null;
        return sessoes.get(sessoes.size() - 1).ip();
    }

    private static void kickSeOnline(MinecraftServer server, UUID uuid, String mensagem) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player != null) player.connection.disconnect(Component.literal(mensagem));
    }
}
