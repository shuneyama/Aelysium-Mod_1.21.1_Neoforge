package net.aelysium.aelysiummod.sistemas.rp.vidas;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DadosVidas extends DadosEmArquivos {

    private static final String DATA_NAME = "aelysium_lives";
    private static final long EXPIRACAO_OFFLINE_MS = 10 * 60 * 1000L;

    public static final int COR_LIVE = 0xAA00FF;

    private final Map<UUID, EntradaVida> lives = new LinkedHashMap<>();

    public record EntradaVida(String link, long offlineDesde) {
        public EntradaVida comOffline(long timestamp) {
            return new EntradaVida(link, timestamp);
        }

        public EntradaVida online() {
            return new EntradaVida(link, -1L);
        }
    }

    public static DadosVidas get(MinecraftServer server) {
        return obter(server, new Factory<>(DadosVidas::new, DadosVidas::load), DATA_NAME, DadosEmArquivos.Destino.porJogador("perfis_sociais", "lives=live"));
    }

    private static DadosVidas load(CompoundTag tag, HolderLookup.Provider provider) {
        DadosVidas data = new DadosVidas();
        CompoundTag entries = tag.getCompound("lives");
        for (String uuidStr : entries.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag e = entries.getCompound(uuidStr);
                data.lives.put(uuid, new EntradaVida(e.getString("link"), e.getLong("offlineDesde")));
            } catch (Exception ignored) {}
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag entries = new CompoundTag();
        for (Map.Entry<UUID, EntradaVida> entry : lives.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("link", entry.getValue().link());
            e.putLong("offlineDesde", entry.getValue().offlineDesde());
            entries.put(entry.getKey().toString(), e);
        }
        tag.put("lives", entries);
        return tag;
    }

    public void publicar(UUID uuid, String link) {
        lives.put(uuid, new EntradaVida(link, -1L));
        setDirty();
    }

    public void encerrar(UUID uuid) {
        if (lives.remove(uuid) != null) setDirty();
    }

    public void marcarOffline(UUID uuid) {
        EntradaVida entry = lives.get(uuid);
        if (entry != null && entry.offlineDesde() < 0) {
            lives.put(uuid, entry.comOffline(System.currentTimeMillis()));
            setDirty();
        }
    }

    public void marcarOnline(UUID uuid) {
        EntradaVida entry = lives.get(uuid);
        if (entry != null && entry.offlineDesde() >= 0) {
            lives.put(uuid, entry.online());
            setDirty();
        }
    }

    public boolean isLive(UUID uuid) {
        return getLinkSeAtiva(uuid) != null;
    }

    public String getLinkSeAtiva(UUID uuid) {
        EntradaVida entry = lives.get(uuid);
        if (entry == null) return null;
        if (entry.offlineDesde() >= 0 && System.currentTimeMillis() - entry.offlineDesde() > EXPIRACAO_OFFLINE_MS) {
            lives.remove(uuid);
            setDirty();
            return null;
        }
        return entry.link();
    }

    public Map<UUID, String> getLivesAtivas() {
        Map<UUID, String> ativas = new LinkedHashMap<>();
        for (UUID uuid : new java.util.ArrayList<>(lives.keySet())) {
            String link = getLinkSeAtiva(uuid);
            if (link != null) ativas.put(uuid, link);
        }
        return ativas;
    }
}
