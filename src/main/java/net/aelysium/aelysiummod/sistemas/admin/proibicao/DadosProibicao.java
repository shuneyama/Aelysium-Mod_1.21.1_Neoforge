package net.aelysium.aelysiummod.sistemas.admin.proibicao;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class DadosProibicao extends DadosEmArquivos {

    private static final String DATA_NAME = "aelysium_proibicao";

    private final Set<String> jogadoresProibidos = new HashSet<>();
    private final Set<String> dimensoesProibidas = new HashSet<>();

    public static DadosProibicao get(MinecraftServer server) {
        return obter(server, new Factory<>(DadosProibicao::new, DadosProibicao::load), DATA_NAME, DadosEmArquivos.Destino.global("proibicao", "estado.json"));
    }

    private static DadosProibicao load(CompoundTag tag, HolderLookup.Provider provider) {
        DadosProibicao data = new DadosProibicao();

        CompoundTag proibidos = tag.getCompound("proibidos");
        for (String nome : proibidos.getAllKeys()) {
            data.jogadoresProibidos.add(nome);
        }

        CompoundTag dimensoes = tag.getCompound("dimensoes_proibidas");
        for (String id : dimensoes.getAllKeys()) {
            data.dimensoesProibidas.add(id);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag proibidos = new CompoundTag();
        for (String nome : jogadoresProibidos) {
            proibidos.putBoolean(nome, true);
        }
        tag.put("proibidos", proibidos);

        CompoundTag dimensoes = new CompoundTag();
        for (String id : dimensoesProibidas) {
            dimensoes.putBoolean(id, true);
        }
        tag.put("dimensoes_proibidas", dimensoes);

        return tag;
    }

    public boolean proibir(String nome) {
        boolean adicionou = jogadoresProibidos.add(nome.toLowerCase());
        if (adicionou) setDirty();
        return adicionou;
    }

    public boolean desproibir(String nome) {
        boolean removeu = jogadoresProibidos.remove(nome.toLowerCase());
        if (removeu) setDirty();
        return removeu;
    }

    public boolean estaProibido(String nome) {
        return jogadoresProibidos.contains(nome.toLowerCase());
    }

    public boolean proibirDimensao(String id) {
        boolean adicionou = dimensoesProibidas.add(id.toLowerCase());
        if (adicionou) setDirty();
        return adicionou;
    }

    public boolean desproibirDimensao(String id) {
        boolean removeu = dimensoesProibidas.remove(id.toLowerCase());
        if (removeu) setDirty();
        return removeu;
    }

    public boolean dimensaoProibida(String id) {
        return dimensoesProibidas.contains(id.toLowerCase());
    }
}
