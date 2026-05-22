package net.aelysium.aelysiummod.protecao.network;

import net.aelysium.aelysiummod.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.protecao.regiao.Regiao;
import net.aelysium.aelysiummod.menu.gui.TelaConfigRegiao;
import net.aelysium.aelysiummod.menu.gui.TelaListaRegioes;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.UUID;

public class ProtecaoClientHandlers {

    public static void handleAbrirTela(PacoteAbrirTela packet, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new TelaConfigRegiao(packet.posicaoMinima(), packet.posicaoMaxima()));
    }

    public static void handleAbrirTelaEdicao(PacoteAbrirTelaEdicao packet, IPayloadContext context) {
        Regiao regiao = new Regiao(packet.nome(), packet.posicaoMinima(), packet.posicaoMaxima());
        for (Map.Entry<FlagRegiao, Boolean> e : packet.flags().entrySet()) {
            regiao.setFlag(e.getKey(), e.getValue());
        }
        for (UUID uuid : packet.donos()) regiao.adicionarDono(uuid);
        regiao.setMensagemEntrada(packet.mensagemEntrada());
        regiao.setMensagemSaida(packet.mensagemSaida());
        Minecraft.getInstance().setScreen(new TelaConfigRegiao(regiao));
    }

    public static void handleListaRegioes(PacoteListaRegioes packet, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new TelaListaRegioes(packet.regioes()));
    }
}