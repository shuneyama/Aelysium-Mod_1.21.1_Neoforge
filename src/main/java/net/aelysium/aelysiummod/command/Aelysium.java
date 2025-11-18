package net.aelysium.aelysiummod.command;

import net.aelysium.aelysiummod.command.racas.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class Aelysium {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("aelysium")
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
