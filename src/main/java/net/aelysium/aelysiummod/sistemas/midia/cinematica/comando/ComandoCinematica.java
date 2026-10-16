package net.aelysium.aelysiummod.sistemas.midia.cinematica.comando;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacotePararVideo;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteStatusDownload;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteTocarVideo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.GerenciadorVideoServidor;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.RelatorioCutscene;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.SessaoDownloadVideo;

public class ComandoCinematica {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_NOMES = (ctx, builder) -> {
        for (String n : GerenciadorVideoServidor.listarVideos(ctx.getSource().getServer())) {
            builder.suggest(n);
        }
        return builder.buildFuture();
    };

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("cutscene")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("baixar")
                        .then(Commands.argument("nome", StringArgumentType.string())
                                .suggests(SUGGEST_NOMES)
                                .executes(ctx -> baixar(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException()),
                                        StringArgumentType.getString(ctx, "nome")))
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ctx -> baixar(ctx.getSource(), EntityArgument.getPlayers(ctx, "alvos"),
                                                StringArgumentType.getString(ctx, "nome")))
                                )
                        )
                )
                .then(Commands.literal("tocar")
                        .then(Commands.argument("nome", StringArgumentType.string())
                                .suggests(SUGGEST_NOMES)
                                .executes(ctx -> {
                                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                    return tocar(ctx.getSource(), List.of(sp),
                                            StringArgumentType.getString(ctx, "nome"), 80, 40);
                                })
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ctx -> tocar(ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "alvos"),
                                                StringArgumentType.getString(ctx, "nome"), 80, 40))
                                        .then(Commands.argument("fadeIn", IntegerArgumentType.integer(0, 200))
                                                .then(Commands.argument("fadeOut", IntegerArgumentType.integer(0, 200))
                                                        .executes(ctx -> tocar(ctx.getSource(),
                                                                EntityArgument.getPlayers(ctx, "alvos"),
                                                                StringArgumentType.getString(ctx, "nome"),
                                                                IntegerArgumentType.getInteger(ctx, "fadeIn"),
                                                                IntegerArgumentType.getInteger(ctx, "fadeOut")
                                                        ))))
                                )
                        )
                )
                .then(Commands.literal("parar")
                        .executes(ctx -> parar(ctx.getSource(), ctx.getSource().getServer().getPlayerList().getPlayers()))
                        .then(Commands.argument("alvos", EntityArgument.players())
                                .executes(ctx -> parar(ctx.getSource(), EntityArgument.getPlayers(ctx, "alvos"))))
                );
    }

    private record VideoValidado(Path video, Path audio, boolean temAudio, long tamanhoVideo, long tamanhoAudio) {}

    private static VideoValidado validar(CommandSourceStack source, String nome) {
        MinecraftServer server = source.getServer();

        if (!GerenciadorVideoServidor.nomeValido(nome)) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.cinematica.nome_invalido_use_apenas_letras"));
            return null;
        }
        if (!GerenciadorVideoServidor.existeVideo(server, nome)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.cinematica.video_mp4_nao_encontrado_em", nome, GerenciadorVideoServidor.NOME_PASTA));
            return null;
        }

        Path videoFile = GerenciadorVideoServidor.arquivoVideo(server, nome);
        long tamanhoVideo;
        try { tamanhoVideo = Files.size(videoFile); } catch (IOException e) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.cinematica.erro_ao_ler_arquivo", e.getMessage()));
            return null;
        }
        if (tamanhoVideo > GerenciadorVideoServidor.TAMANHO_MAXIMO_BYTES) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.cinematica.arquivo_muito_grande", GerenciadorVideoServidor.tamanhoLegivel(tamanhoVideo)));
            return null;
        }

        Path audioFile = GerenciadorVideoServidor.arquivoAudio(server, nome);
        boolean temAudio = Files.isRegularFile(audioFile);
        long tamanhoAudio = 0;
        if (temAudio) {
            try { tamanhoAudio = Files.size(audioFile); } catch (IOException e) { temAudio = false; }
        }
        return new VideoValidado(videoFile, audioFile, temAudio, tamanhoVideo, tamanhoAudio);
    }

    private static int baixar(CommandSourceStack source, Collection<ServerPlayer> alvos, String nome) {
        MinecraftServer server = source.getServer();
        VideoValidado v = validar(source, nome);
        if (v == null) return 0;

        List<ServerPlayer> alvosLista = new ArrayList<>(alvos);
        if (alvosLista.isEmpty()) {
            source.sendFailure(Component.literal("§cNenhum jogador alvo foi encontrado."));
            return 0;
        }

        RelatorioCutscene relatorio = RelatorioCutscene.iniciarDownload(source, nome, alvosLista, v.tamanhoVideo(), v.tamanhoAudio());

        Thread preparo = new Thread(() -> {
            if (v.tamanhoVideo() > 20L * 1024 * 1024) relatorio.avisoCalculandoVerificacao();
            String hashVideo = GerenciadorVideoServidor.hashCache(v.video());
            String hashAudio = v.temAudio() ? GerenciadorVideoServidor.hashCache(v.audio()) : "";
            server.execute(() -> iniciarEnvios(alvosLista, nome, v, hashVideo, hashAudio, relatorio));
        }, "AelysiumCutscenePreparo-" + nome);
        preparo.setDaemon(true);
        preparo.start();
        return alvosLista.size();
    }

    private static int tocar(CommandSourceStack source, Collection<ServerPlayer> alvos, String nome, int fadeIn, int fadeOut) {
        MinecraftServer server = source.getServer();
        VideoValidado v = validar(source, nome);
        if (v == null) return 0;

        List<ServerPlayer> alvosLista = new ArrayList<>(alvos);
        if (alvosLista.isEmpty()) {
            source.sendFailure(Component.literal("§cNenhum jogador alvo foi encontrado."));
            return 0;
        }

        RelatorioCutscene relatorio = RelatorioCutscene.iniciarTocar(source, nome, alvosLista);

        Thread preparo = new Thread(() -> {
            String hashVideo = GerenciadorVideoServidor.hashCache(v.video());
            server.execute(() -> {
                for (ServerPlayer p : alvosLista) {
                    if (GerenciadorVideoServidor.estaConfirmado(p.getUUID(), nome, hashVideo)) {
                        PacketDistributor.sendToPlayer(p, new PacoteTocarVideo(nome, fadeIn, fadeOut, 0L));
                        relatorio.tocarEnviado(p, null);
                        continue;
                    }
                    int status = GerenciadorVideoServidor.statusJogador(p.getUUID(), nome);
                    if (status == PacoteStatusDownload.STATUS_FALHA_HASH || status == PacoteStatusDownload.STATUS_FALHA_IO
                            || status == PacoteStatusDownload.STATUS_CANCELADO) {
                        relatorio.tocarIgnorado(p, "o último download falhou; use /aelysium cutscene baixar de novo");
                        continue;
                    }
                    PacketDistributor.sendToPlayer(p, new PacoteTocarVideo(nome, fadeIn, fadeOut, 0L));
                    relatorio.tocarEnviado(p, "sem confirmação de download nesta sessão (ou o vídeo mudou); se ele não tiver a cutscene no cache, vai falhar");
                }
            });
        }, "AelysiumCutscenePreparo-" + nome);
        preparo.setDaemon(true);
        preparo.start();
        return alvosLista.size();
    }

    private static void iniciarEnvios(List<ServerPlayer> alvos, String nome, VideoValidado v, String hashVideo, String hashAudio,
                                      RelatorioCutscene relatorio) {
        relatorio.enviandoArquivos();
        for (ServerPlayer p : alvos) {
            try {
                SessaoDownloadVideo sessaoMp4 = SessaoDownloadVideo.criar(p, nome, "mp4");
                sessaoMp4.aoProgresso(s -> relatorio.envioProgresso(p, s));
                sessaoMp4.aoConcluir(s -> {
                    if (s.estado() == SessaoDownloadVideo.Estado.CONCLUIDA) relatorio.envioConcluido(p, s);
                    else relatorio.envioFalhou(p, s.mensagemFalha());
                });

                if (v.temAudio()) {
                    SessaoDownloadVideo sessaoOgg = SessaoDownloadVideo.criar(p, nome, "ogg");
                    sessaoOgg.aoProgresso(s -> relatorio.envioProgresso(p, s));
                    sessaoOgg.aoConcluir(s -> {
                        if (s.estado() == SessaoDownloadVideo.Estado.CONCLUIDA) relatorio.envioConcluido(p, s);
                        else relatorio.avisoAudio(p, s.mensagemFalha());
                    });
                    sessaoOgg.enviarArquivo(v.audio(), "ogg", hashAudio);
                    Thread t = new Thread(() -> {
                        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                        sessaoMp4.enviarArquivo(v.video(), "mp4", hashVideo);
                    }, "AelysiumCutsceneQueue-" + p.getName().getString());
                    t.setDaemon(true);
                    t.start();
                } else {
                    sessaoMp4.enviarArquivo(v.video(), "mp4", hashVideo);
                }
            } catch (Exception e) {
                relatorio.envioFalhou(p, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private static int parar(CommandSourceStack source, Collection<ServerPlayer> alvos) {
        for (ServerPlayer p : alvos) {
            PacketDistributor.sendToPlayer(p, new PacotePararVideo());
        }
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.cinematica.cutscene_parada_para_jogador_es", alvos.size()), true);
        return alvos.size();
    }
}
