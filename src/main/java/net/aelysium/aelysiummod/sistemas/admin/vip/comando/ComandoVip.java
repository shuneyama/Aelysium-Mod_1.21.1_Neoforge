package net.aelysium.aelysiummod.sistemas.admin.vip.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.aelysium.aelysiummod.registro.ModItens;
import net.aelysium.aelysiummod.sistemas.admin.vip.DadosVipJogadores;
import net.aelysium.aelysiummod.sistemas.admin.vip.GerenciadorVips;
import net.aelysium.aelysiummod.sistemas.admin.vip.MenuEdicaoVip;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ComandoVip {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("vip")
                .requires(origem -> origem.hasPermission(2))
                .then(Commands.literal("adquirir")
                        .then(argumentoVipExistente().executes(ComandoVip::executarAdquirir)))
                .then(Commands.literal("criar")
                        .then(Commands.argument("vip", StringArgumentType.greedyString()).executes(ComandoVip::executarCriar)))
                .then(Commands.literal("editar")
                        .then(argumentoVipExistente().executes(ComandoVip::executarEditar)))
                .then(Commands.literal("remover")
                        .then(argumentoVipExistente().executes(ComandoVip::executarRemover)))
                .then(Commands.literal("revogar")
                        .then(Commands.argument("jogadores", EntityArgument.players())
                                .then(argumentoVipExistente().executes(ComandoVip::executarRevogar))))
                .then(Commands.literal("ver")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ComandoVip::executarVer)));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> argumentoVipExistente() {
        return Commands.argument("vip", StringArgumentType.greedyString())
                .suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(
                        GerenciadorVips.listar(ctx.getSource().getServer()), construtor));
    }

    private static String nomeVip(CommandContext<CommandSourceStack> ctx) {
        return StringArgumentType.getString(ctx, "vip").trim().replaceAll("\\s+", " ");
    }

    private static int executarAdquirir(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer jogador = ctx.getSource().getPlayerOrException();
        MinecraftServer servidor = ctx.getSource().getServer();
        String nome = nomeVip(ctx);

        if (!GerenciadorVips.existe(servidor, nome)) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.vip.vip_inexistente", nome));
            return 0;
        }

        List<ItemStack> itens = GerenciadorVips.carregar(servidor, nome);
        if (itens.stream().allMatch(ItemStack::isEmpty)) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.vip.vip_vazio", nome));
            return 0;
        }

        ItemStack sacola = new ItemStack(ModItens.SACOLA_VIP.get());
        sacola.set(ModComponentesDados.SACOLA_VIP_CONTEUDO.get(), ItemContainerContents.fromItems(itens));
        sacola.set(ModComponentesDados.SACOLA_VIP_NOME.get(), nome);
        sacola.set(DataComponents.ITEM_NAME, Component.translatableEscape("item.aelysiummod.sacola_vip.nomeada", nome));

        if (!jogador.getInventory().add(sacola)) {
            jogador.drop(sacola, false);
        }
        DadosVipJogadores.get(servidor).conceder(jogador.getUUID(), nome);

        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.vip.vip_adquirido", nome), true);
        return 1;
    }

    private static int executarCriar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer jogador = ctx.getSource().getPlayerOrException();
        MinecraftServer servidor = ctx.getSource().getServer();
        String nome = nomeVip(ctx);

        if (!GerenciadorVips.nomeValido(nome)) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.vip.nome_invalido"));
            return 0;
        }
        if (GerenciadorVips.existe(servidor, nome)) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.vip.vip_ja_existe", nome));
            return 0;
        }

        abrirEditor(jogador, nome, new SimpleContainer(GerenciadorVips.TAMANHO));
        return 1;
    }

    private static int executarEditar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer jogador = ctx.getSource().getPlayerOrException();
        MinecraftServer servidor = ctx.getSource().getServer();
        String nome = nomeVip(ctx);

        if (!GerenciadorVips.existe(servidor, nome)) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.vip.vip_inexistente", nome));
            return 0;
        }

        SimpleContainer container = new SimpleContainer(GerenciadorVips.TAMANHO);
        List<ItemStack> itens = GerenciadorVips.carregar(servidor, nome);
        for (int i = 0; i < itens.size(); i++) {
            container.setItem(i, itens.get(i));
        }

        abrirEditor(jogador, nome, container);
        return 1;
    }

    private static int executarRemover(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer servidor = ctx.getSource().getServer();
        String nome = nomeVip(ctx);

        if (!GerenciadorVips.existe(servidor, nome)) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.vip.vip_inexistente", nome));
            return 0;
        }
        if (!GerenciadorVips.remover(servidor, nome)) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.vip.falha_ao_remover", nome));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.vip.vip_removido", nome), true);
        return 1;
    }

    private static int executarRevogar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> alvos = EntityArgument.getPlayers(ctx, "jogadores");
        DadosVipJogadores dados = DadosVipJogadores.get(ctx.getSource().getServer());
        String nome = nomeVip(ctx);
        int revogados = 0;
        for (ServerPlayer alvo : alvos) {
            if (dados.revogar(alvo.getUUID(), nome)) revogados++;
        }
        int total = revogados;
        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.vip.vip_revogado", nome, total), true);
        return total;
    }

    private static int executarVer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer alvo = EntityArgument.getPlayer(ctx, "jogador");
        Set<String> vips = DadosVipJogadores.get(ctx.getSource().getServer()).vips(alvo.getUUID());
        String nomeJogador = alvo.getName().getString();
        if (vips.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.vip.jogador_sem_vip", nomeJogador), false);
        } else {
            String lista = String.join(", ", vips);
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.vip.vips_do_jogador", nomeJogador, lista), false);
        }
        return vips.size();
    }

    private static void abrirEditor(ServerPlayer jogador, String nome, SimpleContainer container) {
        jogador.openMenu(new SimpleMenuProvider(
                (idContainer, inventario, p) -> new MenuEdicaoVip(idContainer, inventario, container, nome),
                Component.translatableEscape("gui.aelysiummod.vip.titulo_editor", nome)
        ));
    }
}
