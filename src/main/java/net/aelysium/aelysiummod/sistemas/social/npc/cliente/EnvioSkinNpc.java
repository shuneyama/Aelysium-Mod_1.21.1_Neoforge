package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public final class EnvioSkinNpc {

    private EnvioSkinNpc() {}

    public static boolean ehCpm(byte[] dados) {
        return dados != null && dados.length > 0 && (dados[0] & 0xFF) == PacotesNpc.PRIMEIRO_BYTE_CPM;
    }

    public static void enviarCpm(int npcId, byte[] dados) {
        int total = (dados.length + PacotesNpc.TAMANHO_PARTE_CPM - 1) / PacotesNpc.TAMANHO_PARTE_CPM;
        for (int i = 0; i < total; i++) {
            int de = i * PacotesNpc.TAMANHO_PARTE_CPM;
            int ate = Math.min(dados.length, de + PacotesNpc.TAMANHO_PARTE_CPM);
            PacketDistributor.sendToServer(new PacotesNpc.EnviarModeloCpmNpcC2S(npcId, i, total, Arrays.copyOfRange(dados, de, ate)));
        }
    }

    public static void enviar(int npcId, byte[] dados, String tipoModelo) {
        if (dados == null || dados.length == 0) return;
        if (ehCpm(dados)) {
            if (dados.length <= PacotesNpc.TAMANHO_MAXIMO_CPM) enviarCpm(npcId, dados);
        } else if (dados.length < 65536) {
            PacketDistributor.sendToServer(new PacotesNpc.EnviarSkinNpcC2S(npcId, dados, tipoModelo));
        }
    }
}
