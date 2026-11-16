package net.aelysium.aelysiummod.sistemas.admin.protecao.regiao;

import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteSyncRegioesCliente;
import net.minecraft.core.BlockPos;
import net.aelysium.aelysiummod.nucleo.armazenamento.PastaDeEntidades;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GerenciadorRegioes {

    private static GerenciadorRegioes instance;

    private final Map<String, Regiao> regioes = new ConcurrentHashMap<>();
    private final Map<String, Regiao> cachePosicao = new ConcurrentHashMap<>();
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

    public MinecraftServer getServidor() {
        return servidor;
    }

    public void adicionarRegiao(Regiao regiao) {
        regioes.put(regiao.getNome(), regiao);
        cachePosicao.clear();
        salvarRegioes();
        sincronizarTodos();
    }

    public void removerRegiao(String nome) {
        regioes.remove(nome);
        cachePosicao.clear();
        salvarRegioes();
        sincronizarTodos();
    }

    public void marcarAlteracao() {
        cachePosicao.clear();
        salvarRegioes();
        sincronizarTodos();
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

    public Regiao getRegiaoMaisEspecifica(BlockPos pos, ResourceLocation dim) {
        String chave = (dim == null ? "?" : dim.toString()) + ":" + pos.asLong();

        Regiao cached = cachePosicao.get(chave);
        if (cached != null) return cached;

        Regiao resultado = null;
        int menorVolume = Integer.MAX_VALUE;

        for (Regiao regiao : regioes.values()) {
            if (regiao.contemPosicao(pos, dim)) {
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

    public Regiao getRegiaoMaisEspecifica(BlockPos pos) {
        return getRegiaoMaisEspecifica(pos, null);
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

    private java.nio.file.Path pasta() {
        return servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("regioes");
    }

    public void salvarRegioes() {
        if (servidor == null) return;
        Map<String, CompoundTag> arquivos = new LinkedHashMap<>();
        for (Regiao regiao : regioes.values()) arquivos.put(regiao.getNome(), regiao.salvarNBT());
        PastaDeEntidades.gravarTodos(pasta(), arquivos);
    }

    public void carregarRegioes() {
        if (servidor == null) return;
        List<CompoundTag> tags = new ArrayList<>(PastaDeEntidades.ler(pasta()).values());
        boolean migrou = false;
        if (tags.isEmpty()) {
            tags = PastaDeEntidades.lerDatAntigo(
            servidor.getWorldPath(LevelResource.ROOT).resolve(NOME_ARQUIVO), "regioes");
            migrou = !tags.isEmpty();
        }

        regioes.clear();
        cachePosicao.clear();
        for (CompoundTag tag : tags) {
            Regiao regiao = Regiao.carregarNBT(tag);
            regioes.put(regiao.getNome(), regiao);
        }
        if (migrou) {
            salvarRegioes();
            PastaDeEntidades.apagarDatMigrado(servidor.getWorldPath(LevelResource.ROOT).resolve(NOME_ARQUIVO), pasta(), tags.size());
        }
    }

    public void limparCache() {
        cachePosicao.clear();
    }

    public PacoteSyncRegioesCliente construirPacoteSync() {
        List<PacoteSyncRegioesCliente.RegiaoMin> lista = new ArrayList<>();
        for (Regiao r : regioes.values()) {
            lista.add(new PacoteSyncRegioesCliente.RegiaoMin(
            r.getNome(),
            r.getPosicaoMinima(),
            r.getPosicaoMaxima(),
            r.getDimensao(),
            r.isDomoAtivo(),
            r.getDomoCor(),
            r.getDomoDistancia()
            ));
        }
        return new PacoteSyncRegioesCliente(lista);
    }

    public void sincronizarTodos() {
        if (servidor == null) return;
        PacoteSyncRegioesCliente pacote = construirPacoteSync();
        for (ServerPlayer p : servidor.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(p, pacote);
        }
    }

    public void sincronizarJogador(ServerPlayer jogador) {
        if (servidor == null) return;
        PacketDistributor.sendToPlayer(jogador, construirPacoteSync());
    }
}
