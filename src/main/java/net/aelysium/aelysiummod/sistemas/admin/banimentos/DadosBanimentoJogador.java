package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.mojang.logging.LogUtils;
import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.aelysium.aelysiummod.sistemas.admin.historico.FormatoDataHistorico;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DadosBanimentoJogador extends DadosEmArquivos {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "banlist_players";

    public record Liberacao(String por, String porUuid, String em) {
        public static final Liberacao DESCONHECIDA = new Liberacao("desconhecido", "", "");
    }

    private final Map<UUID, Map<ResourceLocation, Liberacao>> playerAllowedItems = new ConcurrentHashMap<>();

    public DadosBanimentoJogador() {
    }

    public static DadosBanimentoJogador load(CompoundTag tag, HolderLookup.Provider provider) {
        DadosBanimentoJogador data = new DadosBanimentoJogador();

        CompoundTag players = tag.getCompound("players");
        for (String uuidStr : players.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<ResourceLocation, Liberacao> items = new ConcurrentHashMap<>();
                for (Tag elemento : players.get(uuidStr) instanceof ListTag lista ? lista : new ListTag()) {
                    if (elemento instanceof StringTag texto) {
                        ResourceLocation rl = ResourceLocation.tryParse(texto.getAsString());
                        if (rl != null) items.put(rl, Liberacao.DESCONHECIDA);
                    } else if (elemento instanceof CompoundTag composto) {
                        ResourceLocation rl = ResourceLocation.tryParse(composto.getString("item"));
                        if (rl != null) {
                            items.put(rl, new Liberacao(
                                    composto.contains("por") ? composto.getString("por") : "desconhecido",
                                    composto.getString("porUuid"),
                                    composto.getString("em")));
                        }
                    }
                }
                data.playerAllowedItems.put(uuid, items);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("[Banlist] UUID inválido nos dados: {}", uuidStr);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag players = new CompoundTag();

        for (Map.Entry<UUID, Map<ResourceLocation, Liberacao>> entry : playerAllowedItems.entrySet()) {
            ListTag list = new ListTag();
            entry.getValue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(item -> {
                        CompoundTag composto = new CompoundTag();
                        composto.putString("item", item.getKey().toString());
                        composto.putString("por", item.getValue().por());
                        composto.putString("porUuid", item.getValue().porUuid());
                        composto.putString("em", item.getValue().em());
                        list.add(composto);
                    });
            players.put(entry.getKey().toString(), list);
        }

        tag.put("players", players);
        return tag;
    }

    public boolean isAllowed(UUID playerId, ResourceLocation itemId) {
        Map<ResourceLocation, Liberacao> allowed = playerAllowedItems.get(playerId);
        return allowed != null && allowed.containsKey(itemId);
    }

    public boolean allowItem(UUID playerId, ResourceLocation itemId) {
        return allowItem(playerId, itemId, "desconhecido", null);
    }

    public boolean allowItem(UUID playerId, ResourceLocation itemId, String por, UUID porUuid) {
        Map<ResourceLocation, Liberacao> items = playerAllowedItems.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        if (items.containsKey(itemId)) return false;
        items.put(itemId, new Liberacao(por, porUuid == null ? "" : porUuid.toString(), FormatoDataHistorico.agora()));
        setDirty();
        return true;
    }

    public boolean revokeItem(UUID playerId, ResourceLocation itemId) {
        Map<ResourceLocation, Liberacao> items = playerAllowedItems.get(playerId);
        if (items == null) return false;
        boolean removed = items.remove(itemId) != null;
        if (removed) {
            if (items.isEmpty()) playerAllowedItems.remove(playerId);
            setDirty();
        }
        return removed;
    }

    public Set<ResourceLocation> getAllowedItems(UUID playerId) {
        Map<ResourceLocation, Liberacao> items = playerAllowedItems.get(playerId);
        return items != null ? Collections.unmodifiableSet(items.keySet()) : Collections.emptySet();
    }

    public Map<ResourceLocation, Liberacao> getLiberacoes(UUID playerId) {
        Map<ResourceLocation, Liberacao> items = playerAllowedItems.get(playerId);
        return items != null ? Collections.unmodifiableMap(items) : Collections.emptyMap();
    }

    public static DadosBanimentoJogador get(MinecraftServer server) {
        return obter(server, new Factory<>(DadosBanimentoJogador::new, DadosBanimentoJogador::load), DATA_NAME,
                DadosEmArquivos.Destino.porJogador("itens_banidos/jogadores", "players=itens_liberados"));
    }
}
