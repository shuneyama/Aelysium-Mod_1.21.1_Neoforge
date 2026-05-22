package net.aelysium.aelysiummod.holograma;

import net.aelysium.aelysiummod.holograma.network.HologramaPackets;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class HologramaTicker {

    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;

        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();
        Collection<Holograma> hologramas = gerenciador.getTodosHologramas();
        if (hologramas.isEmpty()) return;

        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        for (Holograma holo : hologramas) {
            if (!holo.isAtivo()) continue;
            if (holo.getLinhas().isEmpty()) continue;

            if (tickCounter % holo.getIntervaloAtualizacao() != 0) continue;

            for (ServerPlayer player : players) {
                boolean dentroDoRange = gerenciador.estaDentroDoRange(player, holo);
                boolean jaVendo = gerenciador.jogadorVendo(player.getUUID(), holo.getNome());

                if (dentroDoRange && !jaVendo) {
                    enviarSpawn(player, holo);
                    gerenciador.marcarVisivel(player.getUUID(), holo.getNome());
                } else if (!dentroDoRange && jaVendo) {
                    enviarDestroy(player, holo.getNome());
                    gerenciador.marcarInvisivel(player.getUUID(), holo.getNome());
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            GerenciadorHologramas.getInstance().limparJogador(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();

            Set<String> visiveis = new HashSet<>(gerenciador.getHologramasVisiveis(player.getUUID()));
            for (String nomeHolo : visiveis) {
                enviarDestroy(player, nomeHolo);
                gerenciador.marcarInvisivel(player.getUUID(), nomeHolo);
            }
        }
    }

    public static void enviarSpawn(ServerPlayer player, Holograma holo) {
        List<String> linhas = new ArrayList<>();
        List<Double> offsets = new ArrayList<>();
        for (LinhaHolograma linha : holo.getLinhas()) {
            linhas.add(linha.getConteudo());
            offsets.add(linha.getOffsetY());
        }

        PacketDistributor.sendToPlayer(player,
                new HologramaPackets.SpawnHologramaS2C(
                        holo.getNome(), holo.getX(), holo.getY(), holo.getZ(),
                        linhas, offsets
                ));
    }

    public static void enviarDestroy(ServerPlayer player, String nomeHolo) {
        PacketDistributor.sendToPlayer(player,
                new HologramaPackets.DestroyHologramaS2C(nomeHolo));
    }

    public static void enviarUpdate(ServerPlayer player, Holograma holo) {
        List<String> linhas = new ArrayList<>();
        List<Double> offsets = new ArrayList<>();
        for (LinhaHolograma linha : holo.getLinhas()) {
            linhas.add(linha.getConteudo());
            offsets.add(linha.getOffsetY());
        }

        PacketDistributor.sendToPlayer(player,
                new HologramaPackets.UpdateHologramaS2C(
                        holo.getNome(), holo.getX(), holo.getY(), holo.getZ(),
                        linhas, offsets
                ));
    }

    public static void atualizarParaTodos(Holograma holo, List<ServerPlayer> players) {
        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();
        for (ServerPlayer player : players) {
            if (gerenciador.jogadorVendo(player.getUUID(), holo.getNome())) {
                enviarUpdate(player, holo);
            }
        }
    }

    public static void destruirParaTodos(String nomeHolo, List<ServerPlayer> players) {
        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();
        for (ServerPlayer player : players) {
            if (gerenciador.jogadorVendo(player.getUUID(), nomeHolo)) {
                enviarDestroy(player, nomeHolo);
                gerenciador.marcarInvisivel(player.getUUID(), nomeHolo);
            }
        }
    }
}