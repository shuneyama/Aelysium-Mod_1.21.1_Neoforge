package net.aelysium.aelysiummod.sistemas.midia.tocar.comando;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.aelysium.aelysiummod.sistemas.midia.tocar.BaixadorSom;
import net.aelysium.aelysiummod.sistemas.midia.tocar.GerenciadorSom;
import net.aelysium.aelysiummod.sistemas.midia.tocar.rede.PacotesSom;

public class ComandoTocar {

    private static final int CHUNK_SIZE = 65536;

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("sons")
                .requires(source -> AelysiumGuarda.isAutorizado() && source.hasPermission(2))
                .then(Commands.literal("parar")
                        .then(Commands.argument("alvos", EntityArgument.players())
                                .executes(ctx -> {
                                    Collection<ServerPlayer> alvos = EntityArgument.getPlayers(ctx, "alvos");
                                    for (ServerPlayer player : alvos) {
                                        try {
                                            PacketDistributor.sendToPlayer(player, new PacotesSom.PacoteParar());
                                        } catch (UnsupportedOperationException ignored) {}
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.tocar.som_parado_para_jogador_es", alvos.size()), false);
                                    return alvos.size();
                                })
                        )
                )
                .then(Commands.literal("baixar")
                        .then(Commands.argument("entrada", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String[] partes = StringArgumentType.getString(ctx, "entrada").trim().split("\\s+");
                                    String url = partes[0].replaceAll("^[\"']+|[\"']+$", "");
                                    String nome = partes.length > 1 ? partes[1].replaceAll("^[\"']+|[\"']+$", "") : null;
                                    if (nome != null && !nome.matches("[A-Za-z0-9_.+-]+")) {
                                        ctx.getSource().sendFailure(Component.literal("§cO nome só pode ter letras, números, _ - . e +."));
                                        return 0;
                                    }
                                    BaixadorSom.iniciar(ctx.getSource(), url, nome);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("tocar")
                        .then(Commands.argument("som", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String nome : GerenciadorSom.listarSons()) {
                                        builder.suggest(nome);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.literal("jogadores")
                                        .then(Commands.argument("alvos", EntityArgument.players())
                                                .executes(ctx -> executar(ctx.getSource(), StringArgumentType.getString(ctx, "som"),
                                                        EntityArgument.getPlayers(ctx, "alvos"), 50, false, 0.0))
                                                .then(opcoes(ctx -> EntityArgument.getPlayers(ctx, "alvos")))
                                        )
                                )
                                .then(Commands.literal("raio")
                                        .then(Commands.argument("distancia", IntegerArgumentType.integer(1, 10000))
                                                .executes(ctx -> executar(ctx.getSource(), StringArgumentType.getString(ctx, "som"),
                                                        jogadoresNoRaio(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "distancia")), 50, false, 0.0))
                                                .then(opcoes(ctx -> jogadoresNoRaio(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "distancia"))))
                                        )
                                )
                        )
                );
    }

    private interface ResolvedorAlvos {
        Collection<ServerPlayer> resolver(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> opcoes(ResolvedorAlvos alvos) {
        return Commands.argument("volume", IntegerArgumentType.integer(1, 100))
                .executes(ctx -> executar(ctx.getSource(), StringArgumentType.getString(ctx, "som"), alvos.resolver(ctx),
                        IntegerArgumentType.getInteger(ctx, "volume"), false, 0.0))
                .then(Commands.argument("loop", BoolArgumentType.bool())
                        .executes(ctx -> executar(ctx.getSource(), StringArgumentType.getString(ctx, "som"), alvos.resolver(ctx),
                                IntegerArgumentType.getInteger(ctx, "volume"), BoolArgumentType.getBool(ctx, "loop"), 0.0))
                        .then(Commands.argument("transicao", DoubleArgumentType.doubleArg(0.0, 30.0))
                                .executes(ctx -> executar(ctx.getSource(), StringArgumentType.getString(ctx, "som"), alvos.resolver(ctx),
                                        IntegerArgumentType.getInteger(ctx, "volume"), BoolArgumentType.getBool(ctx, "loop"),
                                        DoubleArgumentType.getDouble(ctx, "transicao")))));
    }

    private static List<ServerPlayer> jogadoresNoRaio(CommandSourceStack source, int distancia) {
        Vec3 pos = source.getPosition();
        return source.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> p.position().distanceTo(pos) <= distancia)
                .collect(Collectors.toList());
    }

    private static int executar(CommandSourceStack source, String nome, Collection<ServerPlayer> alvos, int volumeInt, boolean loop, double transicao) {
        if (!GerenciadorSom.somExiste(nome)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.tocar.som_nao_encontrado", nome));
            return 0;
        }

        Path arquivo = GerenciadorSom.getArquivoSom(nome);
        byte[] dados;
        try {
            dados = Files.readAllBytes(arquivo);
        } catch (IOException e) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.tocar.erro_ao_ler_o_arquivo_de_som"));
            return 0;
        }

        float volume = volumeInt / 50.0f;
        int totalSize = dados.length;
        int totalChunks = (int) Math.ceil(totalSize / (double) CHUNK_SIZE);
        int enviados = 0;

        for (ServerPlayer player : alvos) {
            try {
                PacketDistributor.sendToPlayer(player, new PacotesSom.PacoteIniciarSom(nome, totalSize));

                for (int i = 0; i < totalChunks; i++) {
                    int inicio = i * CHUNK_SIZE;
                    int fim = Math.min(inicio + CHUNK_SIZE, totalSize);
                    int len = fim - inicio;
                    byte[] chunk = new byte[len];
                    System.arraycopy(dados, inicio, chunk, 0, len);
                    PacketDistributor.sendToPlayer(player, new PacotesSom.PacoteChunkSom(nome, i, chunk));
                }

                PacketDistributor.sendToPlayer(player, new PacotesSom.PacoteTocarSom(nome, volume, loop, (float) transicao));
                enviados++;
            } catch (UnsupportedOperationException ignored) {}
        }

        final int finalEnviados = enviados;
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.tocar.tocando_para_jogador_es_volume", nome, finalEnviados, volumeInt), false);
        if (loop) {
            String transicaoTexto = transicao > 0 ? " com transição de " + transicao + " s" : "";
            source.sendSuccess(() -> Component.literal("§7Em loop" + transicaoTexto + ". Use /aelysium sons parar para encerrar."), false);
        }
        return finalEnviados;
    }
}
