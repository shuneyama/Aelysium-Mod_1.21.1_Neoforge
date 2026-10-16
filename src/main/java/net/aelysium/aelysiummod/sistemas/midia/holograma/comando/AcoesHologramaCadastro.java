package net.aelysium.aelysiummod.sistemas.midia.holograma.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.midia.holograma.GerenciadorHologramas;
import net.aelysium.aelysiummod.sistemas.midia.holograma.Holograma;
import net.aelysium.aelysiummod.sistemas.midia.holograma.TemporizadorHolograma;
import net.aelysium.aelysiummod.sistemas.midia.holograma.LinhaHolograma;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class AcoesHologramaCadastro {

    static int criarHolograma(CommandContext<CommandSourceStack> ctx, String texto) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        if (GerenciadorHologramas.getInstance().existeHolograma(nome)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.ja_existe_um_holograma_com_o_nome", nome));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.holograma.esse_comando_so_pode_ser_executado_por"));
            return 0;
        }

        String mundo = player.level().dimension().location().toString();
        Holograma holo = new Holograma(nome, mundo, player.getX(), player.getY() + 2.0, player.getZ());

        if (texto != null && !texto.isEmpty()) {
            holo.adicionarLinha(texto);
        } else {
            holo.adicionarLinha("§7Holograma: §f" + nome);
        }

        GerenciadorHologramas.getInstance().adicionarHolograma(holo);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_criado_com_sucesso", nome), true);
        return 1;
    }

    static int deletarHolograma(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        if (!GerenciadorHologramas.getInstance().existeHolograma(nome)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        TemporizadorHolograma.destruirParaTodos(nome, players);

        GerenciadorHologramas.getInstance().removerHolograma(nome);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_deletado", nome), true);
        return 1;
    }

    static int renomear(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        String novoNome = StringArgumentType.getString(ctx, "novoNome");

        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();

        if (!gerenciador.existeHolograma(nome)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        if (gerenciador.existeHolograma(novoNome)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.ja_existe_um_holograma_com_o_nome", novoNome));
            return 0;
        }

        Holograma holo = gerenciador.getHolograma(nome);

        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        TemporizadorHolograma.destruirParaTodos(nome, players);

        gerenciador.removerHolograma(nome);
        holo.setNome(novoNome);
        gerenciador.adicionarHolograma(holo);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_renomeado_para", nome, novoNome), true);
        return 1;
    }

    static int clonar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        String novoNome = StringArgumentType.getString(ctx, "novoNome");

        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();

        Holograma original = gerenciador.getHolograma(nome);
        if (original == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        if (gerenciador.existeHolograma(novoNome)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.ja_existe_um_holograma_com_o_nome", novoNome));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        double x, y, z;
        String mundo;
        if (player != null) {
            x = player.getX();
            y = player.getY() + 2.0;
            z = player.getZ();
            mundo = player.level().dimension().location().toString();
        } else {
            x = original.getX();
            y = original.getY();
            z = original.getZ();
            mundo = original.getMundo();
        }

        Holograma clone = new Holograma(novoNome, mundo, x, y, z);
        clone.setAtivo(original.isAtivo());
        clone.setRangeVisibilidade(original.getRangeVisibilidade());
        clone.setIntervaloAtualizacao(original.getIntervaloAtualizacao());

        for (LinhaHolograma linha : original.getLinhas()) {
            clone.getLinhas().add(new LinhaHolograma(linha.getConteudo(), linha.getOffsetY()));
        }

        gerenciador.adicionarHolograma(clone);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_clonado_como", nome, novoNome), true);
        return 1;
    }
}
