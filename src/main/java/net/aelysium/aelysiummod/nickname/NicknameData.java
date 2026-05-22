package net.aelysium.aelysiummod.nickname;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.aelysium.aelysiummod.network.AelysiumNetwork;

import java.util.*;

public class NicknameData extends SavedData {

    private static final String DATA_NAME = "aelysium_nicknames";

    private final Map<UUID, NicknameEntry> nicknames = new HashMap<>();
    private final Map<String, UUID> nickToUUID = new HashMap<>();

    public NicknameData() { super(); }

    public static NicknameData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(NicknameData::new, NicknameData::load),
                DATA_NAME
        );
    }

    private static NicknameData load(CompoundTag tag, HolderLookup.Provider provider) {
        NicknameData data = new NicknameData();
        CompoundTag nicks = tag.getCompound("nicknames");
        for (String uuidStr : nicks.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag e = nicks.getCompound(uuidStr);
                NicknameEntry entry = new NicknameEntry(
                        e.getString("prefix"),
                        e.getInt("prefixCor1"), e.getInt("prefixCor2"), e.getInt("prefixFormat"),
                        e.getString("nick"),
                        e.getInt("nickCor"), e.getInt("nickFormat"),
                        e.getString("suffix"),
                        e.getInt("suffixCor1"), e.getInt("suffixCor2"), e.getInt("suffixFormat")
                );
                data.nicknames.put(uuid, entry);
                if (!entry.nick.isEmpty()) {
                    data.nickToUUID.put(entry.nick.toLowerCase(Locale.ROOT), uuid);
                }
            } catch (Exception ignored) {}
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag nicks = new CompoundTag();
        for (Map.Entry<UUID, NicknameEntry> entry : nicknames.entrySet()) {
            CompoundTag e = new CompoundTag();
            NicknameEntry n = entry.getValue();
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
        return tag;
    }

    public void setNickname(UUID uuid, NicknameEntry entry) {
        NicknameEntry old = nicknames.get(uuid);
        if (old != null && !old.nick.isEmpty()) {
            nickToUUID.remove(old.nick.toLowerCase(Locale.ROOT));
        }
        nicknames.put(uuid, entry);
        if (!entry.nick.isEmpty()) {
            nickToUUID.put(entry.nick.toLowerCase(Locale.ROOT), uuid);
        }
        setDirty();
    }

    public void setNickSimple(UUID uuid, String nick) {
        NicknameEntry old = nicknames.get(uuid);
        if (old != null) {
            setNickname(uuid, new NicknameEntry(
                    old.prefix, old.prefixCor1, old.prefixCor2, old.prefixFormat,
                    nick, old.nickCor, old.nickFormat,
                    old.suffix, old.suffixCor1, old.suffixCor2, old.suffixFormat
            ));
        } else {
            setNickname(uuid, new NicknameEntry("", 0xFFFFFF, -1, 0, nick, 0xFFFFFF, 0, "", 0xFFFFFF, -1, 0));
        }
    }

    public void removeNickname(UUID uuid) {
        NicknameEntry old = nicknames.remove(uuid);
        if (old != null && !old.nick.isEmpty()) {
            nickToUUID.remove(old.nick.toLowerCase(Locale.ROOT));
        }
        setDirty();
    }

    public NicknameEntry getNickname(UUID uuid) {
        return nicknames.get(uuid);
    }

    public boolean hasNickname(UUID uuid) {
        return nicknames.containsKey(uuid);
    }

    public boolean isNickTaken(String nick, UUID excludeUUID) {
        UUID existing = nickToUUID.get(nick.toLowerCase(Locale.ROOT));
        return existing != null && !existing.equals(excludeUUID);
    }

    public UUID getUUIDByNick(String nick) {
        return nickToUUID.get(nick.toLowerCase(Locale.ROOT));
    }

    public Map<UUID, NicknameEntry> getAllNicknames() {
        return Collections.unmodifiableMap(nicknames);
    }

    public MutableComponent getStyledFullName(UUID uuid) {
        NicknameEntry entry = nicknames.get(uuid);
        if (entry == null) return null;

        MutableComponent result = Component.empty();
        boolean hasPrefix = !entry.prefix.isEmpty();
        boolean hasSuffix = !entry.suffix.isEmpty();

        if (hasPrefix) {
            result.append(NicknameUtil.renderDualColor(entry.prefix, entry.prefixCor1, entry.prefixCor2, entry.prefixFormat));
            result.append(Component.literal(" "));
        }

        result.append(NicknameUtil.renderSingleColor(
                entry.nick.isEmpty() ? "???" : entry.nick,
                entry.nickCor, entry.nickFormat
        ));

        if (hasSuffix) {
            result.append(Component.literal(" "));
            result.append(NicknameUtil.renderDualColor(entry.suffix, entry.suffixCor1, entry.suffixCor2, entry.suffixFormat));
        }

        return result;
    }

    public MutableComponent getStyledNick(UUID uuid) {
        NicknameEntry entry = nicknames.get(uuid);
        if (entry == null) return null;
        return NicknameUtil.renderSingleColor(
                entry.nick.isEmpty() ? "???" : entry.nick,
                entry.nickCor, entry.nickFormat
        );
    }

    public void syncToPlayer(ServerPlayer player) {
        for (Map.Entry<UUID, NicknameEntry> e : nicknames.entrySet()) {
            PacketDistributor.sendToPlayer(player,
                    AelysiumNetwork.NickSyncPacket.fromEntry(e.getKey(), e.getValue()));
        }
    }

    public void syncToAll(MinecraftServer server, UUID targetUUID) {
        NicknameEntry entry = nicknames.get(targetUUID);
        AelysiumNetwork.NickSyncPacket packet;
        if (entry != null) {
            packet = AelysiumNetwork.NickSyncPacket.fromEntry(targetUUID, entry);
        } else {
            packet = AelysiumNetwork.NickSyncPacket.removed(targetUUID);
        }
        PacketDistributor.sendToAllPlayers(packet);
    }

    public record NicknameEntry(
            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
            String nick, int nickCor, int nickFormat,
            String suffix, int suffixCor1, int suffixCor2, int suffixFormat
    ) {}
}