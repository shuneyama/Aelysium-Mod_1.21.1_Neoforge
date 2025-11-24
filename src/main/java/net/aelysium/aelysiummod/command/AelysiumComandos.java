package net.aelysium.aelysiummod.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.aelysium.aelysiummod.command.chat.CorTimes;
import net.aelysium.aelysiummod.command.racas.*;
import net.aelysium.aelysiummod.config.ModConfig;
import net.aelysium.aelysiummod.network.LuaVemelhaServidor;
import net.aelysium.aelysiummod.system.LuaEstado;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class AelysiumComandos {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("aelysium")
                        .then(Commands.literal("chat")
                                .then(Commands.literal("ligar")
                                        .executes(ctx -> {
                                            ModConfig.setJoinLeaveMessagesEnabled(true);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("§aMensagens de entrada/saída ATIVADAS"),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("desligar")
                                        .executes(ctx -> {
                                            ModConfig.setJoinLeaveMessagesEnabled(false);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("§cMensagens de entrada/saída DESATIVADAS"),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("status")
                                        .executes(ctx -> {
                                            boolean enabled = ModConfig.areJoinLeaveMessagesEnabled();
                                            String status = enabled ? "§aATIVADAS" : "§cDESATIVADAS";
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Mensagens de entrada/saída estão: " + status),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("teamcolor")
                                .then(Commands.argument("team", TeamArgument.team())
                                        .then(Commands.literal("rgb")
                                                .then(Commands.argument("red", IntegerArgumentType.integer(0, 255))
                                                        .then(Commands.argument("green", IntegerArgumentType.integer(0, 255))
                                                                .then(Commands.argument("blue", IntegerArgumentType.integer(0, 255))
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
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("team", TeamArgument.team())
                                                .executes(CorTimes::removeColor)
                                        )
                                )
                        )

                        .then(Commands.literal("lua")
                                .executes(ctx -> {
                                    LuaEstado.bloodMoon = !LuaEstado.bloodMoon;

                                    LuaVemelhaServidor packet = new LuaVemelhaServidor(LuaEstado.bloodMoon);
                                    PacketDistributor.sendToAllPlayers(packet);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Lua de Sangue: " + LuaEstado.bloodMoon),
                                            true
                                    );
                                    return 1;
                                }))

                        .then(Commands.literal("dimensão")
                                .then(Commands.literal("superplano")
                                        .executes(ctx -> {
                                            var server = ctx.getSource().getServer();
                                            server.getCommands().performPrefixedCommand(
                                                    ctx.getSource(),
                                                    "execute in aelysium:superplano run tp @s 0 16 0 180 0"
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("the_nether")
                                        .executes(ctx -> {
                                            var server = ctx.getSource().getServer();
                                            server.getCommands().performPrefixedCommand(
                                                    ctx.getSource(),
                                                    "execute in minecraft:the_nether run tp @s 0 128 0 180 0"
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("overworld")
                                        .executes(ctx -> {
                                            var server = ctx.getSource().getServer();
                                            server.getCommands().performPrefixedCommand(
                                                    ctx.getSource(),
                                                    "execute in minecraft:overworld run tp @s 0 128 0 180 0"
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("the_end")
                                        .executes(ctx -> {
                                            var server = ctx.getSource().getServer();
                                            server.getCommands().performPrefixedCommand(
                                                    ctx.getSource(),
                                                    "execute in minecraft:the_end run tp @s 0 64 68 180 0"
                                            );
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("raças")
                                .then(Commands.literal("recarregar")
                                        .executes(ctx -> {
                                            Deus_Config.load(ctx.getSource().getServer());
                                            Dracono_Config.load(ctx.getSource().getServer());
                                            Elvarin_Config.load(ctx.getSource().getServer());
                                            Tiefling_Config.load(ctx.getSource().getServer());
                                            Undyne_Config.load(ctx.getSource().getServer());
                                            Humano_Config.load(ctx.getSource().getServer());
                                            Valkyria_Config.load(ctx.getSource().getServer());
                                            ctx.getSource().sendSuccess(() -> Component.literal("Configs recarregadas!"), true);
                                            return 1;
                                        }))
                                .then(Commands.literal("deus")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Deus_Config.DATA == null)
                                                        Deus_Config.load(ctx.getSource().getServer());

                                                    return Deus.aplicar(ctx);
                                                })))
                                .then(Commands.literal("dracono")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Dracono_Config.DATA == null)
                                                        Dracono_Config.load(ctx.getSource().getServer());

                                                    return Dracono.aplicar(ctx);
                                                })))
                                .then(Commands.literal("elvarin")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Elvarin_Config.DATA == null)
                                                        Elvarin_Config.load(ctx.getSource().getServer());

                                                    return Elvarin.aplicar(ctx);
                                                })))
                                .then(Commands.literal("tiefling")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Tiefling_Config.DATA == null)
                                                        Tiefling_Config.load(ctx.getSource().getServer());

                                                    return Tiefling.aplicar(ctx);
                                                })))
                                .then(Commands.literal("undyne")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Undyne_Config.DATA == null)
                                                        Undyne_Config.load(ctx.getSource().getServer());

                                                    return Undyne.aplicar(ctx);
                                                })))
                                .then(Commands.literal("humano")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Humano_Config.DATA == null)
                                                        Humano_Config.load(ctx.getSource().getServer());

                                                    return Humano.aplicar(ctx);
                                                })))
                                .then(Commands.literal("valkyria")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    if (Valkyria_Config.DATA == null)
                                                        Valkyria_Config.load(ctx.getSource().getServer());

                                                    return Valkyria.aplicar(ctx);
                                                })))
                                .then(Commands.literal("resetar")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(Resetar::aplicar)))
                        ));
    }
}
