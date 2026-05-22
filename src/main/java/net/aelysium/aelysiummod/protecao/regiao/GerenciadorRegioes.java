package net.aelysium.aelysiummod.protecao.regiao;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GerenciadorRegioes {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static GerenciadorRegioes instance;

    private final Map<String, Regiao> regioes = new ConcurrentHashMap<>();
    private final Map<Long, Regiao> cachePosicao = new ConcurrentHashMap<>();
    private static final int MAX_CACHE = 2048;
    private MinecraftServer servidor;
    private volatile boolean precisaSalvar = false;

    private static final String NOME_ARQUIVO = "aelysium_regioes.dat";

    private GerenciadorRegioes() {}

    public static GerenciadorRegioes getInstance() {
        if (instance == null) {
            instance = new GerenciadorRegioes();
        }
        return instance;
    }

    public void setServidor(MinecraftServer servidor) {
        this.servidor = servidor;
        carregarRegioes();
    }

    public void adicionarRegiao(Regiao regiao) {
        regioes.put(regiao.getNome(), regiao);
        cachePosicao.clear();
        salvarRegioes();
    }

    public void removerRegiao(String nome) {
        regioes.remove(nome);
        cachePosicao.clear();
        salvarRegioes();
    }

    public Regiao getRegiao(String nome) {
        return regioes.get(nome);
    }

    public Collection<Regiao> getTodasRegioes() {
        return regioes.values();
    }

    public List<String> getNomesRegioes() {
        return new ArrayList<>(regioes.keySet());
    }

    public boolean existeRegiao(String nome) {
        return regioes.containsKey(nome);
    }

    public Regiao getRegiaoMaisEspecifica(BlockPos pos) {
        long chave = pos.asLong();

        Regiao cached = cachePosicao.get(chave);
        if (cached != null) return cached;

        Regiao resultado = null;
        int menorVolume = Integer.MAX_VALUE;

        for (Regiao regiao : regioes.values()) {
            if (regiao.contemPosicao(pos)) {
                int volume = regiao.getVolume();
                if (volume < menorVolume) {
                    menorVolume = volume;
                    resultado = regiao;
                }
            }
        }

        if (cachePosicao.size() >= MAX_CACHE) {
            cachePosicao.clear();
        }
        if (resultado != null) {
            cachePosicao.put(chave, resultado);
        }

        return resultado;
    }

    public void marcarParaSalvar() {
        this.precisaSalvar = true;
    }

    public void salvarSeNecessario() {
        if (precisaSalvar) {
            salvarRegioes();
            precisaSalvar = false;
        }
    }

    public void salvarRegioes() {
        if (servidor == null) return;
        try {
            File arquivo = new File(servidor.getWorldPath(LevelResource.ROOT).toFile(), NOME_ARQUIVO);
            CompoundTag tagPrincipal = new CompoundTag();
            ListTag listaRegioes = new ListTag();

            for (Regiao regiao : regioes.values()) {
                listaRegioes.add(regiao.salvarNBT());
            }

            tagPrincipal.put("regioes", listaRegioes);
            NbtIo.writeCompressed(tagPrincipal, arquivo.toPath());
        } catch (IOException e) {
            LOGGER.error("[AelysiumProtect] Erro ao salvar regiões!", e);
        }
    }

    public void carregarRegioes() {
        if (servidor == null) return;
        try {
            File arquivo = new File(servidor.getWorldPath(LevelResource.ROOT).toFile(), NOME_ARQUIVO);
            if (!arquivo.exists()) return;

            CompoundTag tagPrincipal = NbtIo.readCompressed(arquivo.toPath(), NbtAccounter.unlimitedHeap());
            ListTag listaRegioes = tagPrincipal.getList("regioes", 10);

            regioes.clear();
            cachePosicao.clear();

            for (int i = 0; i < listaRegioes.size(); i++) {
                Regiao regiao = Regiao.carregarNBT(listaRegioes.getCompound(i));
                regioes.put(regiao.getNome(), regiao);
            }

            LOGGER.info("[AelysiumProtect] {} regiões carregadas.", regioes.size());
        } catch (IOException e) {
            LOGGER.error("[AelysiumProtect] Erro ao carregar regiões!", e);
        }
    }

    public void limparCache() {
        cachePosicao.clear();
    }
}
