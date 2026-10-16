package net.aelysium.aelysiummod.sistemas.admin.banimentos.comando;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class ComandosModsDesativados {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("capar-mod")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("set")
                        .then(Commands.argument("mod_id", StringArgumentType.word())
                                .then(Commands.argument("loot", BoolArgumentType.bool())
                                        .then(Commands.argument("trocas", BoolArgumentType.bool())
                                                .executes(ComandosModsDesativados::set)
                                                .then(Commands.argument("receitas", BoolArgumentType.bool())
                                                        .then(Commands.argument("worldgen", BoolArgumentType.bool())
                                                                .then(Commands.argument("conquistas", BoolArgumentType.bool())
                                                                        .executes(ComandosModsDesativados::set))))))))

                .then(Commands.literal("remover")
                        .then(Commands.argument("mod_id", StringArgumentType.word())
                                .executes(ComandosModsDesativados::remover)))

                .then(Commands.literal("listar")
                        .executes(ComandosModsDesativados::listar))

                .then(Commands.literal("reload")
                        .executes(ComandosModsDesativados::reload));
    }

    private static int set(CommandContext<CommandSourceStack> ctx) {
        String modId = StringArgumentType.getString(ctx, "mod_id").toLowerCase();
        boolean loot = BoolArgumentType.getBool(ctx, "loot");
        boolean trocas = BoolArgumentType.getBool(ctx, "trocas");

        boolean completo = ctx.getNodes().stream().anyMatch(n -> n.getNode().getName().equals("conquistas"));
        if (completo) {
            ConfiguracaoModsDesativados.setCap(modId, loot, trocas,
                    BoolArgumentType.getBool(ctx, "receitas"), BoolArgumentType.getBool(ctx, "worldgen"), BoolArgumentType.getBool(ctx, "conquistas"));
        } else {
            ConfiguracaoModsDesativados.setCap(modId, loot, trocas);
        }
        net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());

        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.aelysium_mod_capado_loot_trocas", modId, loot, trocas), true);

        if (trocas) {
            ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.banimentos.trocas_do_mercador_errante_vilaroes_so"), false);
        }

        return 1;
    }

    private static int remover(CommandContext<CommandSourceStack> ctx) {
        String modId = StringArgumentType.getString(ctx, "mod_id").toLowerCase();

        if (ConfiguracaoModsDesativados.removeCap(modId)) {
            net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.aelysium_mod_nao_esta_mais_capado", modId), true);
            return 1;
        }

        ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.mod_nao_esta_na_lista_de_capados", modId)
                .withStyle(ChatFormatting.RED));
        return 0;
    }

    private static int listar(CommandContext<CommandSourceStack> ctx) {
        Map<String, ConfiguracaoModsDesativados.ModDesativado> todos = ConfiguracaoModsDesativados.getAll();

        if (todos.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.banimentos.nenhum_mod_capado_no_momento"), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.mods_capados", todos.size()), false);
        for (Map.Entry<String, ConfiguracaoModsDesativados.ModDesativado> entry : todos.entrySet()) {
            ConfiguracaoModsDesativados.ModDesativado cap = entry.getValue();
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.loot_trocas", entry.getKey(), cap.loot, cap.trocas), false);
            ctx.getSource().sendSuccess(() -> Component.literal("    receitas=" + cap.receitas + ", worldgen=" + cap.worldgen + ", conquistas=" + cap.conquistas), false);
        }
        return todos.size();
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ConfiguracaoModsDesativados.load();
        net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());
        int count = ConfiguracaoModsDesativados.getAll().size();
        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.aelysium_config_de_mods_capados", count), true);
        return 1;
    }
}
