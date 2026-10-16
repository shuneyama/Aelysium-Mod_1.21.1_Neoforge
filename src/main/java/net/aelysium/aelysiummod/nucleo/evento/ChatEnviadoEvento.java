package net.aelysium.aelysiummod.nucleo.evento;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class ChatEnviadoEvento extends Event {

    private final ServerPlayer remetente;
    private final String mensagem;
    private final String tag;

    public ChatEnviadoEvento(ServerPlayer remetente, String mensagem, String tag) {
        this.remetente = remetente;
        this.mensagem = mensagem;
        this.tag = tag;
    }

    public ServerPlayer getRemetente() { return remetente; }

    public String getMensagem() { return mensagem; }

    public String getTag() { return tag; }
}
