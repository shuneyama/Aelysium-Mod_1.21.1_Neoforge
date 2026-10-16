package net.aelysium.aelysiummod.sistemas.rp.vidas.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.sistemas.rp.vidas.DadosVidas;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public class ComandoVidas {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("lives")
                .then(Commands.literal("publicar")
                        .then(Commands.argument("link", StringArgumentType.greedyString())
                                .executes(ComandoVidas::executePublicar)
                        )
                )
                .then(Commands.literal("visualizar")
                        .executes(ComandoVidas::executeVisualizar)
                )
                .then(Commands.literal("encerrar")
                        .executes(ComandoVidas::executeEncerrar)
                );
    }

    private static int executePublicar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String link = StringArgumentType.getString(ctx, "link").trim();
        MinecraftServer server = ctx.getSource().getServer();

        if (link.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.vidas.voce_precisa_informar_um_link_valido"));
            return 0;
        }

        DadosVidas.get(server).publicar(player.getUUID(), link);
        PerfilJogador.apelido().atualizarListaJogadores(player);

        MutableComponent nome = PerfilJogador.apelido().nomeCompletoEstilizado(server, player.getUUID());
        if (nome == null) nome = Component.literal(player.getGameProfile().getName());

        MutableComponent anuncio = Component.literal("§d")
                .append(nome)
                .append(Component.translatable("mensagem.aelysiummod.vidas.acabou_de_abrir_uma_live_em_solaris"));

        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            online.sendSystemMessage(anuncio);
        }

        ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.vidas.sua_live_foi_publicada_seu_nick_agora"), false);
        return 1;
    }

    private static int executeVisualizar(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Map<UUID, String> ativas = DadosVidas.get(server).getLivesAtivas();

        if (ativas.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.vidas.ninguem_esta_fazendo_live_no_momento"), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.vidas.lives_ativas"), false);
        for (Map.Entry<UUID, String> entry : ativas.entrySet()) {
            MutableComponent nome = PerfilJogador.apelido().nomeCompletoEstilizado(server, entry.getKey());
            if (nome == null) nome = Component.literal(PerfilJogador.apelido().nomeExibicao(server, entry.getKey()));

            String link = entry.getValue();
            MutableComponent linkComponent = Component.literal(link).setStyle(Style.EMPTY
                    .withColor(net.minecraft.ChatFormatting.AQUA)
                    .withUnderlined(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("mensagem.aelysiummod.vidas.clique_para_abrir_a_live"))));

            MutableComponent linha = Component.empty()
                    .append(nome)
                    .append(Component.translatable("mensagem.aelysiummod.vidas.separador"))
                    .append(linkComponent);
            ctx.getSource().sendSuccess(() -> linha, false);
        }
        return 1;
    }

    private static int executeEncerrar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = ctx.getSource().getServer();

        if (!DadosVidas.get(server).isLive(player.getUUID())) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.vidas.voce_nao_esta_com_nenhuma_live_ativa"));
            return 0;
        }

        DadosVidas.get(server).encerrar(player.getUUID());
        PerfilJogador.apelido().atualizarListaJogadores(player);

        ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.vidas.sua_live_foi_encerrada"), false);
        return 1;
    }
}
