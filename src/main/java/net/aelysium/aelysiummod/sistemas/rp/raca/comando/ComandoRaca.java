package net.aelysium.aelysiummod.sistemas.rp.raca.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteVooValkyria;
import net.aelysium.aelysiummod.sistemas.rp.raca.DadosRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.GerenciadorRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.AnimacaoRevelacaoRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.TemporizadorRaca;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class ComandoRaca {

    private static final SuggestionProvider<CommandSourceStack> RACE_SUGGESTIONS =
            (ctx, builder) -> {
                for (TipoRaca r : TipoRaca.values()) {
                    if (r != TipoRaca.NONE) builder.suggest(r.id);
                }
                builder.suggest("nenhum");
                return builder.buildFuture();
            };

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("raca")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("definir")
                        .then(Commands.argument("raca", StringArgumentType.string())
                                .suggests(RACE_SUGGESTIONS)
                                .executes(ctx -> executeSet(ctx, ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("jogador", EntityArgument.player())
                                        .executes(ctx -> executeSet(ctx, EntityArgument.getPlayer(ctx, "jogador")))
                                )
                        )
                )
                .then(Commands.literal("resetar")
                        .executes(ctx -> executeReset(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> executeReset(ctx, EntityArgument.getPlayer(ctx, "jogador")))
                        )
                )
                .then(Commands.literal("selecao")
                        .executes(ctx -> abrirSelecao(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> abrirSelecao(ctx, EntityArgument.getPlayer(ctx, "jogador")))
                        )
                )
                .then(Commands.literal("ver")
                        .executes(ctx -> executeView(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> executeView(ctx, EntityArgument.getPlayer(ctx, "jogador")))
                        )
                );
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        try {
            String raceId = StringArgumentType.getString(ctx, "raca");
            TipoRaca race = TipoRaca.fromId(raceId);

            if (AnimacaoRevelacaoRaca.isAnimating(target.getUUID())) {
                ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.raca.esse_jogador_ja_esta_recebendo_uma"));
                return 0;
            }

            TipoRaca oldRace = TemporizadorRaca.getCachedRace(target.getUUID());

            if (race == TipoRaca.NONE) {
                return doReset(ctx, target);
            }

            AnimacaoRevelacaoRaca.start(target, race, oldRace);

            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.raca.recebendo_raca",
                    target.getName().getString(), race.displayName), true);

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.raca.erro_ao_aplicar_raca", e.getMessage()));
            return 0;
        }
    }

    private static int executeReset(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        try {
            return doReset(ctx, target);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.raca.erro_ao_resetar_raca", e.getMessage()));
            return 0;
        }
    }

    private static int doReset(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        target.removeAllEffects();
        GerenciadorRaca.applyRace(target, TipoRaca.NONE);
        DadosRaca.get(target.serverLevel()).setRace(target.getUUID(), TipoRaca.NONE);
        TemporizadorRaca.updateCache(target.getUUID(), TipoRaca.NONE);

        PacketDistributor.sendToPlayer(target, new PacoteVooValkyria(false, 0, 0));

        ctx.getSource().sendSuccess(() ->
                Component.translatableEscape("mensagem.aelysiummod.raca.perdeu_sua_raca", target.getName().getString()), true);
        target.sendSystemMessage(Component.translatable("mensagem.aelysiummod.raca.aelysium_sua_raca_foi_removida"));

        return 1;
    }

    private static int abrirSelecao(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        net.aelysium.aelysiummod.sistemas.rp.raca.SelecaoRaca.abrir(target);
        ctx.getSource().sendSuccess(() ->
                Component.translatableEscape("mensagem.aelysiummod.raca.selecao_aberta", target.getName().getString()), true);
        return 1;
    }

    private static int executeView(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        try {
            TipoRaca race = TemporizadorRaca.getCachedRace(target.getUUID());

            ctx.getSource().sendSuccess(() ->
                    Component.translatableEscape("mensagem.aelysiummod.raca.e_da_raca", target.getName().getString(), race.displayName), false);

            return race.ordinal();
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.raca.erro", e.getMessage()));
            return 0;
        }
    }
}