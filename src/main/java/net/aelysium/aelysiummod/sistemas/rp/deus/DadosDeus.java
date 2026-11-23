package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;

public class DadosDeus extends DadosEmArquivos {

    private static final String DATA_NAME = "aelysium_deus_data";

    private final Map<UUID, TipoDeus> playerDeus = new HashMap<>();

    public static DadosDeus get(ServerLevel level) {
        return obter(level.getServer(), new Factory<>(DadosDeus::new, DadosDeus::load), DATA_NAME, DadosEmArquivos.Destino.porJogador("perfis_sociais", "deuses=deus"));
    }

    public TipoDeus getDeus(UUID uuid) {
        return playerDeus.getOrDefault(uuid, TipoDeus.NONE);
    }

    public Map<UUID, TipoDeus> todos() {
        return new HashMap<>(playerDeus);
    }

    public void setDeus(UUID uuid, TipoDeus deus) {
        playerDeus.put(uuid, deus);
        UtilDeus.atualizar(uuid, deus != TipoDeus.NONE);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag deuses = new CompoundTag();
        playerDeus.forEach((uuid, deus) -> deuses.putString(uuid.toString(), deus.id));
        tag.put("deuses", deuses);
        return tag;
    }

    public static DadosDeus load(CompoundTag tag, HolderLookup.Provider registries) {
        DadosDeus data = new DadosDeus();
        CompoundTag deuses = tag.getCompound("deuses");
        for (String key : deuses.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                TipoDeus deus = TipoDeus.fromId(deuses.getString(key));
                data.playerDeus.put(uuid, deus);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }
}