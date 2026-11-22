package net.aelysium.aelysiummod.sistemas.mundo.bordas;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

import java.util.Locale;

public final class ComandoBorda {

    private interface Resolvedor {
        ServerLevel de(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }

    private ComandoBorda() {
    }

    public static void registrar(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.getRoot().getChildren().removeIf(no -> no.getName().equals("worldborder"));
        LiteralArgumentBuilder<CommandSourceStack> raiz = Commands.literal("worldborder").requires(source -> source.hasPermission(2));
        acoes(raiz, ctx -> ctx.getSource().getLevel());
        raiz.then(Commands.literal("dimension")
                .then(acoes(Commands.argument("dimensao", DimensionArgument.dimension()), ctx -> DimensionArgument.getDimension(ctx, "dimensao"))));
        raiz.then(Commands.literal("list").executes(ComandoBorda::listar));
        dispatcher.register(raiz);
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T acoes(T no, Resolvedor r) {
        return no
                .then(Commands.literal("get").executes(ctx -> obter(ctx, r.de(ctx))))
                .then(Commands.literal("set")
                        .then(Commands.argument("distancia", DoubleArgumentType.doubleArg(1.0, WorldBorder.MAX_SIZE))
                                .executes(ctx -> ajustar(ctx, r.de(ctx), DoubleArgumentType.getDouble(ctx, "distancia"), 0))
                                .then(Commands.argument("tempo", IntegerArgumentType.integer(0))
                                        .executes(ctx -> ajustar(ctx, r.de(ctx), DoubleArgumentType.getDouble(ctx, "distancia"), IntegerArgumentType.getInteger(ctx, "tempo"))))))
                .then(Commands.literal("add")
                        .then(Commands.argument("distancia", DoubleArgumentType.doubleArg(-WorldBorder.MAX_SIZE, WorldBorder.MAX_SIZE))
                                .executes(ctx -> ajustar(ctx, r.de(ctx), r.de(ctx).getWorldBorder().getLerpTarget() + DoubleArgumentType.getDouble(ctx, "distancia"), 0))
                                .then(Commands.argument("tempo", IntegerArgumentType.integer(0))
                                        .executes(ctx -> ajustar(ctx, r.de(ctx), r.de(ctx).getWorldBorder().getLerpTarget() + DoubleArgumentType.getDouble(ctx, "distancia"), IntegerArgumentType.getInteger(ctx, "tempo"))))))
                .then(Commands.literal("center")
                        .then(Commands.argument("pos", Vec2Argument.vec2())
                                .executes(ctx -> centro(ctx, r.de(ctx), Vec2Argument.getVec2(ctx, "pos")))))
                .then(Commands.literal("damage")
                        .then(Commands.literal("amount")
                                .then(Commands.argument("dano", DoubleArgumentType.doubleArg(0.0))
                                        .executes(ctx -> dano(ctx, r.de(ctx), DoubleArgumentType.getDouble(ctx, "dano")))))
                        .then(Commands.literal("buffer")
                                .then(Commands.argument("distancia", DoubleArgumentType.doubleArg(0.0))
                                        .executes(ctx -> zonaSegura(ctx, r.de(ctx), DoubleArgumentType.getDouble(ctx, "distancia"))))))
                .then(Commands.literal("warning")
                        .then(Commands.literal("distance")
                                .then(Commands.argument("distancia", IntegerArgumentType.integer(0))
                                        .executes(ctx -> avisoDistancia(ctx, r.de(ctx), IntegerArgumentType.getInteger(ctx, "distancia")))))
                        .then(Commands.literal("time")
                                .then(Commands.argument("tempo", IntegerArgumentType.integer(0))
                                        .executes(ctx -> avisoTempo(ctx, r.de(ctx), IntegerArgumentType.getInteger(ctx, "tempo"))))));
    }

    private static String nome(ServerLevel nivel) {
        return nivel.dimension().location().toString();
    }

    private static String numero(double valor) {
        return String.format(Locale.ROOT, "%.1f", valor);
    }

    private static int obter(CommandContext<CommandSourceStack> ctx, ServerLevel nivel) {
        double tamanho = nivel.getWorldBorder().getSize();
        ctx.getSource().sendSuccess(() -> Component.literal("§aA borda de §f" + nome(nivel) + " §atem §f" + numero(tamanho) + " §ablocos de largura"), false);
        return (int) Math.floor(tamanho + 0.5);
    }

    private static int listar(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("§aBordas por dimensão:"), false);
        for (ServerLevel nivel : ctx.getSource().getServer().getAllLevels()) {
            WorldBorder borda = nivel.getWorldBorder();
            ctx.getSource().sendSuccess(() -> Component.literal("§7- §f" + nome(nivel) + "§7: §f" + numero(borda.getSize())
                    + " §7blocos, centro §f" + numero(borda.getCenterX()) + " " + numero(borda.getCenterZ())), false);
        }
        return 1;
    }

    private static int ajustar(CommandContext<CommandSourceStack> ctx, ServerLevel nivel, double destino, long segundos) {
        WorldBorder borda = nivel.getWorldBorder();
        double atual = borda.getSize();
        if (destino < 1.0 || destino > WorldBorder.MAX_SIZE) {
            ctx.getSource().sendFailure(Component.literal("§cO tamanho deve ficar entre 1 e " + numero(WorldBorder.MAX_SIZE) + " blocos."));
            return 0;
        }
        if (destino == atual && borda.getLerpRemainingTime() == 0L) {
            ctx.getSource().sendFailure(Component.literal("§cA borda de " + nome(nivel) + " já tem esse tamanho."));
            return 0;
        }
        if (segundos > 0L) {
            borda.lerpSizeBetween(atual, destino, segundos * 1000L);
            String verbo = destino > atual ? "Aumentando" : "Reduzindo";
            ctx.getSource().sendSuccess(() -> Component.literal("§a" + verbo + " a borda de §f" + nome(nivel) + " §apara §f" + numero(destino) + " §ablocos em §f" + segundos + " §asegundos"), true);
        } else {
            borda.setSize(destino);
            ctx.getSource().sendSuccess(() -> Component.literal("§aBorda de §f" + nome(nivel) + " §adefinida em §f" + numero(destino) + " §ablocos"), true);
        }
        BordasPorDimensao.salvar(nivel);
        return (int) Math.abs(destino - atual);
    }

    private static int centro(CommandContext<CommandSourceStack> ctx, ServerLevel nivel, Vec2 posicao) {
        if (Math.abs(posicao.x) > WorldBorder.MAX_CENTER_COORDINATE || Math.abs(posicao.y) > WorldBorder.MAX_CENTER_COORDINATE) {
            ctx.getSource().sendFailure(Component.literal("§cO centro está longe demais (máximo " + numero(WorldBorder.MAX_CENTER_COORDINATE) + ")."));
            return 0;
        }
        nivel.getWorldBorder().setCenter(posicao.x, posicao.y);
        BordasPorDimensao.salvar(nivel);
        ctx.getSource().sendSuccess(() -> Component.literal("§aCentro da borda de §f" + nome(nivel) + " §adefinido em §f" + numero(posicao.x) + " " + numero(posicao.y)), true);
        return 1;
    }

    private static int dano(CommandContext<CommandSourceStack> ctx, ServerLevel nivel, double valor) {
        nivel.getWorldBorder().setDamagePerBlock(valor);
        BordasPorDimensao.salvar(nivel);
        ctx.getSource().sendSuccess(() -> Component.literal("§aDano da borda de §f" + nome(nivel) + " §adefinido em §f" + numero(valor) + " §apor bloco por segundo"), true);
        return 1;
    }

    private static int zonaSegura(CommandContext<CommandSourceStack> ctx, ServerLevel nivel, double valor) {
        nivel.getWorldBorder().setDamageSafeZone(valor);
        BordasPorDimensao.salvar(nivel);
        ctx.getSource().sendSuccess(() -> Component.literal("§aZona segura da borda de §f" + nome(nivel) + " §adefinida em §f" + numero(valor) + " §ablocos"), true);
        return 1;
    }

    private static int avisoDistancia(CommandContext<CommandSourceStack> ctx, ServerLevel nivel, int valor) {
        nivel.getWorldBorder().setWarningBlocks(valor);
        BordasPorDimensao.salvar(nivel);
        ctx.getSource().sendSuccess(() -> Component.literal("§aAviso de distância da borda de §f" + nome(nivel) + " §adefinido em §f" + valor + " §ablocos"), true);
        return valor;
    }

    private static int avisoTempo(CommandContext<CommandSourceStack> ctx, ServerLevel nivel, int valor) {
        nivel.getWorldBorder().setWarningTime(valor);
        BordasPorDimensao.salvar(nivel);
        ctx.getSource().sendSuccess(() -> Component.literal("§aAviso de tempo da borda de §f" + nome(nivel) + " §adefinido em §f" + valor + " §asegundos"), true);
        return valor;
    }
}
