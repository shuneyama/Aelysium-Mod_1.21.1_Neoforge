package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.network.AelysiumNetwork;
import net.aelysium.aelysiummod.nickname.NicknameData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NickCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("nick")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("jogador", EntityArgument.player())
                        .then(Commands.literal("modificar")
                                .then(Commands.argument("nome", StringArgumentType.greedyString())
                                        .executes(NickCommand::executeModificar)
                                )
                        )
                        .then(Commands.literal("resetar")
                                .executes(NickCommand::executeResetar)
                        )
                        .then(Commands.literal("editor")
                                .executes(NickCommand::executeEditor)
                        )
                );
    }

    private static int executeModificar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
        String rawNick = StringArgumentType.getString(ctx, "nome").trim();

        if (rawNick.startsWith("\"") && rawNick.endsWith("\"") && rawNick.length() > 1) {
            rawNick = rawNick.substring(1, rawNick.length() - 1).trim();
        } else if (rawNick.startsWith("\"")) {
            rawNick = rawNick.substring(1).trim();
        }

        if (rawNick.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cO nickname não pode ser vazio!"));
            return 0;
        }

        if (rawNick.length() > 24) {
            ctx.getSource().sendFailure(Component.literal("§cO nickname não pode ter mais de 24 caracteres!"));
            return 0;
        }

        NicknameData data = NicknameData.get(ctx.getSource().getServer());

        if (data.isNickTaken(rawNick, target.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("§cO nickname §e" + rawNick + "§c já está em uso!"));
            return 0;
        }

        data.setNickSimple(target.getUUID(), rawNick);
        data.syncToAll(ctx.getSource().getServer(), target.getUUID());
        refreshTabList(target);
        refreshCommandTree(ctx.getSource().getServer());

        MutableComponent styled = data.getStyledFullName(target.getUUID());
        MutableComponent feedback = Component.literal("§aNickname de §b" + target.getName().getString() + "§a alterado para ");
        if (styled != null) feedback.append(styled);
        feedback.append(Component.literal("§a."));
        ctx.getSource().sendSuccess(() -> feedback, true);
        return 1;
    }

    private static int executeResetar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
        NicknameData data = NicknameData.get(ctx.getSource().getServer());

        if (!data.hasNickname(target.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("§c" + target.getName().getString() + " não possui um nickname."));
            return 0;
        }

        String oldNick = data.getNickname(target.getUUID()).nick();
        data.removeNickname(target.getUUID());
        data.syncToAll(ctx.getSource().getServer(), target.getUUID());
        refreshTabList(target);
        refreshCommandTree(ctx.getSource().getServer());

        ctx.getSource().sendSuccess(
                () -> Component.literal("§aNickname §e" + oldNick + "§a removido de §b" + target.getName().getString() + "§a."),
                true
        );
        return 1;
    }

    private static int executeEditor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");

        ServerPlayer executor = ctx.getSource().getPlayerOrException();

        NicknameData data = NicknameData.get(ctx.getSource().getServer());
        NicknameData.NicknameEntry entry = data.getNickname(target.getUUID());

        AelysiumNetwork.NickEditorOpenPacket packet;
        if (entry != null) {
            packet = new AelysiumNetwork.NickEditorOpenPacket(
                    target.getUUID(),
                    target.getGameProfile().getName(),
                    entry.prefix(), entry.prefixCor1(), entry.prefixCor2(), entry.prefixFormat(),
                    entry.nick(), entry.nickCor(), entry.nickFormat(),
                    entry.suffix(), entry.suffixCor1(), entry.suffixCor2(), entry.suffixFormat()
            );
        } else {
            packet = new AelysiumNetwork.NickEditorOpenPacket(
                    target.getUUID(),
                    target.getGameProfile().getName(),
                    "", 0xFFFFFF, -1, 0,
                    target.getGameProfile().getName(), 0xFFFFFF, 0,
                    "", 0xFFFFFF, -1, 0
            );
        }

        PacketDistributor.sendToPlayer(executor, packet);
        return 1;
    }

    public static void refreshCommandTree(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(player);
        }
    }

    public static void refreshTabList(ServerPlayer target) {
        target.refreshDisplayName();
        target.refreshTabListName();
        var packet = new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                target
        );
        for (ServerPlayer player : target.getServer().getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }
}