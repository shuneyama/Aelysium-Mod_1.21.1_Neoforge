package net.aelysium.aelysiummod.nucleo.comando;

import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public class AelysiumOpcoesSeletor {

    public static void registrar() {
        EntitySelectorOptions.register("raca", parser -> {
            boolean negado = parser.shouldInvertValue();
            String valor = parser.getReader().readUnquotedString();
            TipoRaca raca = TipoRaca.fromId(valor);
            parser.addPredicate(entity -> {
                if (!(entity instanceof ServerPlayer player)) return false;
                TipoRaca racaJogador = PerfilJogador.raca().raca(player.getUUID());
                return (racaJogador == raca) != negado;
            });
        }, parser -> AelysiumGuarda.isAutorizadoAgora(), Component.translatable("mensagem.aelysiummod.nucleo_comando.raca_raca"));

        EntitySelectorOptions.register("deus", parser -> {
            boolean negado = parser.shouldInvertValue();
            String valor = parser.getReader().readUnquotedString();
            TipoDeus deus = TipoDeus.fromId(valor);
            parser.addPredicate(entity -> {
                if (!(entity instanceof ServerPlayer player)) return false;
                TipoDeus deusJogador = PerfilJogador.deus().deus(player);
                return (deusJogador == deus) != negado;
            });
        }, parser -> AelysiumGuarda.isAutorizadoAgora(), Component.translatable("mensagem.aelysiummod.nucleo_comando.deus_deus"));
    }
}
