package net.aelysium.aelysiummod.sistemas.midia.holograma.comando;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.midia.holograma.GerenciadorHologramas;
import net.aelysium.aelysiummod.sistemas.midia.holograma.Holograma;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AcoesHologramaPosicao {

    static int moverHolograma(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        double x = DoubleArgumentType.getDouble(ctx, "x");
        double y = DoubleArgumentType.getDouble(ctx, "y");
        double z = DoubleArgumentType.getDouble(ctx, "z");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        holo.setPosicao(x, y, z);
        GerenciadorHologramas.getInstance().salvarHologramas();
        UtilitariosComandoHolograma.atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_movido_para", nome, String.format("%.2f", x), String.format("%.2f", y), String.format("%.2f", z)), true);
        return 1;
    }

    static int moverAqui(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.holograma.esse_comando_so_pode_ser_executado_por"));
            return 0;
        }

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        String mundo = player.level().dimension().location().toString();
        holo.setPosicao(mundo, player.getX(), player.getY() + 2.0, player.getZ());
        GerenciadorHologramas.getInstance().salvarHologramas();
        UtilitariosComandoHolograma.atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_movido_para_sua_posicao", nome), true);
        return 1;
    }

    static int teleportar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.holograma.esse_comando_so_pode_ser_executado_por"));
            return 0;
        }

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        player.teleportTo(holo.getX(), holo.getY(), holo.getZ());
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.teleportado_para", nome), true);
        return 1;
    }

    static int alinhar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        String outroNome = StringArgumentType.getString(ctx, "outroNome");
        String eixo = StringArgumentType.getString(ctx, "eixo").toLowerCase();

        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();

        Holograma holo = gerenciador.getHolograma(nome);
        Holograma outro = gerenciador.getHolograma(outroNome);

        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }
        if (outro == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", outroNome));
            return 0;
        }

        double x = holo.getX(), y = holo.getY(), z = holo.getZ();

        switch (eixo) {
            case "x" -> x = outro.getX();
            case "y" -> y = outro.getY();
            case "z" -> z = outro.getZ();
            case "xz" -> { x = outro.getX(); z = outro.getZ(); }
            default -> {
                source.sendFailure(Component.translatable("mensagem.aelysiummod.holograma.eixo_invalido_use_x_y_z_ou_xz"));
                return 0;
            }
        }

        holo.setPosicao(x, y, z);
        gerenciador.salvarHologramas();
        UtilitariosComandoHolograma.atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_alinhado_com_no_eixo", nome, outroNome, eixo), true);
        return 1;
    }
}
