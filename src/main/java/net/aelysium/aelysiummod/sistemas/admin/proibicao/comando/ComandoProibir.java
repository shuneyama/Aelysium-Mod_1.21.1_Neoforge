package net.aelysium.aelysiummod.sistemas.admin.proibicao.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.admin.proibicao.MensagemFalhaFalsa;
import net.aelysium.aelysiummod.sistemas.admin.proibicao.DadosProibicao;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.aelysium.aelysiummod.nucleo.servico.RegistroServicos;

public class ComandoProibir {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("proibir")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("jogador")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .executes(ComandoProibir::toggleJogador)))

                .then(Commands.literal("mod")
                        .then(Commands.argument("mod_id", StringArgumentType.word())
                                .executes(ComandoProibir::toggleMod)))

                .then(Commands.literal("dimensao")
                        .then(Commands.argument("dimensao", DimensionArgument.dimension())
                                .executes(ComandoProibir::toggleDimensao)));
    }

    private static int toggleJogador(CommandContext<CommandSourceStack> ctx) {
        String nome = StringArgumentType.getString(ctx, "nome");
        MinecraftServer server = ctx.getSource().getServer();
        DadosProibicao data = DadosProibicao.get(server);

        if (data.estaProibido(nome)) {
            data.desproibir(nome);
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.proibicao.jogador_foi_desbanido", nome), true);
        } else {
            data.proibir(nome);
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.proibicao.jogador_foi_banido", nome), true);

            ServerPlayer online = server.getPlayerList().getPlayerByName(nome);
            if (online != null) {
                online.connection.disconnect(MensagemFalhaFalsa.gerar());
            }
        }
        return 1;
    }

    private static int toggleDimensao(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceKey<Level> dimensao = DimensionArgument.getDimension(ctx, "dimensao").dimension();
        String id = dimensao.location().toString();
        MinecraftServer server = ctx.getSource().getServer();
        DadosProibicao data = DadosProibicao.get(server);

        if (data.dimensaoProibida(id)) {
            data.desproibirDimensao(id);
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.proibicao.a_dimensao_nao_esta_mais_proibida", id), true);
        } else {
            data.proibirDimensao(id);
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.proibicao.a_dimensao_agora_esta_proibida_so_e", id), true);
        }
        return 1;
    }

    private static int toggleMod(CommandContext<CommandSourceStack> ctx) {
        String modId = StringArgumentType.getString(ctx, "mod_id").toLowerCase();
        boolean removeu = RegistroServicos.listaNegraMods().alternar(modId);
        if (removeu) {
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.proibicao.mod_nao_esta_mais_proibido", modId), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.proibicao.mod_agora_esta_proibido_no_servidor", modId), true);
        }
        return 1;
    }
}
