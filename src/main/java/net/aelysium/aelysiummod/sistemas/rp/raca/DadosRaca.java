package net.aelysium.aelysiummod.sistemas.rp.raca;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;

public class DadosRaca extends DadosEmArquivos {

    private static final String DATA_NAME = "aelysium_race_data";

    private final Map<UUID, TipoRaca> playerRaces = new HashMap<>();

    public static DadosRaca get(ServerLevel level) {
        return obter(level.getServer(), new Factory<>(DadosRaca::new, DadosRaca::load), DATA_NAME, DadosEmArquivos.Destino.porJogador("perfis_sociais", "races=raca"));
    }

    public TipoRaca getRace(UUID uuid) {
        return playerRaces.getOrDefault(uuid, TipoRaca.NONE);
    }

    public void setRace(UUID uuid, TipoRaca race) {
        playerRaces.put(uuid, race);
        setDirty();
        net.aelysium.aelysiummod.sistemas.social.insignia.DadosInsignia.aoMudarRaca(uuid);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag races = new CompoundTag();
        playerRaces.forEach((uuid, race) -> races.putString(uuid.toString(), race.id));
        tag.put("races", races);
        return tag;
    }

    public static DadosRaca load(CompoundTag tag, HolderLookup.Provider registries) {
        DadosRaca data = new DadosRaca();
        CompoundTag races = tag.getCompound("races");
        for (String key : races.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                TipoRaca race = TipoRaca.fromId(races.getString(key));
                data.playerRaces.put(uuid, race);
            } catch (IllegalArgumentException ignored) {}
        }
        return data;
    }
}