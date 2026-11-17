package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import com.mojang.brigadier.CommandDispatcher;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ComandoVersao {

    @SubscribeEvent
    public static void registrar(RegisterCommandsEvent evento) {
        CommandDispatcher<CommandSourceStack> despachante = evento.getDispatcher();
        despachante.register(Commands.literal("version")
                .requires(fonte -> fonte.getServer().isSingleplayer() || fonte.hasPermission(2))
                .executes(contexto -> {
                    WorldVersion versao = SharedConstants.getCurrentVersion();
                    CommandSourceStack fonte = contexto.getSource();
                    fonte.sendSuccess(() -> Component.translatable("commands.version.header"), false);
                    fonte.sendSuccess(() -> Component.translatable("commands.version.id", versao.getId()), false);
                    fonte.sendSuccess(() -> Component.translatable("commands.version.name", versao.getName()), false);
                    fonte.sendSuccess(() -> Component.translatable("commands.version.data", versao.getDataVersion().getVersion()), false);
                    fonte.sendSuccess(() -> Component.translatable("commands.version.series", versao.getDataVersion().getSeries()), false);
                    fonte.sendSuccess(() -> Component.translatable("commands.version.protocol", versao.getProtocolVersion(), "0x" + Integer.toHexString(versao.getProtocolVersion())), false);
                    fonte.sendSuccess(() -> Component.translatable("commands.version.pack.resource", versao.getPackVersion(PackType.CLIENT_RESOURCES)), false);
                    fonte.sendSuccess(() -> Component.translatable("commands.version.pack.data", versao.getPackVersion(PackType.SERVER_DATA)), false);
                    fonte.sendSuccess(() -> Component.translatable(versao.isStable() ? "commands.version.stable.yes" : "commands.version.stable.no"), false);
                    return 1;
                }));
    }
}
