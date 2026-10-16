package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteListaMods;
import net.aelysium.aelysiummod.sistemas.admin.historico.ConfiguracaoRegrasMods;
import net.aelysium.aelysiummod.sistemas.admin.historico.DadosHistorico;
import net.aelysium.aelysiummod.sistemas.admin.historico.ModCliente;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManipuladorListaNegraModsServidor {

    private static final int MAX_PARTES = 50;

    private static class Recebimento {
        String minecraft = "";
        String loader = "";
        String loaderVersao = "";
        int totalPartes;
        int proximaParte;
        final List<ModCliente> mods = new ArrayList<>();
    }

    private static final Map<UUID, Recebimento> RECEBENDO = new ConcurrentHashMap<>();

    public static void limpar(UUID jogador) {
        RECEBENDO.remove(jogador);
    }

    public static void handle(PacoteListaMods packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        Recebimento recebimento = RECEBENDO.get(player.getUUID());
        if (packet.parte() == 0) {
            if (packet.totalPartes() < 1 || packet.totalPartes() > MAX_PARTES) return;
            recebimento = new Recebimento();
            recebimento.totalPartes = packet.totalPartes();
            recebimento.minecraft = cortar(packet.minecraft());
            recebimento.loader = cortar(packet.loader());
            recebimento.loaderVersao = cortar(packet.loaderVersao());
            RECEBENDO.put(player.getUUID(), recebimento);
        }
        if (recebimento == null || packet.parte() != recebimento.proximaParte) {
            RECEBENDO.remove(player.getUUID());
            return;
        }

        for (ModCliente mod : packet.mods()) recebimento.mods.add(mod.limpo());
        recebimento.proximaParte++;
        if (recebimento.proximaParte < recebimento.totalPartes) return;

        RECEBENDO.remove(player.getUUID());
        concluir(player, recebimento);
    }

    private static void concluir(ServerPlayer player, Recebimento recebimento) {
        DadosHistorico.registrarMods(player, recebimento.minecraft, recebimento.loader,
                recebimento.loaderVersao, recebimento.mods);

        if (player.hasPermissions(2)) return;

        List<String> ids = recebimento.mods.stream().map(ModCliente::id).toList();
        String bloqueado = ConfiguracaoRegrasMods.checarBloqueio(ids);
        if (bloqueado == null) return;

        String mensagem = ConfiguracaoRegrasMods.mensagemDesconexao(bloqueado);

        if (ConfiguracaoRegrasMods.logarBloqueios()) {
            org.slf4j.LoggerFactory.getLogger(AelysiumMod.MOD_ID)
                    .info("[ModBlacklist] Jogador '{}' desconectado por uso do mod '{}'.",
                            player.getGameProfile().getName(), bloqueado);
        }

        player.connection.disconnect(Component.literal(mensagem));
    }

    private static String cortar(String texto) {
        if (texto == null) return "";
        return texto.length() > 64 ? texto.substring(0, 64) : texto;
    }
}
