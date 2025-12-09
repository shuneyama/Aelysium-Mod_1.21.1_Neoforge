package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.aelysium.aelysiummod.comandos.chat.GlobalChatCommand;
import net.aelysium.aelysiummod.comandos.racas.Deus;
import net.aelysium.aelysiummod.comandos.racas.Dracono;
import net.aelysium.aelysiummod.comandos.racas.Elvarin;
import net.aelysium.aelysiummod.comandos.racas.Humano;
import net.aelysium.aelysiummod.comandos.racas.Resetar;
import net.aelysium.aelysiummod.comandos.racas.Tiefling;
import net.aelysium.aelysiummod.comandos.racas.Undyne;
import net.aelysium.aelysiummod.comandos.racas.Valkyria;
import net.aelysium.aelysiummod.config.ChatConfig;
import net.aelysium.aelysiummod.config.ModConfig;
import net.aelysium.aelysiummod.config.racas.Deus_Config;
import net.aelysium.aelysiummod.config.racas.Dracono_Config;
import net.aelysium.aelysiummod.config.racas.Elvarin_Config;
import net.aelysium.aelysiummod.config.racas.Humano_Config;
import net.aelysium.aelysiummod.config.racas.Tiefling_Config;
import net.aelysium.aelysiummod.config.racas.Undyne_Config;
import net.aelysium.aelysiummod.config.racas.Valkyria_Config;
import net.aelysium.aelysiummod.eventos.LuaEstado;
import net.aelysium.aelysiummod.network.LuaVemelhaServidor;
import net.aelysium.aelysiummod.time.CorTimes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class AelysiumComandos {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        // Comando principal /aelysium
        event.getDispatcher().register(
                Commands.literal("aelysium")
                        .requires(source -> source.hasPermission(2))

                        // ==================== /aelysium chat ====================
                        .then(Commands.literal("chat")
                                // /aelysium chat raio
                                .then(Commands.literal("raio")
                                        .then(Commands.literal("verificar")
                                                .executes(ctx -> {
                                                    int radius = ChatConfig.getLocalChatRadius();
                                                    ctx.getSource().sendSuccess(() ->
                                                                    Component.literal("O raio do chat local é de " + radius + " blocos."),
                                                            false
                                                    );
                                                    return 1;
                                                })
                                        )
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
                                // /aelysium chat login
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
                                        .then(Commands.literal("status")
                                                .executes(ctx -> {
                                                    boolean enabled = ModConfig.areJoinLeaveMessagesEnabled();
                                                    String status = enabled ? "ativadas" : "desativadas";
                                                    ctx.getSource().sendSuccess(() ->
                                                                    Component.literal("Mensagens de login/logout estão " + status + "."),
                                                            false
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                                // /aelysium chat time
                                .then(Commands.literal("time")
                                        .then(Commands.argument("time", TeamArgument.team())
                                                // Jade integration
                                                .then(Commands.literal("jade")
                                                        .then(Commands.literal("ocultar")
                                                                .executes(CorTimes::hideTeamInJade)
                                                        )
                                                        .then(Commands.literal("mostrar")
                                                                .executes(CorTimes::showTeamInJade)
                                                        )
                                                        .then(Commands.literal("listar")
                                                                .executes(CorTimes::listHiddenTeams)
                                                        )
                                                )
                                                // Nome do time
                                                .then(Commands.literal("nome")
                                                        .then(Commands.literal("rgb")
                                                                .then(Commands.argument("vermelho", IntegerArgumentType.integer(0, 255))
                                                                        .then(Commands.argument("verde", IntegerArgumentType.integer(0, 255))
                                                                                .then(Commands.argument("azul", IntegerArgumentType.integer(0, 255))
                                                                                        .executes(CorTimes::setRgbColor)
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                        .then(Commands.literal("hex")
                                                                .then(Commands.argument("hexcode", StringArgumentType.string())
                                                                        .executes(CorTimes::setHexColor)
                                                                )
                                                        )
                                                        .then(Commands.literal("remover")
                                                                .executes(CorTimes::removeColor)
                                                        )
                                                        .then(Commands.literal("formatar")
                                                                .then(Commands.literal("negrito")
                                                                        .executes(CorTimes::toggleNameBold)
                                                                )
                                                                .then(Commands.literal("italico")
                                                                        .executes(CorTimes::toggleNameItalic)
                                                                )
                                                                .then(Commands.literal("sublinhado")
                                                                        .executes(CorTimes::toggleNameUnderlined)
                                                                )
                                                                .then(Commands.literal("riscado")
                                                                        .executes(CorTimes::toggleNameStrikethrough)
                                                                )
                                                                .then(Commands.literal("zalgo")
                                                                        .executes(CorTimes::toggleNameObfuscated)
                                                                )
                                                        )
                                                )
                                                // Prefixo do time
                                                .then(Commands.literal("prefixo")
                                                        .then(Commands.literal("rgb")
                                                                .then(Commands.argument("vermelho", IntegerArgumentType.integer(0, 255))
                                                                        .then(Commands.argument("verde", IntegerArgumentType.integer(0, 255))
                                                                                .then(Commands.argument("azul", IntegerArgumentType.integer(0, 255))
                                                                                        .executes(CorTimes::setPrefixRgbColor)
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                        .then(Commands.literal("hex")
                                                                .then(Commands.argument("hexcode", StringArgumentType.string())
                                                                        .executes(CorTimes::setPrefixHexColor)
                                                                )
                                                        )
                                                        .then(Commands.literal("remover")
                                                                .executes(CorTimes::removePrefixColor)
                                                        )
                                                        .then(Commands.literal("formatar")
                                                                .then(Commands.literal("negrito")
                                                                        .executes(CorTimes::togglePrefixBold)
                                                                )
                                                                .then(Commands.literal("italico")
                                                                        .executes(CorTimes::togglePrefixItalic)
                                                                )
                                                                .then(Commands.literal("sublinhado")
                                                                        .executes(CorTimes::togglePrefixUnderlined)
                                                                )
                                                                .then(Commands.literal("riscado")
                                                                        .executes(CorTimes::togglePrefixStrikethrough)
                                                                )
                                                                .then(Commands.literal("zalgo")
                                                                        .executes(CorTimes::togglePrefixObfuscated)
                                                                )
                                                        )
                                                )
                                                // Sufixo do time
                                                .then(Commands.literal("sufixo")
                                                        .then(Commands.literal("rgb")
                                                                .then(Commands.argument("vermelho", IntegerArgumentType.integer(0, 255))
                                                                        .then(Commands.argument("verde", IntegerArgumentType.integer(0, 255))
                                                                                .then(Commands.argument("azul", IntegerArgumentType.integer(0, 255))
                                                                                        .executes(CorTimes::setSuffixRgbColor)
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                        .then(Commands.literal("hex")
                                                                .then(Commands.argument("hexcode", StringArgumentType.string())
                                                                        .executes(CorTimes::setSuffixHexColor)
                                                                )
                                                        )
                                                        .then(Commands.literal("remover")
                                                                .executes(CorTimes::removeSuffixColor)
                                                        )
                                                        .then(Commands.literal("formatar")
                                                                .then(Commands.literal("negrito")
                                                                        .executes(CorTimes::toggleSuffixBold)
                                                                )
                                                                .then(Commands.literal("italico")
                                                                        .executes(CorTimes::toggleSuffixItalic)
                                                                )
                                                                .then(Commands.literal("sublinhado")
                                                                        .executes(CorTimes::toggleSuffixUnderlined)
                                                                )
                                                                .then(Commands.literal("riscado")
                                                                        .executes(CorTimes::toggleSuffixStrikethrough)
                                                                )
                                                                .then(Commands.literal("zalgo")
                                                                        .executes(CorTimes::toggleSuffixObfuscated)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )

                        // ==================== /aelysium lua ====================
                        .then(Commands.literal("lua")
                                .then(Commands.literal("alternar")
                                        .executes(ctx -> {
                                            LuaEstado.bloodMoon = !LuaEstado.bloodMoon;
                                            LuaVemelhaServidor packet = new LuaVemelhaServidor(LuaEstado.bloodMoon);
                                            PacketDistributor.sendToAllPlayers(packet);
                                            String status = LuaEstado.bloodMoon ? "ativada" : "desativada";
                                            ctx.getSource().sendSuccess(() ->
                                                            Component.literal("Lua vermelha " + status + "!"),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )

                        // ==================== /aelysium dimensao ====================
                        .then(Commands.literal("dimensao")
                                .then(Commands.literal("superplano")
                                        .executes(ctx -> {
                                            MinecraftServer server = ctx.getSource().getServer();
                                            server.getCommands().performPrefixedCommand(
                                                    ctx.getSource(),
                                                    "execute in aelysium:superplano run tp @s 0 16 0 180 0"
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("the_nether")
                                        .executes(ctx -> {
                                            MinecraftServer server = ctx.getSource().getServer();
                                            server.getCommands().performPrefixedCommand(
                                                    ctx.getSource(),
                                                    "execute in minecraft:the_nether run tp @s 0 128 0 180 0"
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("overworld")
                                        .executes(ctx -> {
                                            MinecraftServer server = ctx.getSource().getServer();
                                            server.getCommands().performPrefixedCommand(
                                                    ctx.getSource(),
                                                    "execute in minecraft:overworld run tp @s 0 128 0 180 0"
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("the_end")
                                        .executes(ctx -> {
                                            MinecraftServer server = ctx.getSource().getServer();
                                            server.getCommands().performPrefixedCommand(
                                                    ctx.getSource(),
                                                    "execute in minecraft:the_end run tp @s 0 64 68 180 0"
                                            );
                                            return 1;
                                        })
                                )
                        )

                        // ==================== /aelysium raca ====================
                        .then(Commands.literal("raca")
                                .then(Commands.literal("recarregar")
                                        .executes(ctx -> {
                                            Deus_Config.load(ctx.getSource().getServer());
                                            Dracono_Config.load(ctx.getSource().getServer());
                                            Elvarin_Config.load(ctx.getSource().getServer());
                                            Tiefling_Config.load(ctx.getSource().getServer());
                                            Undyne_Config.load(ctx.getSource().getServer());
                                            Humano_Config.load(ctx.getSource().getServer());
                                            Valkyria_Config.load(ctx.getSource().getServer());
                                            ctx.getSource().sendSuccess(() ->
                                                            Component.literal("Configurações de raças recarregadas!"),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("deus")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Deus_Config.DATA == null) {
                                                        Deus_Config.load(ctx.getSource().getServer());
                                                    }
                                                    return Deus.aplicar(ctx);
                                                })
                                        )
                                )
                                .then(Commands.literal("dracono")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Dracono_Config.DATA == null) {
                                                        Dracono_Config.load(ctx.getSource().getServer());
                                                    }
                                                    return Dracono.aplicar(ctx);
                                                })
                                        )
                                )
                                .then(Commands.literal("elvarin")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Elvarin_Config.DATA == null) {
                                                        Elvarin_Config.load(ctx.getSource().getServer());
                                                    }
                                                    return Elvarin.aplicar(ctx);
                                                })
                                        )
                                )
                                .then(Commands.literal("tiefling")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Tiefling_Config.DATA == null) {
                                                        Tiefling_Config.load(ctx.getSource().getServer());
                                                    }
                                                    return Tiefling.aplicar(ctx);
                                                })
                                        )
                                )
                                .then(Commands.literal("undyne")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Undyne_Config.DATA == null) {
                                                        Undyne_Config.load(ctx.getSource().getServer());
                                                    }
                                                    return Undyne.aplicar(ctx);
                                                })
                                        )
                                )
                                .then(Commands.literal("humano")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Humano_Config.DATA == null) {
                                                        Humano_Config.load(ctx.getSource().getServer());
                                                    }
                                                    return Humano.aplicar(ctx);
                                                })
                                        )
                                )
                                .then(Commands.literal("valkyria")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Valkyria_Config.DATA == null) {
                                                        Valkyria_Config.load(ctx.getSource().getServer());
                                                    }
                                                    return Valkyria.aplicar(ctx);
                                                })
                                        )
                                )
                                .then(Commands.literal("resetar")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(Resetar::aplicar)
                                        )
                                )
                        )
        );

        // ==================== /global ====================
        event.getDispatcher().register(
                Commands.literal("global")
                        .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                                .executes(GlobalChatCommand::sendGlobalMessage)
                        )
        );
    }
}