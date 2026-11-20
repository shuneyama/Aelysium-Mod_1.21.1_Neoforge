package net.aelysium.aelysiummod.sistemas.social.apelido;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;

import java.util.*;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ResolvedorArgumentoApelido {

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        CommandContextBuilder<CommandSourceStack> contexto = event.getParseResults().getContext();
        if (contexto.getSource().getServer() == null) return;

        MinecraftServer server = contexto.getSource().getServer();
        DadosApelido data = DadosApelido.get(server);
        if (data.getAllNicknames().isEmpty()) return;

        String input = event.getParseResults().getReader().getString();
        List<ParsedCommandNode<CommandSourceStack>> nos = todosOsNos(contexto);

        StringBuilder novoInput = new StringBuilder(input);
        boolean modificado = false;

        for (int i = nos.size() - 1; i >= 0; i--) {
            ParsedCommandNode<CommandSourceStack> no = nos.get(i);
            if (!(no.getNode() instanceof ArgumentCommandNode<?, ?> argNode)) continue;
            if (!(argNode.getType() instanceof EntityArgument)) continue;

            StringRange range = no.getRange();
            if (range.getStart() < 0 || range.getEnd() > input.length()) continue;

            String valorBruto = range.get(input);
            if (valorBruto.isEmpty() || valorBruto.startsWith("@")) continue;

            UUID uuid = data.getUUIDByNick(valorBruto);
            if (uuid == null) continue;

            ServerPlayer realPlayer = server.getPlayerList().getPlayer(uuid);
            if (realPlayer == null) continue;

            novoInput.replace(range.getStart(), range.getEnd(), realPlayer.getGameProfile().getName());
            modificado = true;
        }

        if (modificado) {
            var dispatcher = server.getCommands().getDispatcher();
            event.setParseResults(dispatcher.parse(novoInput.toString(), contexto.getSource()));
        }
    }

    private static List<ParsedCommandNode<CommandSourceStack>> todosOsNos(CommandContextBuilder<CommandSourceStack> contexto) {
        List<ParsedCommandNode<CommandSourceStack>> nos = new ArrayList<>(contexto.getNodes());
        CommandContextBuilder<CommandSourceStack> filho = contexto.getChild();
        if (filho != null) nos.addAll(todosOsNos(filho));
        return nos;
    }

    public static Collection<String> getPlayerNamesWithNicknames(MinecraftServer server) {
        Set<String> names = new LinkedHashSet<>();

        DadosApelido data = DadosApelido.get(server);
        for (DadosApelido.EntradaApelido entry : data.getAllNicknames().values()) {
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
