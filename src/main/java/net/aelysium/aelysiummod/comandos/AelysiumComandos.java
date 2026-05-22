package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.aelysium.aelysiummod.banlist.command.BanlistCommands;
import net.aelysium.aelysiummod.chat.ChatManager;
import net.aelysium.aelysiummod.util.ChatConfig;
import net.aelysium.aelysiummod.util.ModConfig;
import net.aelysium.aelysiummod.deus.DeusCommand;
import net.aelysium.aelysiummod.deus.VanishCommand;
import net.aelysium.aelysiummod.lua.LuaManager;
import net.aelysium.aelysiummod.lua.TipoLua;
import net.aelysium.aelysiummod.network.AelysiumNetwork;
import net.aelysium.aelysiummod.socialspy.SocialSpyManager;
import net.aelysium.aelysiummod.whitelist.WhitelistOfflineManager;
import net.aelysium.aelysiummod.habilidade.HabilidadeManager;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.UUID;

public class AelysiumComandos {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("aelysium")
                        .requires(source -> source.hasPermission(2))

                        .then(NickCommand.build())

                        .then(BanlistCommands.build())

                        .then(NpcCommand.build())

                        .then(Commands.literal("chat")
                                .then(Commands.literal("raio")
                                        .then(Commands.literal("definir")
                                                .then(Commands.argument("blocos", IntegerArgumentType.integer(1, 1000))
                                                        .executes(ctx -> {
                                                            int radius = IntegerArgumentType.getInteger(ctx, "blocos");
                                                            ChatConfig.setLocalChatRadius(radius);
                                                            ctx.getSource().sendSuccess(() ->
                                                                            Component.literal("Raio do chat local definido para " + radius + " blocos."),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("login")
                                        .then(Commands.literal("ligar")
                                                .executes(ctx -> {
                                                    ModConfig.setJoinLeaveMessagesEnabled(true);
                                                    ctx.getSource().sendSuccess(() ->
                                                                    Component.literal("Mensagens de login/logout ativadas."),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                        .then(Commands.literal("desligar")
                                                .executes(ctx -> {
                                                    ModConfig.setJoinLeaveMessagesEnabled(false);
                                                    ctx.getSource().sendSuccess(() ->
                                                                    Component.literal("Mensagens de login/logout desativadas."),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(Commands.literal("balao")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        AelysiumNetwork.enviarAbrirBalloonGui(player);
                                        return 1;
                                    }
                                    return 0;
                                })
                        )

                        .then(Commands.literal("lua")
                                .executes(ctx -> {
                                    LuaManager.toggleLuaVermelha(ctx.getSource().getServer());
                                    String status = LuaManager.luaAtual == TipoLua.VERMELHA
                                            ? "§cLua Vermelha ativada!"
                                            : "§7Lua voltou ao normal.";
                                    ctx.getSource().sendSuccess(() -> Component.literal(status), true);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("socialspy")
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                        ctx.getSource().sendFailure(Component.literal("§cApenas jogadores podem usar este comando."));
                                        return 0;
                                    }
                                    boolean ativo = SocialSpyManager.toggle(player.getUUID());
                                    String status = ativo ? "§aativado" : "§cdesativado";
                                    ctx.getSource().sendSuccess(() -> Component.literal("§7SocialSpy " + status + "§7!"), false);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("manutencao")
                                .executes(ctx -> {
                                    boolean ativo = HabilidadeManager.toggleManutencao();
                                    if (ativo) {
                                        int kickados = 0;
                                        for (ServerPlayer player : new java.util.ArrayList<>(ctx.getSource().getServer().getPlayerList().getPlayers())) {
                                            if (!player.hasPermissions(2)) {
                                                player.connection.disconnect(Component.literal(
                                                        "§c§lAelysium está em manutenção!\n\n§7Fique de olho nos avisos do servidor!"
                                                ));
                                                kickados++;
                                            }
                                        }
                                        int finalKickados = kickados;
                                        ctx.getSource().sendSuccess(() ->
                                                Component.literal("§c§lManutenção ativada! §f" + finalKickados + " jogador(es) desconectado(s)."), true);
                                    } else {
                                        ctx.getSource().sendSuccess(() ->
                                                Component.literal("§a§lManutenção desativada! §fJogadores podem entrar novamente."), true);
                                    }
                                    return 1;
                                })
                        )

                        .then(Commands.literal("whitelist")
                                .then(Commands.literal("adicionar")
                                        .then(Commands.argument("nome", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String nome = StringArgumentType.getString(ctx, "nome");
                                                    if (WhitelistOfflineManager.adicionar(nome)) {
                                                        UUID uuid = WhitelistOfflineManager.gerarUUIDOffline(nome);
                                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                                "§aJogador §f" + nome + " §aadicionado à whitelist! §7(UUID: " + uuid + ")"), true);
                                                        return 1;
                                                    } else {
                                                        ctx.getSource().sendFailure(Component.literal(
                                                                "§cJogador §f" + nome + " §cjá está na whitelist!"));
                                                        return 0;
                                                    }
                                                })
                                        )
                                )
                                .then(Commands.literal("remover")
                                        .then(Commands.argument("nome", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String nome = StringArgumentType.getString(ctx, "nome");
                                                    if (WhitelistOfflineManager.remover(nome)) {
                                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                                "§aJogador §f" + nome + " §aremovido da whitelist!"), true);
                                                        return 1;
                                                    } else {
                                                        ctx.getSource().sendFailure(Component.literal(
                                                                "§cJogador §f" + nome + " §cnão encontrado na whitelist!"));
                                                        return 0;
                                                    }
                                                })
                                        )
                                )
                                .then(Commands.literal("procurar")
                                        .then(Commands.argument("nome", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String nome = StringArgumentType.getString(ctx, "nome");
                                                    String info = WhitelistOfflineManager.getInfo(nome);
                                                    if (info == null) {
                                                        ctx.getSource().sendFailure(Component.literal(
                                                                "§cJogador §f" + nome + " §cnão encontrado na whitelist!"));
                                                        return 0;
                                                    }
                                                    UUID uuid = WhitelistOfflineManager.gerarUUIDOffline(nome);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "§e=== Whitelist ===\n§fNome: " + nome + "\n§fUUID Offline: " + uuid), false);
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(Commands.literal("anunciar")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    Collection<ServerPlayer> alvos = EntityArgument.getPlayers(ctx, "alvos");
                                                    String mensagem = StringArgumentType.getString(ctx, "mensagem");
                                                    String mensagemFormatada = mensagem.replace("&", "\u00a7");
                                                    Component componente = Component.literal(mensagemFormatada);

                                                    for (ServerPlayer player : alvos) {
                                                        player.sendSystemMessage(componente);
                                                    }
                                                    return alvos.size();
                                                })
                                        )
                                )
                        )

                        .then(Commands.literal("dimensao")
                                .then(Commands.argument("dimensao", DimensionArgument.dimension())
                                        .executes(ctx -> {
                                            ServerLevel targetLevel = DimensionArgument.getDimension(ctx, "dimensao");
                                            ResourceKey<?> dimKey = targetLevel.dimension();
                                            String dimId = dimKey.location().toString();

                                            MinecraftServer server = ctx.getSource().getServer();
                                            server.getCommands().performPrefixedCommand(
                                                    ctx.getSource(),
                                                    "execute in " + dimId + " run tp @s 0 128 0 180 0"
                                            );
                                            return 1;
                                        })
                                )
                        )
        );

        event.getDispatcher().register(
                Commands.literal("g")
                        .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        ChatManager.enviarMensagemGlobal(player, StringArgumentType.getString(ctx, "mensagem"));
                                        return 1;
                                    }
                                    return 0;
                                })
                        )
        );

        event.getDispatcher().register(
                Commands.literal("global")
                        .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        ChatManager.enviarMensagemGlobal(player, StringArgumentType.getString(ctx, "mensagem"));
                                        return 1;
                                    }
                                    return 0;
                                })
                        )
        );

        HologramaCommand.register(event.getDispatcher());
        TeleportCommand.register(event.getDispatcher());
        ProtecaoCommand.registrar(event.getDispatcher());
        ProtecaoCommand.registrarSubComandosLista(event.getDispatcher());
        VanishCommand.register(event.getDispatcher());
        RacaCommand.register(event.getDispatcher());
        DeusCommand.register(event.getDispatcher());
        AtributosCommand.registrar(event.getDispatcher());
        HabilidadeCommand.registrar(event.getDispatcher());
    }
}