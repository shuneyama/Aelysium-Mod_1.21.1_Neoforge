package net.aelysium.aelysiummod.sistemas.social.npc.comando;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ComandoNpc {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("npc")
                .executes(ctx -> listar(ctx.getSource()));
    }

    public static void registrarSubComandosAcoes(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("debug")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("_npceditar")
                                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                                        .executes(ctx -> editar(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id")))))
                        .then(Commands.literal("_npcteleportar")
                                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                                        .executes(ctx -> teleportar(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id")))))
                        .then(Commands.literal("_npcremover")
                                .then(Commands.argument("id", IntegerArgumentType.integer(1))
                                        .executes(ctx -> remover(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "id"))))));
    }

    private static int listar(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.npc.npcs_no_mundo"), false);

        int count = 0;
        for (var entity : level.getAllEntities()) {
            if (!(entity instanceof EntidadeNpcPersonalizado npc)) continue;
            count++;
            int id = npc.getNpcId();
            MutableComponent linha = Component.translatableEscape(
                    "mensagem.aelysiummod.npc.item_lista", id, npc.getNpcName());
            linha.append(Component.literal("  "));
            linha.append(botao("mensagem.aelysiummod.npc.botao_editar", "mensagem.aelysiummod.npc.clique_editar",
                    "/debug _npceditar " + id));
            linha.append(Component.literal(" "));
            linha.append(botao("mensagem.aelysiummod.npc.botao_teleportar", "mensagem.aelysiummod.npc.clique_teleportar",
                    "/debug _npcteleportar " + id));
            linha.append(Component.literal(" "));
            linha.append(botao("mensagem.aelysiummod.npc.botao_remover", "mensagem.aelysiummod.npc.clique_remover",
                    "/debug _npcremover " + id));
            source.sendSuccess(() -> linha, false);
        }

        final int total = count;
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.npc.total_npcs", total), false);
        return count;
    }

    private static MutableComponent botao(String chaveTexto, String chaveDica, String comando) {
        return Component.translatable(chaveTexto).withStyle(estilo -> estilo
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(chaveDica))));
    }

    private static EntidadeNpcPersonalizado procurar(CommandSourceStack source, int id) {
        ServerLevel atual = source.getLevel();
        for (var entity : atual.getAllEntities()) {
            if (entity instanceof EntidadeNpcPersonalizado npc && npc.getNpcId() == id) return npc;
        }
        for (ServerLevel outro : source.getServer().getAllLevels()) {
            if (outro == atual) continue;
            for (var entity : outro.getAllEntities()) {
                if (entity instanceof EntidadeNpcPersonalizado npc && npc.getNpcId() == id) return npc;
            }
        }
        return null;
    }

    private static int editar(CommandSourceStack source, int id) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.nucleo_comando.apenas_jogadores_podem_usar_este"));
            return 0;
        }
        EntidadeNpcPersonalizado npc = procurar(source, id);
        if (npc == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.npc.npc_nao_encontrado", id));
            return 0;
        }
        PacotesNpc.sendOpenEditor(player, npc);
        return 1;
    }

    private static int teleportar(CommandSourceStack source, int id) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.nucleo_comando.apenas_jogadores_podem_usar_este"));
            return 0;
        }
        EntidadeNpcPersonalizado npc = procurar(source, id);
        if (npc == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.npc.npc_nao_encontrado", id));
            return 0;
        }
        player.teleportTo((ServerLevel) npc.level(), npc.getX(), npc.getY(), npc.getZ(), player.getYRot(), player.getXRot());
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.npc.teleportado_para", npc.getNpcName(), npc.getNpcId()), true);
        return 1;
    }

    private static int remover(CommandSourceStack source, int id) {
        EntidadeNpcPersonalizado npc = procurar(source, id);
        if (npc == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.npc.npc_nao_encontrado", id));
            return 0;
        }
        String nome = npc.getNpcName();
        npc.discard();
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.npc.npc_removido", nome), true);
        return 1;
    }
}
