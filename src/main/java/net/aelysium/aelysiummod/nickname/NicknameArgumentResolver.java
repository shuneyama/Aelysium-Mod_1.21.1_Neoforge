package net.aelysium.aelysiummod.nickname;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;

import java.util.*;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class NicknameArgumentResolver {

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (event.getParseResults().getContext().getSource().getServer() == null) return;

        MinecraftServer server = event.getParseResults().getContext().getSource().getServer();
        NicknameData data = NicknameData.get(server);

        if (data.getAllNicknames().isEmpty()) return;

        String input = event.getParseResults().getReader().getString();

        int firstSpace = input.indexOf(' ');
        if (firstSpace == -1) return;

        boolean modified = false;
        String[] parts = input.split("\\s+");
        StringBuilder newInput = null;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i > 0 && !part.startsWith("@") && !part.startsWith("/")) {
                UUID uuid = data.getUUIDByNick(part);
                if (uuid != null) {
                    ServerPlayer realPlayer = server.getPlayerList().getPlayer(uuid);
                    if (realPlayer != null) {
                        if (!modified) {
                            newInput = new StringBuilder();
                            for (int j = 0; j < i; j++) {
                                if (j > 0) newInput.append(" ");
                                newInput.append(parts[j]);
                            }
                            modified = true;
                        }
                        newInput.append(" ").append(realPlayer.getGameProfile().getName());
                        continue;
                    }
                }
            }

            if (modified) {
                if (i > 0) newInput.append(" ");
                newInput.append(part);
            }
        }

        if (modified && newInput != null) {
            var dispatcher = server.getCommands().getDispatcher();
            var source = event.getParseResults().getContext().getSource();
            event.setParseResults(dispatcher.parse(newInput.toString(), source));
        }
    }

    public static Collection<String> getPlayerNamesWithNicknames(MinecraftServer server) {
        Set<String> names = new LinkedHashSet<>();

        NicknameData data = NicknameData.get(server);
        for (NicknameData.NicknameEntry entry : data.getAllNicknames().values()) {
            if (!entry.nick().isEmpty()) {
                names.add(entry.nick());
            }
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            names.add(player.getGameProfile().getName());
        }

        return names;
    }
}