package net.aelysium.aelysiummod.sistemas.social.apelido;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedeApelido;

public class DadosApelido extends DadosEmArquivos {

    private static final String DATA_NAME = "aelysium_nicknames";

    private final Map<UUID, EntradaApelido> nicknames = new HashMap<>();
    private final Map<String, UUID> nickToUUID = new HashMap<>();
    private final Map<UUID, String> pronomes = new HashMap<>();
    private MinecraftServer server;

    public DadosApelido() { super(); }

    public static DadosApelido get(MinecraftServer server) {
        DadosApelido data = obter(server, new Factory<>(DadosApelido::new, DadosApelido::load), DATA_NAME, DadosEmArquivos.Destino.porJogador("perfis_sociais", "nicknames=apelido", "pronomes=pronomes")
                        .comFormato("nicknames", new FormatoApelido()));
        data.server = server;
        return data;
    }

    private static DadosApelido load(CompoundTag tag, HolderLookup.Provider provider) {
        DadosApelido data = new DadosApelido();
        CompoundTag nicks = tag.getCompound("nicknames");
        for (String uuidStr : nicks.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag e = nicks.getCompound(uuidStr);
                EntradaApelido entry = new EntradaApelido(
                        e.getString("prefix"),
                        e.getInt("prefixCor1"), e.getInt("prefixCor2"), e.getInt("prefixFormat"),
                        e.getString("nick"),
                        e.getInt("nickCor"), e.getInt("nickFormat"),
                        e.getString("suffix"),
                        e.getInt("suffixCor1"), e.getInt("suffixCor2"), e.getInt("suffixFormat")
                );
                data.nicknames.put(uuid, entry);
                if (!entry.nick.isEmpty()) {
                    data.nickToUUID.put(chave(entry.nick), uuid);
                }
            } catch (Exception ignored) {}
        }
        CompoundTag pron = tag.getCompound("pronomes");
        for (String uuidStr : pron.getAllKeys()) {
            try {
                String valor = pron.getString(uuidStr);
                if (!valor.isEmpty()) data.pronomes.put(UUID.fromString(uuidStr), valor);
            } catch (Exception ignored) {}
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag nicks = new CompoundTag();
        for (Map.Entry<UUID, EntradaApelido> entry : nicknames.entrySet()) {
            CompoundTag e = new CompoundTag();
            EntradaApelido n = entry.getValue();
            e.putString("prefix", n.prefix);
            e.putInt("prefixCor1", n.prefixCor1);
            e.putInt("prefixCor2", n.prefixCor2);
            e.putInt("prefixFormat", n.prefixFormat);
            e.putString("nick", n.nick);
            e.putInt("nickCor", n.nickCor);
            e.putInt("nickFormat", n.nickFormat);
            e.putString("suffix", n.suffix);
            e.putInt("suffixCor1", n.suffixCor1);
            e.putInt("suffixCor2", n.suffixCor2);
            e.putInt("suffixFormat", n.suffixFormat);
            nicks.put(entry.getKey().toString(), e);
        }
        tag.put("nicknames", nicks);
        CompoundTag pron = new CompoundTag();
        for (Map.Entry<UUID, String> entry : pronomes.entrySet()) {
            pron.putString(entry.getKey().toString(), entry.getValue());
        }
        tag.put("pronomes", pron);
        return tag;
    }

    public static String normalizarPronomes(String bruto) {
        if (bruto == null) return "";
        String texto = bruto.trim();
        if (texto.isEmpty()) return "";
        Pronome pronome = Pronome.deId(texto);
        return pronome == null ? null : pronome.id;
    }

    public String getPronomes(UUID uuid) {
        return pronomes.getOrDefault(uuid, "");
    }

    public void setPronomes(UUID uuid, String valor) {
        if (valor == null || valor.isEmpty()) pronomes.remove(uuid);
        else pronomes.put(uuid, valor);
        setDirty();
    }

    public MutableComponent getStyledFullNameComPronomes(UUID uuid) {
        MutableComponent base = getStyledFullName(uuid);
        String valor = pronomes.get(uuid);
        if (valor == null || valor.isEmpty()) return base;
        if (base == null) base = Component.literal(resolverNomeExibicao(server, uuid));
        Pronome pronome = Pronome.deId(valor);
        String texto = pronome != null ? pronome.texto : valor;
        int cor = pronome != null ? pronome.cor : 0xAAAAAA;
        return base.append(Component.literal(" ")).append(Component.literal("(" + texto + ")")
                .withStyle(estilo -> estilo.withColor(net.minecraft.network.chat.TextColor.fromRgb(cor))));
    }

    private void notificarHistorico(UUID uuid) {
        if (server == null) return;
        net.aelysium.aelysiummod.sistemas.admin.historico.DadosHistorico.aoApelidoAlterado(server, uuid);
    }

    public void setNickname(UUID uuid, EntradaApelido entry) {
        EntradaApelido old = nicknames.get(uuid);
        if (old != null && !old.nick.isEmpty()) {
            nickToUUID.remove(chave(old.nick));
        }
        nicknames.put(uuid, entry);
        notificarHistorico(uuid);
        if (!entry.nick.isEmpty()) {
            nickToUUID.put(chave(entry.nick), uuid);
        }
        setDirty();
    }

    public void setNickSimple(UUID uuid, String nick) {
        EntradaApelido old = nicknames.get(uuid);
        if (old != null) {
            setNickname(uuid, new EntradaApelido(
                    old.prefix, old.prefixCor1, old.prefixCor2, old.prefixFormat,
                    nick, old.nickCor, old.nickFormat,
                    old.suffix, old.suffixCor1, old.suffixCor2, old.suffixFormat
            ));
        } else {
            setNickname(uuid, new EntradaApelido("", 0xFFFFFF, -1, 0, nick, 0xFFFFFF, 0, "", 0xFFFFFF, -1, 0));
        }
    }

    public void removeNickname(UUID uuid) {
        EntradaApelido old = nicknames.remove(uuid);
        notificarHistorico(uuid);
        if (old != null && !old.nick.isEmpty()) {
            nickToUUID.remove(chave(old.nick));
        }
        setDirty();
    }

    public EntradaApelido getNickname(UUID uuid) {
        return nicknames.get(uuid);
    }

    public boolean hasNickname(UUID uuid) {
        return nicknames.containsKey(uuid);
    }

    private static String chave(String nick) {
        return net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo.semCodigos(nick).toLowerCase(Locale.ROOT);
    }

    public UUID releasePreviousOwner(String nick, UUID excludeUUID) {
        UUID existing = nickToUUID.get(chave(nick));
        if (existing != null && !existing.equals(excludeUUID)) {
            removeNickname(existing);
            return existing;
        }
        return null;
    }

    public UUID getUUIDByNick(String nick) {
        return nickToUUID.get(chave(nick));
    }

    public Map<UUID, EntradaApelido> getAllNicknames() {
        return Collections.unmodifiableMap(nicknames);
    }

    public MutableComponent getStyledFullName(UUID uuid) {
        EntradaApelido entry = nicknames.get(uuid);
        boolean emLive = server != null && PerfilJogador.vidas().emLive(server, uuid);
        if (entry == null && !emLive) return null;

        MutableComponent result = Component.empty();

        if (entry != null) {
            boolean hasPrefix = !entry.prefix.isEmpty();
            if (hasPrefix) {
                result.append(UtilitariosApelido.renderDualColor(entry.prefix, entry.prefixCor1, entry.prefixCor2, entry.prefixFormat));
                result.append(Component.literal(" "));
            }
        }

        String nomeBase = entry != null && !entry.nick.isEmpty() ? entry.nick : resolverNomeExibicao(server, uuid);
        int nickCor = emLive ? PerfilJogador.vidas().corLive() : (entry != null ? entry.nickCor : 0xFFFFFF);
        int nickFormat = entry != null ? entry.nickFormat : 0;
        result.append(UtilitariosApelido.renderSingleColor(nomeBase, nickCor, nickFormat));

        if (entry != null && !entry.suffix.isEmpty()) {
            result.append(Component.literal(" "));
            result.append(UtilitariosApelido.renderDualColor(entry.suffix, entry.suffixCor1, entry.suffixCor2, entry.suffixFormat));
        }

        return result;
    }

    public MutableComponent getStyledNick(UUID uuid) {
        EntradaApelido entry = nicknames.get(uuid);
        boolean emLive = server != null && PerfilJogador.vidas().emLive(server, uuid);
        if (entry == null && !emLive) return null;

        String nomeBase = entry != null && !entry.nick.isEmpty() ? entry.nick : resolverNomeExibicao(server, uuid);
        int nickCor = emLive ? PerfilJogador.vidas().corLive() : (entry != null ? entry.nickCor : 0xFFFFFF);
        int nickFormat = entry != null ? entry.nickFormat : 0;
        return UtilitariosApelido.renderSingleColor(nomeBase, nickCor, nickFormat);
    }

    public static String resolverNomeExibicao(MinecraftServer server, UUID uuid) {
        EntradaApelido entry = get(server).getNickname(uuid);
        if (entry != null && !entry.nick().isEmpty()) return entry.nick();

        ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null) return online.getGameProfile().getName();

        var cache = server.getProfileCache();
        if (cache != null) {
            var perfil = cache.get(uuid);
            if (perfil.isPresent()) return perfil.get().getName();
        }
        return uuid.toString();
    }

    public static UUID resolverUUIDPorNomeOuNick(MinecraftServer server, String nome) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(nome);
        if (online != null) return online.getUUID();

        UUID porNick = get(server).getUUIDByNick(nome);
        if (porNick != null) return porNick;

        var cache = server.getProfileCache();
        if (cache != null) {
            var perfil = cache.get(nome);
            if (perfil.isPresent()) return perfil.get().getId();
        }
        return null;
    }

    public void syncToPlayer(ServerPlayer player) {
        for (Map.Entry<UUID, EntradaApelido> e : nicknames.entrySet()) {
            PacketDistributor.sendToPlayer(player,
                    RedeApelido.PacoteSincApelido.fromEntry(e.getKey(), e.getValue()));
        }
    }

    public void syncToAll(MinecraftServer server, UUID targetUUID) {
        EntradaApelido entry = nicknames.get(targetUUID);
        RedeApelido.PacoteSincApelido packet;
        if (entry != null) {
            packet = RedeApelido.PacoteSincApelido.fromEntry(targetUUID, entry);
        } else {
            packet = RedeApelido.PacoteSincApelido.removed(targetUUID);
        }
        PacketDistributor.sendToAllPlayers(packet);
    }

    public record EntradaApelido(
            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
            String nick, int nickCor, int nickFormat,
            String suffix, int suffixCor1, int suffixCor2, int suffixFormat
    ) {}
}