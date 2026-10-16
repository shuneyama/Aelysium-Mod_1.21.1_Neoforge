package net.aelysium.aelysiummod.sistemas.admin.protecao.cliente;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class RegiaoCacheCliente {

    public record RegiaoEntry(String nome, BlockPos min, BlockPos max, ResourceLocation dimensao,
                               boolean domoAtivo, int domoCor, float domoDistancia) {}

    private static final List<RegiaoEntry> entries = new ArrayList<>();

    public static synchronized void setEntries(List<RegiaoEntry> novas) {
        entries.clear();
        entries.addAll(novas);
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

    public static synchronized void clear() {
        entries.clear();
    }
}