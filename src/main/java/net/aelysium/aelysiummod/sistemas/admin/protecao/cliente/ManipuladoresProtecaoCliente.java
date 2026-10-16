package net.aelysium.aelysiummod.sistemas.admin.protecao.cliente;

import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.RegiaoCacheCliente;
import net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.TelaConfigRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.TelaListaRegioes;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTela;
import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTelaEdicao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteListaRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteSyncRegioesCliente;

public class ManipuladoresProtecaoCliente {

    public static void handleAbrirTela(PacoteAbrirTela packet, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new TelaConfigRegiao(packet.posicaoMinima(), packet.posicaoMaxima()));
    }

    public static void handleAbrirTelaEdicao(PacoteAbrirTelaEdicao packet, IPayloadContext context) {
        Regiao regiao = new Regiao(packet.nome(), packet.posicaoMinima(), packet.posicaoMaxima());
        for (Map.Entry<FlagRegiao, Boolean> e : packet.flags().entrySet()) {
            regiao.setFlag(e.getKey(), e.getValue());
        }
        for (UUID uuid : packet.donos()) regiao.adicionarDono(uuid);
        regiao.setDonoOps(packet.donoOps());
        regiao.setMensagemEntrada(packet.mensagemEntrada());
        regiao.setMensagemSaida(packet.mensagemSaida());

        Map<UUID, String> nomesDonos = new java.util.HashMap<>();
        List<UUID> donos = packet.donos();
        List<String> nomes = packet.donosNomes();
        for (int i = 0; i < donos.size() && i < nomes.size(); i++) {
            nomesDonos.put(donos.get(i), nomes.get(i));
        }

        Minecraft.getInstance().setScreen(new TelaConfigRegiao(regiao, nomesDonos));
    }

    public static void handleListaRegioes(PacoteListaRegioes packet, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new TelaListaRegioes(packet.regioes()));
    }

    public static void handleSyncRegioes(PacoteSyncRegioesCliente packet, IPayloadContext context) {
        List<RegiaoCacheCliente.RegiaoEntry> entries = new ArrayList<>();
        for (PacoteSyncRegioesCliente.RegiaoMin r : packet.regioes()) {
            entries.add(new RegiaoCacheCliente.RegiaoEntry(r.nome(), r.min(), r.max(), r.dim(),
                    r.domoAtivo(), r.domoCor(), r.domoDistancia()));
        }
        RegiaoCacheCliente.setEntries(entries);
    }
}