package net.aelysium.aelysiummod.sistemas.midia.holograma.comando;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.midia.holograma.GerenciadorHologramas;
import net.aelysium.aelysiummod.sistemas.midia.holograma.Holograma;
import net.aelysium.aelysiummod.sistemas.midia.holograma.TemporizadorHolograma;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class AcoesHologramaAjustes {

    static int setAtivo(CommandContext<CommandSourceStack> ctx, boolean ativo) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        holo.setAtivo(ativo);
        GerenciadorHologramas.getInstance().salvarHologramas();

        if (!ativo) {
            List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
            TemporizadorHolograma.destruirParaTodos(nome, players);
        }

        String status = ativo ? "§aativado" : "§cdesativado";
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.holograma", nome, status), true);
        return 1;
    }

    static int setRange(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        double range = DoubleArgumentType.getDouble(ctx, "blocos");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        holo.setRangeVisibilidade(range);
        GerenciadorHologramas.getInstance().salvarHologramas();

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.range_de_definido_para_blocos", nome, range), true);
        return 1;
    }

    static int setIntervalo(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        holo.setIntervaloAtualizacao(ticks);
        GerenciadorHologramas.getInstance().salvarHologramas();

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.intervalo_de_definido_para_ticks", nome, ticks), true);
        return 1;
    }
}
