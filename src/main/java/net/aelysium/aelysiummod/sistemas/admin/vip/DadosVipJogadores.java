package net.aelysium.aelysiummod.sistemas.admin.vip;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class DadosVipJogadores extends DadosEmArquivos {

    private static final String NOME_DADOS = "aelysium_vip_jogadores";

    private final Map<UUID, Set<String>> vips = new HashMap<>();

    public static DadosVipJogadores get(MinecraftServer servidor) {
        return obter(servidor, new Factory<>(DadosVipJogadores::new, DadosVipJogadores::carregar), NOME_DADOS,
                DadosEmArquivos.Destino.porJogador("perfis_sociais", "vips=vip"));
    }

    private static DadosVipJogadores carregar(CompoundTag tag, HolderLookup.Provider provedor) {
        DadosVipJogadores dados = new DadosVipJogadores();
        CompoundTag vips = tag.getCompound("vips");
        for (String chave : vips.getAllKeys()) {
            try {
                Set<String> nomes = novoConjunto();
                for (Tag item : vips.getList(chave, Tag.TAG_STRING)) nomes.add(item.getAsString());
                dados.vips.put(UUID.fromString(chave), nomes);
            } catch (IllegalArgumentException ignorado) {
            }
        }
        return dados;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provedor) {
        CompoundTag vips = new CompoundTag();
        this.vips.forEach((uuid, nomes) -> {
            if (nomes.isEmpty()) return;
            ListTag lista = new ListTag();
            for (String nome : nomes) lista.add(StringTag.valueOf(nome));
            vips.put(uuid.toString(), lista);
        });
        tag.put("vips", vips);
        return tag;
    }

    private static Set<String> novoConjunto() {
        return new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    }

    public Set<String> vips(UUID uuid) {
        return Collections.unmodifiableSet(vips.getOrDefault(uuid, Set.of()));
    }

    public boolean possui(UUID uuid, String vip) {
        Set<String> nomes = vips.get(uuid);
        return nomes != null && nomes.contains(vip);
    }

    public void conceder(UUID uuid, String vip) {
        if (vips.computeIfAbsent(uuid, chave -> novoConjunto()).add(vip)) setDirty();
    }

    public boolean revogar(UUID uuid, String vip) {
        Set<String> nomes = vips.get(uuid);
        if (nomes == null || !nomes.remove(vip)) return false;
        setDirty();
        return true;
    }
}
