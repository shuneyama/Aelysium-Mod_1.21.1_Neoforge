package net.aelysium.aelysiummod.sistemas.midia.holograma;

import com.mojang.logging.LogUtils;
import net.aelysium.aelysiummod.nucleo.armazenamento.PastaDeEntidades;
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

    private java.nio.file.Path pasta() {
        return servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("hologramas");
    }

    public void salvarHologramas() {
        if (servidor == null) return;
        Map<String, CompoundTag> arquivos = new LinkedHashMap<>();
        for (Holograma holo : hologramas.values()) arquivos.put(holo.getNome(), holo.salvarNBT());
        PastaDeEntidades.gravarTodos(pasta(), arquivos);
    }

    public void carregarHologramas() {
        if (servidor == null) return;
        List<CompoundTag> tags = new ArrayList<>(PastaDeEntidades.ler(pasta()).values());
        boolean migrou = false;
        if (tags.isEmpty()) {
            tags = PastaDeEntidades.lerDatAntigo(
            servidor.getWorldPath(LevelResource.ROOT).resolve(NOME_ARQUIVO), "hologramas");
            migrou = !tags.isEmpty();
        }

        hologramas.clear();
        for (CompoundTag tag : tags) {
            Holograma holo = Holograma.carregarNBT(tag);
            hologramas.put(holo.getNome().toLowerCase(), holo);
        }
        if (migrou) {
            salvarHologramas();
            PastaDeEntidades.apagarDatMigrado(servidor.getWorldPath(LevelResource.ROOT).resolve(NOME_ARQUIVO), pasta(), tags.size());
        }

        LOGGER.info("[AelysiumHolograma] {} hologramas carregados.", hologramas.size());
    }
}