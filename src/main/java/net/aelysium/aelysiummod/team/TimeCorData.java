package net.aelysium.aelysiummod.team;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TimeCorData extends SavedData {
    private static final String DATA_NAME = "custom_team_colors";
    private final Map<String, Integer> teamColors = new HashMap<>();

    public TimeCorData() {
        super();
    }

    public TimeCorData(CompoundTag tag, HolderLookup.Provider provider) {
        super();
        loadFromNBT(tag);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        CompoundTag colorsTag = new CompoundTag();
        teamColors.forEach((teamName, color) -> {
            colorsTag.putInt(teamName, color);
        });
        tag.put("TeamColors", colorsTag);
        return tag;
    }

    private void loadFromNBT(CompoundTag tag) {
        if (tag.contains("TeamColors")) {
            CompoundTag colorsTag = tag.getCompound("TeamColors");
            colorsTag.getAllKeys().forEach(key -> {
                int color = colorsTag.getInt(key);
                teamColors.put(key, color);
                TimeCorGerenciador.setTeamColor(key, new CustomTimeCor("saved", color));
            });
        }
    }

    public void setTeamColor(String teamName, int rgb) {
        teamColors.put(teamName, rgb);
        setDirty();
    }

    public void removeTeamColor(String teamName) {
        teamColors.remove(teamName);
        setDirty();
    }

    public static TimeCorData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<TimeCorData>(
                                TimeCorData::new,
                                (tag, provider) -> new TimeCorData(tag, provider)
                        ),
                        DATA_NAME
                );
    }

    public static void saveTeamColor(MinecraftServer server, String teamName, CustomTimeCor color) {
        TimeCorData data = get(server);
        data.setTeamColor(teamName, color.getRgb());
        TimeCorGerenciador.setTeamColor(teamName, color);
    }

    public static void removeTeamColorFromSave(MinecraftServer server, String teamName) {
        TimeCorData data = get(server);
        data.removeTeamColor(teamName);
        TimeCorGerenciador.removeTeamColor(teamName);
    }
}