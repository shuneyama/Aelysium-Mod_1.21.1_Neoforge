package net.aelysium.aelysiummod.holograma;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GerenciadorHologramas {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static GerenciadorHologramas instance;

    private final Map<String, Holograma> hologramas = new ConcurrentHashMap<>();

    private final Map<UUID, Set<String>> jogadoresVisiveis = new ConcurrentHashMap<>();

    private MinecraftServer servidor;
    private static final String NOME_ARQUIVO = "aelysium_hologramas.dat";

    private GerenciadorHologramas() {}

    public static GerenciadorHologramas getInstance() {
        if (instance == null) {
            instance = new GerenciadorHologramas();
        }
        return instance;
    }

    public void setServidor(MinecraftServer servidor) {
        this.servidor = servidor;
        carregarHologramas();
    }

    public void adicionarHolograma(Holograma holo) {
        hologramas.put(holo.getNome().toLowerCase(), holo);
        salvarHologramas();
    }

    public Holograma getHolograma(String nome) {
        return hologramas.get(nome.toLowerCase());
    }

    public boolean removerHolograma(String nome) {
        Holograma removido = hologramas.remove(nome.toLowerCase());
        if (removido != null) {
            for (Set<String> visiveis : jogadoresVisiveis.values()) {
                visiveis.remove(nome.toLowerCase());
            }
            salvarHologramas();
            return true;
        }
        return false;
    }

    public boolean existeHolograma(String nome) {
        return hologramas.containsKey(nome.toLowerCase());
    }

    public Collection<Holograma> getTodosHologramas() {
        return hologramas.values();
    }

    public List<String> getNomesHologramas() {
        return new ArrayList<>(hologramas.keySet());
    }

    public boolean jogadorVendo(UUID jogadorUUID, String nomeHolo) {
        Set<String> visiveis = jogadoresVisiveis.get(jogadorUUID);
        return visiveis != null && visiveis.contains(nomeHolo.toLowerCase());
    }

    public void marcarVisivel(UUID jogadorUUID, String nomeHolo) {
        jogadoresVisiveis.computeIfAbsent(jogadorUUID, k -> ConcurrentHashMap.newKeySet())
                .add(nomeHolo.toLowerCase());
    }

    public void marcarInvisivel(UUID jogadorUUID, String nomeHolo) {
        Set<String> visiveis = jogadoresVisiveis.get(jogadorUUID);
        if (visiveis != null) {
            visiveis.remove(nomeHolo.toLowerCase());
        }
    }

    public Set<String> getHologramasVisiveis(UUID jogadorUUID) {
        return jogadoresVisiveis.getOrDefault(jogadorUUID, Collections.emptySet());
    }

    public void limparJogador(UUID jogadorUUID) {
        jogadoresVisiveis.remove(jogadorUUID);
    }

    public boolean estaDentroDoRange(ServerPlayer player, Holograma holo) {
        if (!player.level().dimension().location().toString().equals(holo.getMundo())) {
            return false;
        }
        double dx = player.getX() - holo.getX();
        double dy = player.getY() - holo.getY();
        double dz = player.getZ() - holo.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;
        double range = holo.getRangeVisibilidade();
        return distSq <= range * range;
    }

    public void salvarHologramas() {
        if (servidor == null) return;
        try {
            File arquivo = new File(servidor.getWorldPath(LevelResource.ROOT).toFile(), NOME_ARQUIVO);
            CompoundTag tagPrincipal = new CompoundTag();
            ListTag listaHologramas = new ListTag();

            for (Holograma holo : hologramas.values()) {
                listaHologramas.add(holo.salvarNBT());
            }

            tagPrincipal.put("hologramas", listaHologramas);
            NbtIo.writeCompressed(tagPrincipal, arquivo.toPath());
        } catch (IOException e) {
            LOGGER.error("[AelysiumHolograma] Erro ao salvar hologramas!", e);
        }
    }

    public void carregarHologramas() {
        if (servidor == null) return;
        try {
            File arquivo = new File(servidor.getWorldPath(LevelResource.ROOT).toFile(), NOME_ARQUIVO);
            if (!arquivo.exists()) return;

            CompoundTag tagPrincipal = NbtIo.readCompressed(arquivo.toPath(), NbtAccounter.unlimitedHeap());
            ListTag listaHologramas = tagPrincipal.getList("hologramas", 10);

            hologramas.clear();

            for (int i = 0; i < listaHologramas.size(); i++) {
                Holograma holo = Holograma.carregarNBT(listaHologramas.getCompound(i));
                hologramas.put(holo.getNome().toLowerCase(), holo);
            }

            LOGGER.info("[AelysiumHolograma] {} hologramas carregados.", hologramas.size());
        } catch (IOException e) {
            LOGGER.error("[AelysiumHolograma] Erro ao carregar hologramas!", e);
        }
    }
}