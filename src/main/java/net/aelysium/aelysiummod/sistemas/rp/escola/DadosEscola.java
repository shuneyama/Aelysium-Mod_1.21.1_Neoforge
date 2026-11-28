package net.aelysium.aelysiummod.sistemas.rp.escola;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DadosEscola extends DadosEmArquivos {

    private static final String NOME_DADOS = "aelysium_escola";

    private final Map<UUID, List<ElementoMagico>> escolas = new HashMap<>();

    public static DadosEscola get(MinecraftServer servidor) {
        return obter(servidor, new Factory<>(DadosEscola::new, DadosEscola::carregar), NOME_DADOS,
                DadosEmArquivos.Destino.porJogador("perfis_sociais", "escolas=escola"));
    }

    private static DadosEscola carregar(CompoundTag tag, HolderLookup.Provider provedor) {
        DadosEscola dados = new DadosEscola();
        CompoundTag escolas = tag.getCompound("escolas");
        for (String chave : escolas.getAllKeys()) {
            try {
                List<ElementoMagico> elementos = new ArrayList<>();
                for (Tag item : escolas.getList(chave, Tag.TAG_STRING)) {
                    ElementoMagico elemento = ElementoMagico.doId(item.getAsString());
                    if (elemento != null) elementos.add(elemento);
                }
                dados.escolas.put(UUID.fromString(chave), elementos);
            } catch (IllegalArgumentException ignorado) {
            }
        }
        return dados;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provedor) {
        CompoundTag escolas = new CompoundTag();
        this.escolas.forEach((uuid, elementos) -> {
            ListTag lista = new ListTag();
            for (ElementoMagico elemento : elementos) lista.add(StringTag.valueOf(elemento.id()));
            escolas.put(uuid.toString(), lista);
        });
        tag.put("escolas", escolas);
        return tag;
    }

    public List<ElementoMagico> elementos(UUID uuid) {
        return escolas.getOrDefault(uuid, List.of());
    }

    public boolean possui(UUID uuid, ElementoMagico elemento) {
        List<ElementoMagico> elementos = elementos(uuid);
        if (elemento == ElementoMagico.NENHUMA) return elementos.isEmpty() || elementos.contains(elemento);
        return elementos.contains(elemento);
    }

    public void definir(UUID uuid, List<ElementoMagico> elementos) {
        escolas.put(uuid, List.copyOf(elementos));
        setDirty();
    }
}
