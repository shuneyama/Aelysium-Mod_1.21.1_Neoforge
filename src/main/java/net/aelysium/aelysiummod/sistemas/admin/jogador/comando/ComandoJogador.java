package net.aelysium.aelysiummod.sistemas.admin.jogador.comando;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.compatibilidade.voicechat.ComandoVoz;
import net.aelysium.aelysiummod.compatibilidade.voicechat.ConfiguracaoVoz;
import net.aelysium.aelysiummod.compatibilidade.voicechat.ConfiguracaoVoz.TipoLista;
import net.aelysium.aelysiummod.sistemas.rp.habilidade.GerenciadorHabilidade;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ComandoJogador {
    private static final int PERMISSAO = 2;
    private static final String ALVOS = "alvos";
    private static final String JOGADORES = "jogadores";
    private static final String ESCOPO = "escopo";
    private static final String HAB = "mensagem.aelysiummod.habilidade.";
    private static final String CONV = "mensagem.aelysiummod.conversa.";
    private static final List<String> ESCOPOS = List.of("chat", "voz", "tudo");

    private interface Acao {
        int executar(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos);
    }

    private interface AcaoPerfis {
        int executar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("jogador")
                .requires(source -> source.hasPermission(PERMISSAO))
                .then(comAlvos("voo", ComandoJogador::voo))
                .then(comAlvos("curar", ComandoJogador::curar))
                .then(comAlvos("congelar", ComandoJogador::congelar))
                .then(comAlvos("invulneravel", ComandoJogador::invulneravel))
                .then(conversa());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> comAlvos(String nome, Acao acao) {
        return Commands.literal(nome)
                .executes(ctx -> acao.executar(ctx, executor(ctx)))
                .then(Commands.argument(ALVOS, EntityArgument.players())
                        .executes(ctx -> acao.executar(ctx, EntityArgument.getPlayers(ctx, ALVOS))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> conversa() {
        return Commands.literal("conversa")
                .then(Commands.literal("silenciar")
                        .then(perfis(ctx -> silenciar(ctx, true), true)))
                .then(Commands.literal("dessilenciar")
                        .then(perfis(ctx -> silenciar(ctx, false), true)))
                .then(Commands.literal("lista")
                        .executes(ctx -> listar(ctx, "tudo"))
                        .then(escopo(ctx -> listar(ctx, StringArgumentType.getString(ctx, ESCOPO)))))
                .then(Commands.literal("global")
                        .then(Commands.literal("adicionar").then(perfis(ComandoVoz::globalAdicionar, false)))
                        .then(Commands.literal("remover").then(perfis(ComandoVoz::globalRemover, false)))
                        .then(Commands.literal("lista").executes(ctx -> ComandoVoz.listar(ctx, TipoLista.GLOBAL, "titulo_global"))))
                .then(Commands.literal("protegidos")
                        .then(Commands.literal("adicionar").then(perfis(ComandoVoz::whitelistAdicionar, false)))
                        .then(Commands.literal("remover").then(perfis(ComandoVoz::whitelistRemover, false)))
                        .then(Commands.literal("lista").executes(ctx -> ComandoVoz.listar(ctx, TipoLista.WHITELIST, "titulo_whitelist"))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> perfis(AcaoPerfis acao, boolean comEscopo) {
        var argumento = Commands.argument(JOGADORES, GameProfileArgument.gameProfile());
        if (comEscopo) {
            return argumento
                    .executes(ctx -> acao.executar(ctx))
                    .then(escopo(acao::executar));
        }
        return argumento.executes(acao::executar);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> escopo(com.mojang.brigadier.Command<CommandSourceStack> comando) {
        return Commands.argument(ESCOPO, StringArgumentType.word())
                .suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(ESCOPOS, construtor))
                .executes(comando);
    }

    private static String escopoDe(CommandContext<CommandSourceStack> ctx) {
        try {
            return StringArgumentType.getString(ctx, ESCOPO).toLowerCase();
        } catch (IllegalArgumentException e) {
            return "tudo";
        }
    }

    private static boolean escopoValido(CommandContext<CommandSourceStack> ctx, String escopo) {
        if (ESCOPOS.contains(escopo)) return true;
        ctx.getSource().sendFailure(Component.translatableEscape(CONV + "escopo_invalido", escopo));
        return false;
    }

    private static Collection<ServerPlayer> executor(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getEntity() instanceof ServerPlayer jogador) {
            return List.of(jogador);
        }
        ctx.getSource().sendFailure(Component.translatable(HAB + "voce_precisa_especificar_um_jogador"));
        return List.of();
    }

    private static int voo(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer jogador : alvos) {
            boolean ativo = GerenciadorHabilidade.toggleVoo(jogador);
            jogador.getAbilities().mayfly = ativo;
            jogador.getAbilities().flying = ativo;
            jogador.getAbilities().setFlyingSpeed(ativo ? 0.05f * 0.5f : 0.05f);
            jogador.onUpdateAbilities();
            String chave = ativo ? "voo_ativado_para" : "voo_desativado_para";
            ctx.getSource().sendSuccess(() -> Component.translatableEscape(HAB + chave, jogador.getName().getString()), true);
        }
        return alvos.size();
    }

    private static int invulneravel(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer jogador : alvos) {
            boolean ativo = GerenciadorHabilidade.toggleDeus(jogador);
            jogador.setInvulnerable(ativo);
            String chave = ativo ? "modo_deus_ativado_para" : "modo_deus_desativado_para";
            ctx.getSource().sendSuccess(() -> Component.translatableEscape(HAB + chave, jogador.getName().getString()), true);
        }
        return alvos.size();
    }

    private static int curar(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer jogador : alvos) {
            jogador.setHealth(jogador.getMaxHealth());
            FoodData comida = jogador.getFoodData();
            comida.setFoodLevel(20);
            comida.setSaturation(20f);
            ctx.getSource().sendSuccess(() -> Component.translatableEscape(HAB + "foi_curado_completamente", jogador.getName().getString()), true);
        }
        return alvos.size();
    }

    private static int congelar(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer jogador : alvos) {
            boolean ativo = GerenciadorHabilidade.toggleCongelar(jogador);
            String chave = ativo ? "foi_congelado" : "foi_descongelado";
            ctx.getSource().sendSuccess(() -> Component.translatableEscape(HAB + chave, jogador.getName().getString()), true);
        }
        return alvos.size();
    }

    private static int silenciar(CommandContext<CommandSourceStack> ctx, boolean silenciar) throws CommandSyntaxException {
        String escopo = escopoDe(ctx);
        if (!escopoValido(ctx, escopo)) return 0;
        boolean comVoz = !escopo.equals("chat");
        boolean comChat = !escopo.equals("voz");
        int total = 0;
        if (comVoz) {
            if (ConfiguracaoVoz.voicechatCarregado()) {
                total += silenciar ? ComandoVoz.mutar(ctx) : ComandoVoz.desmutar(ctx);
            } else {
                ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.voz.nao_instalado"));
            }
        }
        if (comChat) {
            total += silenciarChat(ctx, silenciar);
        }
        return total;
    }

    private static int silenciarChat(CommandContext<CommandSourceStack> ctx, boolean silenciar) throws CommandSyntaxException {
        List<String> alterados = new ArrayList<>();
        List<String> iguais = new ArrayList<>();
        List<String> ausentes = new ArrayList<>();
        for (GameProfile perfil : GameProfileArgument.getGameProfiles(ctx, JOGADORES)) {
            ServerPlayer jogador = ctx.getSource().getServer().getPlayerList().getPlayer(perfil.getId());
            if (jogador == null) {
                ausentes.add(perfil.getName());
            } else if (GerenciadorHabilidade.definirMudo(jogador.getUUID(), silenciar)) {
                alterados.add(perfil.getName());
                jogador.sendSystemMessage(Component.translatable(HAB + (silenciar
                        ? "voce_foi_silenciado_e_nao_pode_mais"
                        : "voce_pode_enviar_mensagens_no_chat")));
            } else {
                iguais.add(perfil.getName());
            }
        }
        if (!alterados.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable(CONV + (silenciar ? "chat_silenciou" : "chat_dessilenciou"), String.join(", ", alterados)), true);
        }
        if (!iguais.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(CONV + (silenciar ? "chat_ja_silenciados" : "chat_nao_silenciados"), String.join(", ", iguais)));
        }
        if (!ausentes.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable(CONV + "chat_fora_do_servidor", String.join(", ", ausentes)));
        }
        return alterados.size();
    }

    private static int listar(CommandContext<CommandSourceStack> ctx, String escopo) {
        escopo = escopo.toLowerCase();
        if (!escopoValido(ctx, escopo)) return 0;
        int total = 0;
        if (!escopo.equals("voz")) {
            total += listarChat(ctx);
        }
        if (!escopo.equals("chat")) {
            if (ConfiguracaoVoz.voicechatCarregado()) {
                total += ComandoVoz.listar(ctx, TipoLista.MUTADOS, "titulo_mutados");
            } else {
                ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.voz.nao_instalado"));
            }
        }
        return total;
    }

    private static int listarChat(CommandContext<CommandSourceStack> ctx) {
        List<UUID> silenciados = GerenciadorHabilidade.silenciados();
        CommandSourceStack fonte = ctx.getSource();
        fonte.sendSystemMessage(Component.translatable(CONV + "titulo_chat", silenciados.size()));
        if (silenciados.isEmpty()) {
            fonte.sendSystemMessage(Component.translatable("mensagem.aelysiummod.voz.lista_vazia"));
        }
        for (UUID id : silenciados) {
            ServerPlayer jogador = fonte.getServer().getPlayerList().getPlayer(id);
            String nome = jogador != null ? jogador.getGameProfile().getName() : id.toString();
            fonte.sendSystemMessage(Component.literal("  §a● §f" + nome));
        }
        return silenciados.size();
    }
}
