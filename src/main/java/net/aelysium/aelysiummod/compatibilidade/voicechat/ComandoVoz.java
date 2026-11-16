package net.aelysium.aelysiummod.compatibilidade.voicechat;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.compatibilidade.voicechat.ConfiguracaoVoz.TipoLista;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ComandoVoz {

    private static final String JOGADORES = "jogadores";
    private static final String PREFIXO = "mensagem.aelysiummod.voz.";

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("voice")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("microfone")
                        .then(Commands.literal("mutar")
                                .then(Commands.argument(JOGADORES, GameProfileArgument.gameProfile())
                                        .executes(ComandoVoz::mutar)))
                        .then(Commands.literal("desmutar")
                                .then(Commands.argument(JOGADORES, GameProfileArgument.gameProfile())
                                        .executes(ComandoVoz::desmutar)))
                        .then(Commands.literal("lista")
                                .executes(ctx -> listar(ctx, TipoLista.MUTADOS, "titulo_mutados"))))

                .then(Commands.literal("global")
                        .then(Commands.literal("adicionar")
                                .then(Commands.argument(JOGADORES, GameProfileArgument.gameProfile())
                                        .executes(ComandoVoz::globalAdicionar)))
                        .then(Commands.literal("remover")
                                .then(Commands.argument(JOGADORES, GameProfileArgument.gameProfile())
                                        .executes(ComandoVoz::globalRemover)))
                        .then(Commands.literal("lista")
                                .executes(ctx -> listar(ctx, TipoLista.GLOBAL, "titulo_global"))))

                .then(Commands.literal("whitelist")
                        .then(Commands.literal("adicionar")
                                .then(Commands.argument(JOGADORES, GameProfileArgument.gameProfile())
                                        .executes(ComandoVoz::whitelistAdicionar)))
                        .then(Commands.literal("remover")
                                .then(Commands.argument(JOGADORES, GameProfileArgument.gameProfile())
                                        .executes(ComandoVoz::whitelistRemover)))
                        .then(Commands.literal("lista")
                                .executes(ctx -> listar(ctx, TipoLista.WHITELIST, "titulo_whitelist"))));
    }

    private static boolean verificarVoicechat(CommandContext<CommandSourceStack> ctx) {
        if (ConfiguracaoVoz.voicechatCarregado()) return true;
        ctx.getSource().sendFailure(Component.translatable(PREFIXO + "nao_instalado"));
        return false;
    }

    private static int mutar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!verificarVoicechat(ctx)) return 0;

        List<String> mutados = new ArrayList<>();
        List<String> jaMutados = new ArrayList<>();
        List<String> protegidos = new ArrayList<>();
        for (GameProfile alvo : GameProfileArgument.getGameProfiles(ctx, JOGADORES)) {
            if (ConfiguracaoVoz.contem(TipoLista.WHITELIST, alvo.getId())) {
                protegidos.add(alvo.getName());
            } else if (ConfiguracaoVoz.adicionar(TipoLista.MUTADOS, alvo.getId(), alvo.getName())) {
                mutados.add(alvo.getName());
                notificar(ctx, alvo, "voce_foi_mutado");
            } else {
                jaMutados.add(alvo.getName());
            }
        }

        relatar(ctx, mutados, "mutou");
        avisar(ctx, jaMutados, "ja_mutados");
        avisar(ctx, protegidos, "protegidos_whitelist");
        return mutados.size();
    }

    private static int desmutar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!verificarVoicechat(ctx)) return 0;

        List<String> desmutados = new ArrayList<>();
        List<String> naoMutados = new ArrayList<>();
        for (GameProfile alvo : GameProfileArgument.getGameProfiles(ctx, JOGADORES)) {
            if (ConfiguracaoVoz.remover(TipoLista.MUTADOS, alvo.getId())) {
                desmutados.add(alvo.getName());
                notificar(ctx, alvo, "voce_foi_desmutado");
            } else {
                naoMutados.add(alvo.getName());
            }
        }

        relatar(ctx, desmutados, "desmutou");
        avisar(ctx, naoMutados, "nao_mutados");
        return desmutados.size();
    }

    private static int globalAdicionar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!verificarVoicechat(ctx)) return 0;

        List<String> adicionados = new ArrayList<>();
        List<String> jaTinham = new ArrayList<>();
        for (GameProfile alvo : GameProfileArgument.getGameProfiles(ctx, JOGADORES)) {
            if (ConfiguracaoVoz.adicionar(TipoLista.GLOBAL, alvo.getId(), alvo.getName())) {
                adicionados.add(alvo.getName());
                notificar(ctx, alvo, "voz_global_ativada");
            } else {
                jaTinham.add(alvo.getName());
            }
        }

        relatar(ctx, adicionados, "global_adicionou");
        avisar(ctx, jaTinham, "global_ja_tinham");
        return adicionados.size();
    }

    private static int globalRemover(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!verificarVoicechat(ctx)) return 0;

        List<String> removidos = new ArrayList<>();
        List<String> ausentes = new ArrayList<>();
        for (GameProfile alvo : GameProfileArgument.getGameProfiles(ctx, JOGADORES)) {
            if (ConfiguracaoVoz.remover(TipoLista.GLOBAL, alvo.getId())) {
                removidos.add(alvo.getName());
                notificar(ctx, alvo, "voz_global_desativada");
            } else {
                ausentes.add(alvo.getName());
            }
        }

        relatar(ctx, removidos, "global_removeu");
        avisar(ctx, ausentes, "global_nao_tinham");
        return removidos.size();
    }

    private static int whitelistAdicionar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!verificarVoicechat(ctx)) return 0;

        List<String> adicionados = new ArrayList<>();
        List<String> jaEstavam = new ArrayList<>();
        for (GameProfile alvo : GameProfileArgument.getGameProfiles(ctx, JOGADORES)) {
            if (ConfiguracaoVoz.adicionar(TipoLista.WHITELIST, alvo.getId(), alvo.getName())) {
                adicionados.add(alvo.getName());
            } else {
                jaEstavam.add(alvo.getName());
            }
        }

        relatar(ctx, adicionados, "whitelist_adicionou");
        avisar(ctx, jaEstavam, "whitelist_ja_estavam");
        return adicionados.size();
    }

    private static int whitelistRemover(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!verificarVoicechat(ctx)) return 0;

        List<String> removidos = new ArrayList<>();
        List<String> ausentes = new ArrayList<>();
        for (GameProfile alvo : GameProfileArgument.getGameProfiles(ctx, JOGADORES)) {
            if (ConfiguracaoVoz.remover(TipoLista.WHITELIST, alvo.getId())) {
                removidos.add(alvo.getName());
            } else {
                ausentes.add(alvo.getName());
            }
        }

        relatar(ctx, removidos, "whitelist_removeu");
        avisar(ctx, ausentes, "whitelist_nao_estavam");
        return removidos.size();
    }

    private static int listar(CommandContext<CommandSourceStack> ctx, TipoLista tipo, String chaveTitulo) {
        if (!verificarVoicechat(ctx)) return 0;

        Map<UUID, String> entradas = ConfiguracaoVoz.obter(tipo);
        MinecraftServer server = ctx.getSource().getServer();
        CommandSourceStack fonte = ctx.getSource();

        fonte.sendSystemMessage(Component.translatable(PREFIXO + chaveTitulo, entradas.size()));

        if (entradas.isEmpty()) {
            fonte.sendSystemMessage(Component.translatable(PREFIXO + "lista_vazia"));
        }

        entradas.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER))
                .forEach(entrada -> {
                    boolean online = server.getPlayerList().getPlayer(entrada.getKey()) != null;
                    MutableComponent linha = Component.literal((online ? "  §a● §f" : "  §8● §7") + entrada.getValue());
                    if (tipo == TipoLista.MUTADOS && ConfiguracaoVoz.contem(TipoLista.WHITELIST, entrada.getKey())) {
                        linha.append(Component.translatable(PREFIXO + "lista_protegido"));
                    }
                    fonte.sendSystemMessage(linha);
                });

        return entradas.size();
    }

    private static void notificar(CommandContext<CommandSourceStack> ctx, GameProfile alvo, String chave) {
        ServerPlayer jogador = ctx.getSource().getServer().getPlayerList().getPlayer(alvo.getId());
        if (jogador != null) {
            jogador.sendSystemMessage(Component.translatable(PREFIXO + chave));
        }
    }

    private static void relatar(CommandContext<CommandSourceStack> ctx, List<String> nomes, String chave) {
        if (nomes.isEmpty()) return;
        ctx.getSource().sendSuccess(() -> Component.translatable(PREFIXO + chave, String.join(", ", nomes)), true);
    }

    private static void avisar(CommandContext<CommandSourceStack> ctx, List<String> nomes, String chave) {
        if (nomes.isEmpty()) return;
        ctx.getSource().sendFailure(Component.translatable(PREFIXO + chave, String.join(", ", nomes)));
    }
}
