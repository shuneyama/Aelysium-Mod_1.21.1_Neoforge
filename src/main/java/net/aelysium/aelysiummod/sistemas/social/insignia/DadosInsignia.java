package net.aelysium.aelysiummod.sistemas.social.insignia;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.aelysium.aelysiummod.nucleo.jogador.ArquivosJogador;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DadosInsignia extends DadosEmArquivos {

    private static final String DATA_NAME = "aelysium_badges";
    public static final String NONE = "";
    public static final String NENHUMA = "nenhuma";

    private static DadosInsignia ultima;

    private record Entrada(String manual, String atual, String origem) {}

    private final Map<UUID, Entrada> entradas = new LinkedHashMap<>();

    public static DadosInsignia get(MinecraftServer server) {
        DadosInsignia dados = obter(server, new Factory<>(DadosInsignia::new, DadosInsignia::load), DATA_NAME,
                DadosEmArquivos.Destino.porJogador("perfis_sociais", "overrides=insignia"));
        ultima = dados;
        return dados;
    }

    private static DadosInsignia load(CompoundTag tag, HolderLookup.Provider provider) {
        DadosInsignia data = new DadosInsignia();
        CompoundTag overrides = tag.getCompound("overrides");
        for (String uuidStr : overrides.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                if (overrides.get(uuidStr) instanceof StringTag texto) {
                    data.entradas.put(uuid, new Entrada(texto.getAsString(), "", ""));
                } else {
                    CompoundTag e = overrides.getCompound(uuidStr);
                    String manual = e.contains("manual") ? e.getString("manual") : null;
                    data.entradas.put(uuid, new Entrada(manual, e.getString("atual"), e.getString("origem")));
                }
            } catch (Exception ignored) {}
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag overridesTag = new CompoundTag();
        for (Map.Entry<UUID, Entrada> entry : entradas.entrySet()) {
            String[] efetiva = calcular(entry.getKey(), entry.getValue().manual());
            CompoundTag e = new CompoundTag();
            if (entry.getValue().manual() != null) e.putString("manual", entry.getValue().manual());
            e.putString("atual", efetiva[0]);
            e.putString("origem", efetiva[1]);
            overridesTag.put(entry.getKey().toString(), e);
        }
        tag.put("overrides", overridesTag);
        return tag;
    }

    private static String[] calcular(UUID uuid, String manual) {
        if (manual != null) {
            return manual.equals(NONE) ? new String[]{NENHUMA, "manual"} : new String[]{manual, "manual"};
        }
        if (ArquivosJogador.ehOp(uuid)) return new String[]{NENHUMA, "op"};
        try {
            TipoRaca raca = PerfilJogador.raca().raca(uuid);
            TipoInsignia porRaca = raca == null ? null : TipoInsignia.forRace(raca);
            if (porRaca != null) return new String[]{porRaca.id, "raca"};
        } catch (Exception ignored) {}
        return new String[]{NENHUMA, NENHUMA};
    }

    public void registrar(UUID uuid) {
        if (!entradas.containsKey(uuid)) {
            entradas.put(uuid, new Entrada(null, "", ""));
        }
        setDirty();
    }

    public static void aoMudarRaca(UUID uuid) {
        if (ultima != null) ultima.registrar(uuid);
    }

    public String getOverride(UUID uuid) {
        Entrada entrada = entradas.get(uuid);
        return entrada == null ? null : entrada.manual();
    }

    public void setOverride(UUID uuid, String value) {
        Entrada atual = entradas.get(uuid);
        entradas.put(uuid, new Entrada(value, atual == null ? "" : atual.atual(), atual == null ? "" : atual.origem()));
        setDirty();
    }

    public void limparPorId(String id) {
        boolean mudou = false;
        for (Map.Entry<UUID, Entrada> entry : entradas.entrySet()) {
            if (id.equals(entry.getValue().manual())) {
                entry.setValue(new Entrada(null, entry.getValue().atual(), entry.getValue().origem()));
                mudou = true;
            }
        }
        if (mudou) setDirty();
    }

    public void clearOverride(UUID uuid) {
        setOverride(uuid, null);
    }
}
