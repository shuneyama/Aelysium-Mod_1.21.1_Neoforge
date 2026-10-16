package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteChunkArquivo;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteFinalizarDownload;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteIniciarDownload;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacotePararVideo;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteTocarVideo;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ManipuladoresCinematicaCliente {

    public static void handleChunkArquivo(PacoteChunkArquivo packet, IPayloadContext context) {
        context.enqueueWork(() -> GerenciadorVideoCliente.receberChunk(
                packet.nome(), packet.tipo(), packet.indice(), packet.dados()
        ));
    }

    public static void handleFinalizarDownload(PacoteFinalizarDownload packet, IPayloadContext context) {
        context.enqueueWork(() -> GerenciadorVideoCliente.finalizarRecepcao(packet.nome(), packet.tipo()));
    }

    public static void handleIniciarDownload(PacoteIniciarDownload packet, IPayloadContext context) {
        context.enqueueWork(() -> GerenciadorVideoCliente.iniciarRecepcao(
                packet.nome(), packet.tipo(), packet.totalBytes(), packet.totalChunks(), packet.sha256()
        ));
    }

    public static void handlePararVideo(PacotePararVideo packet, IPayloadContext context) {
        context.enqueueWork(ControladorCinematica::parar);
    }

    public static void handleTocarVideo(PacoteTocarVideo packet, IPayloadContext context) {
        context.enqueueWork(() -> ControladorCinematica.iniciarPreload(
                packet.nome(), packet.fadeInTicks(), packet.fadeOutTicks(), packet.startDelayMillis()
        ));
    }
}
