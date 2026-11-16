package net.aelysium.aelysiummod.sistemas.admin.protecao.cliente;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class RegiaoCacheCliente {

    public record RegiaoEntry(String nome, BlockPos min, BlockPos max, ResourceLocation dimensao,
                               boolean domoAtivo, int domoCor, float domoDistancia) {}

    private static final List<RegiaoEntry> entries = new ArrayList<>();

    private static int versao;
    private static int versaoDomosCache = -1;
    private static ResourceLocation dimensaoDomosCache;
    private static List<RegiaoEntry> domosCache = List.of();

    public static synchronized void setEntries(List<RegiaoEntry> novas) {
        entries.clear();
        entries.addAll(novas);
        versao++;
    }

    public static synchronized List<RegiaoEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public static synchronized List<RegiaoEntry> getEntriesPorDimensao(ResourceLocation dim) {
        List<RegiaoEntry> filtradas = new ArrayList<>();
        for (RegiaoEntry e : entries) {
            if (e.dimensao() != null && e.dimensao().equals(dim)) {
                filtradas.add(e);
            }
        }
        return filtradas;
    }

    public static synchronized List<RegiaoEntry> getDomosAtivos(ResourceLocation dim) {
        if (versaoDomosCache != versao || !dim.equals(dimensaoDomosCache)) {
            List<RegiaoEntry> filtradas = new ArrayList<>();
            for (RegiaoEntry e : entries) {
                if (e.domoAtivo() && e.dimensao() != null && e.dimensao().equals(dim)) {
                    filtradas.add(e);
                }
            }
            domosCache = List.copyOf(filtradas);
            versaoDomosCache = versao;
            dimensaoDomosCache = dim;
        }
        return domosCache;
    }

    public static synchronized void clear() {
        entries.clear();
        versao++;
    }
}
