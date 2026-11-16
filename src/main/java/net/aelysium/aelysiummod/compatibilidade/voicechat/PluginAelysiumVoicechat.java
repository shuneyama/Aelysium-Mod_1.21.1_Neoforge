package net.aelysium.aelysiummod.compatibilidade.voicechat;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ForgeVoicechatPlugin
public class PluginAelysiumVoicechat implements VoicechatPlugin {

    private static final long INTERVALO_AVISO_MUTADO_MS = 3000;

    private final Set<UUID> conectados = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> ultimoAvisoMutado = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return AelysiumMod.MOD_ID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(PlayerConnectedEvent.class,
                event -> conectados.add(event.getConnection().getPlayer().getUuid()));
        registration.registerEvent(PlayerDisconnectedEvent.class, event -> {
            conectados.remove(event.getPlayerUuid());
            ultimoAvisoMutado.remove(event.getPlayerUuid());
        });
        registration.registerEvent(MicrophonePacketEvent.class, this::aoMicrofone);
    }

    private void aoMicrofone(MicrophonePacketEvent event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        VoicechatConnection remetente = event.getSenderConnection();
        if (remetente == null) return;
        UUID idRemetente = remetente.getPlayer().getUuid();

        if (ConfiguracaoVoz.estaMutado(idRemetente)) {
            event.cancel();
            avisarMutado(idRemetente);
            return;
        }

        if (ConfiguracaoVoz.temVozGlobal(idRemetente)) {
            event.cancel();
            VoicechatServerApi api = event.getVoicechat();
            StaticSoundPacket pacote = event.getPacket().staticSoundPacketBuilder().build();
            for (UUID idOuvinte : conectados) {
                if (idOuvinte.equals(idRemetente)) continue;
                VoicechatConnection ouvinte = api.getConnectionOf(idOuvinte);
                if (ouvinte != null) {
                    api.sendStaticSoundPacketTo(ouvinte, pacote);
                }
            }
        }
    }

    private void avisarMutado(UUID idJogador) {
        long agora = System.currentTimeMillis();
        Long ultimo = ultimoAvisoMutado.get(idJogador);
        if (ultimo != null && agora - ultimo < INTERVALO_AVISO_MUTADO_MS) return;
        ultimoAvisoMutado.put(idJogador, agora);

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        server.execute(() -> {
            ServerPlayer jogador = server.getPlayerList().getPlayer(idJogador);
            if (jogador != null) {
                jogador.displayClientMessage(Component.translatable("mensagem.aelysiummod.voz.microfone_mutado"), true);
            }
        });
    }
}
