package net.aelysium.aelysiummod.nucleo.comando;

import net.aelysium.aelysiummod.nucleo.texto.SemConversao;
import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.aelysium.aelysiummod.sistemas.social.chat.GerenciadorChat;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.comando.ComandoCinematica;
import net.aelysium.aelysiummod.sistemas.midia.tocar.comando.ComandoTocar;
import net.aelysium.aelysiummod.sistemas.admin.discord.comando.ComandoDiscord;
import net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoChat;
import net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoMod;
import net.aelysium.aelysiummod.sistemas.mundo.lua.GerenciadorLua;
import net.aelysium.aelysiummod.sistemas.mundo.lua.TipoLua;
import net.aelysium.aelysiummod.sistemas.social.chat.GerenciadorEspiaoSocial;
import net.aelysium.aelysiummod.sistemas.admin.listabranca.comando.ComandoListaBranca;
import net.aelysium.aelysiummod.sistemas.rp.habilidade.GerenciadorHabilidade;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.social.chat.comando.ComandoChat;
import net.aelysium.aelysiummod.sistemas.mundo.dimensao.ComandoDimensao;
import net.aelysium.aelysiummod.sistemas.admin.historico.comando.ComandoAnotacao;
import net.aelysium.aelysiummod.sistemas.admin.historico.comando.ComandoHistorico;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.UUID;
import net.aelysium.aelysiummod.sistemas.admin.atributos.comando.ComandoAtributos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.comando.ComandoBanidos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.comando.ComandosBanimentoCriacao;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.comando.ComandosListaBanimentos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.comando.ComandosModsDesativados;
import net.aelysium.aelysiummod.sistemas.admin.discord.comando.ComandoTicket;
import net.aelysium.aelysiummod.sistemas.admin.proibicao.comando.ComandoProibir;
import net.aelysium.aelysiummod.sistemas.admin.protecao.comando.ComandoProtecao;
import net.aelysium.aelysiummod.sistemas.midia.holograma.comando.ComandoHolograma;
import net.aelysium.aelysiummod.sistemas.mundo.teleporte.comando.ComandoTeleporte;
import net.aelysium.aelysiummod.sistemas.rp.escola.comando.ComandoAfinidade;
import net.aelysium.aelysiummod.sistemas.admin.jogador.comando.ComandoJogador;
import net.aelysium.aelysiummod.sistemas.rp.morte.comando.ComandoMorte;
import net.aelysium.aelysiummod.sistemas.rp.raca.comando.ComandoRaca;
import net.aelysium.aelysiummod.sistemas.rp.vidas.comando.ComandoVidas;
import net.aelysium.aelysiummod.sistemas.admin.vip.comando.ComandoVip;
import net.aelysium.aelysiummod.sistemas.mundo.dificuldade.comando.ComandoDificuldade;
import net.aelysium.aelysiummod.sistemas.social.apelido.comando.ComandoApelido;
import net.aelysium.aelysiummod.sistemas.social.npc.comando.ComandoNpc;
import net.aelysium.aelysiummod.sistemas.social.chat.rede.RedeChat;

public class AelysiumComandos {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("aelysium")
                        .requires(source -> AelysiumGuarda.isAutorizado())

                        .then(ComandoApelido.build())

                        .then(ComandoApelido.buildPronomes())

                        .then(ComandoVidas.build())

                        .then(ComandoVip.build())

                        .then(ComandoDificuldade.build())

                        .then(ComandoTicket.build())

                        .then(ComandoTocar.build())

                        .then(ComandosListaBanimentos.build())

                        .then(ComandosModsDesativados.build())

                        .then(ComandoProibir.build())

                        .then(ComandoAfinidade.build())

                        .then(ComandosBanimentoCriacao.build().requires(source -> source.hasPermission(2)))

                        .then(ComandoNpc.build().requires(source -> source.hasPermission(2)))

                        .then(ComandoCinematica.build())

                        .then(ComandoMorte.build().requires(source -> source.hasPermission(2)))

                        .then(ComandoHistorico.build())

                        .then(ComandoAnotacao.build())

                        .then(ComandoTeleporte.build())

                        .then(ComandoProtecao.build())

                        .then(ComandoRaca.build())


                        .then(ComandoAtributos.build())

                        .then(net.aelysium.aelysiummod.sistemas.rp.personagem.comando.ComandoPersonagem.build())

                        .then(ComandoJogador.build())

                        .then(ComandoChat.build())

                        .then(Commands.literal("balao")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        RedeChat.enviarAbrirBalloonGui(player);
                                        return 1;
                                    }
                                    return 0;
                                })
                        )

                        .then(Commands.literal("lua")
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> {
                                    GerenciadorLua.toggleLuaVermelha(ctx.getSource().getServer());
                                    boolean vermelha = GerenciadorLua.luaAtual == TipoLua.VERMELHA;
                                    ctx.getSource().sendSuccess(() -> Component.translatable(vermelha
                                            ? "mensagem.aelysiummod.lua.vermelha_ativada"
                                            : "mensagem.aelysiummod.lua.voltou_ao_normal"), true);
                                    return 1;
                                })
                        )

                        .then(Commands.literal("manutencao")
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> {
                                    boolean ativo = GerenciadorHabilidade.toggleManutencao();
                                    if (ativo) {
                                        int kickados = 0;
                                        for (ServerPlayer player : new java.util.ArrayList<>(ctx.getSource().getServer().getPlayerList().getPlayers())) {
                                            if (!player.hasPermissions(2)) {
                                                player.connection.disconnect(Component.literal(
                                                        "§c§lAelysium está em manutenção!\n\n§7Fique de olho nos avisos do servidor!"
                                                ));
                                                kickados++;
                                            }
                                        }
                                        int finalKickados = kickados;
                                        ctx.getSource().sendSuccess(() ->
                                                Component.translatableEscape("mensagem.aelysiummod.nucleo_comando.manutencao_ativada_jogador_es", finalKickados), true);
                                    } else {
                                        ctx.getSource().sendSuccess(() ->
                                                Component.translatable("mensagem.aelysiummod.nucleo_comando.manutencao_desativada_jogadores_podem"), true);
                                    }
                                    return 1;
                                })
                        )

                        .then(ComandoListaBranca.build())

                        .then(Commands.literal("anunciar")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    Collection<ServerPlayer> alvos = EntityArgument.getPlayers(ctx, "alvos");
                                                    String mensagem = StringArgumentType.getString(ctx, "mensagem");
                                                    Component componente = CodigosEstilo.componente(mensagem);

                                                    for (ServerPlayer player : alvos) {
                                                        player.sendSystemMessage(componente);
                                                    }
                                                    return alvos.size();
                                                })
                                        )
                                )
                        )

                        .then(ComandoDimensao.build())
        );

        ComandoChat.registrarAtalhos(event.getDispatcher());
        event.getDispatcher().register(ComandoDiscord.build());

        net.aelysium.aelysiummod.sistemas.mundo.renascimento.ComandoSetWorldSpawn.registrar(event.getDispatcher());
        net.aelysium.aelysiummod.sistemas.mundo.bordas.ComandoBorda.registrar(event.getDispatcher());
        ComandoBanidos.registrarExtras(event);
        ComandoHistorico.registrarDebug(event);
        com.mojang.brigadier.tree.CommandNode<net.minecraft.commands.CommandSourceStack> raizAelysium = event.getDispatcher().getRoot().getChild("aelysium");
        if (raizAelysium != null) {
            SemConversao.proteger(raizAelysium.getChild("lives"));
            SemConversao.proteger(raizAelysium.getChild("sons"));
        }
        ComandoHolograma.register(event.getDispatcher());
        ComandoProtecao.registrarSubComandosLista(event.getDispatcher());
        ComandoNpc.registrarSubComandosAcoes(event.getDispatcher());
    }
}