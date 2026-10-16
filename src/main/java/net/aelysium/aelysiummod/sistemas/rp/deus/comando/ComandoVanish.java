package net.aelysium.aelysiummod.sistemas.rp.deus.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.aelysium.aelysiummod.sistemas.rp.deus.ManipuladorTransicaoVanish;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;

public class ComandoVanish {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> vanishNode = Commands.literal("vanish")
                .requires(src -> src.hasPermission(2));

        for (TipoDeus deus : TipoDeus.values()) {
            if (deus == TipoDeus.NONE) continue;
            final TipoDeus finalDeus = deus;
            vanishNode.then(
                    Commands.literal(deus.id)
                            .executes(context -> {
                                if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                                    return 0;
                                }
                                ManipuladorTransicaoVanish.startTransition(player, finalDeus);
                                return 1;
                            })
            );
        }

        return vanishNode;
    }
}
