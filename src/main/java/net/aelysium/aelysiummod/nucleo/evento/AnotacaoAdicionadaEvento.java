package net.aelysium.aelysiummod.nucleo.evento;

import net.neoforged.bus.api.Event;

public class AnotacaoAdicionadaEvento extends Event {

    private final String alvoNome;
    private final String tipo;
    private final String responsavelNome;
    private final String comentario;
    private final long timestamp;

    public AnotacaoAdicionadaEvento(String alvoNome, String tipo, String responsavelNome, String comentario, long timestamp) {
        this.alvoNome = alvoNome;
        this.tipo = tipo;
        this.responsavelNome = responsavelNome;
        this.comentario = comentario;
        this.timestamp = timestamp;
    }

    public String getAlvoNome() { return alvoNome; }

    public String getTipo() { return tipo; }

    public String getResponsavelNome() { return responsavelNome; }

    public String getComentario() { return comentario; }

    public long getTimestamp() { return timestamp; }
}
