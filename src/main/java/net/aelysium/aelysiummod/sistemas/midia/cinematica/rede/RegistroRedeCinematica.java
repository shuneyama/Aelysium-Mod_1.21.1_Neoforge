package net.aelysium.aelysiummod.sistemas.midia.cinematica.rede;

import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteChunkArquivo;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteFinalizarDownload;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteIniciarDownload;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacotePararVideo;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteStatusDownload;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.PacoteTocarVideo;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeCinematica {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(
                PacoteIniciarDownload.TYPE,
                PacoteIniciarDownload.CODEC,
                PacoteIniciarDownload::handle);

        registrar.playToClient(
                PacoteChunkArquivo.TYPE,
                PacoteChunkArquivo.CODEC,
                PacoteChunkArquivo::handle);

        registrar.playToClient(
                PacoteFinalizarDownload.TYPE,
                PacoteFinalizarDownload.CODEC,
                PacoteFinalizarDownload::handle);

        registrar.playToClient(
                PacoteTocarVideo.TYPE,
                PacoteTocarVideo.CODEC,
                PacoteTocarVideo::handle);

        registrar.playToClient(
                PacotePararVideo.TYPE,
                PacotePararVideo.CODEC,
                PacotePararVideo::handle);

        registrar.playToServer(
                PacoteStatusDownload.TYPE,
                PacoteStatusDownload.CODEC,
                PacoteStatusDownload::handle);

        registrar.playToServer(
                PacoteStatusCutscene.TYPE,
                PacoteStatusCutscene.CODEC,
                PacoteStatusCutscene::handle);
    }
}
