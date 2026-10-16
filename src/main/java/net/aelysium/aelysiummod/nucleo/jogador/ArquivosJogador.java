package net.aelysium.aelysiummod.nucleo.jogador;

import com.google.gson.JsonObject;
import net.aelysium.aelysiummod.sistemas.admin.historico.DadosHistorico;
import net.aelysium.aelysiummod.sistemas.admin.historico.PerfilHistorico;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class ArquivosJogador {

    public static final String SUBPASTA_OPS = "ops";

    public static JsonObject cabecalho(UUID uuid, String nomeReserva) {
        PerfilHistorico perfil = DadosHistorico.getPerfil(uuid);
        if (perfil != null) return perfil.cabecalho();

        JsonObject root = new JsonObject();
        root.addProperty("uuid", uuid.toString());
        root.addProperty("nome", nomeReserva);
        root.addProperty("nomeMinecraft", nomeReserva);
        root.add("historicoNomes", new com.google.gson.JsonArray());
        return root;
    }

    public static String nomeAtual(UUID uuid, String nomeReserva) {
        PerfilHistorico perfil = DadosHistorico.getPerfil(uuid);
        return perfil != null ? perfil.nomeAtual() : nomeReserva;
    }

    public static boolean ehOp(UUID uuid) {
        PerfilHistorico perfil = DadosHistorico.getPerfil(uuid);
        return perfil != null && perfil.op;
    }

    public static Path caminho(UUID uuid, Path pasta, Map<UUID, Path> arquivosEmUso, String nomeReserva) {
        Path destino = ehOp(uuid) ? pasta.resolve(SUBPASTA_OPS) : pasta;
        String base = sanitizar(nomeAtual(uuid, nomeReserva));
        Path candidato = destino.resolve(base + ".json");
        for (var e : arquivosEmUso.entrySet()) {
            if (!e.getKey().equals(uuid) && e.getValue().toString().equalsIgnoreCase(candidato.toString())) {
                return destino.resolve(base + "_" + uuid.toString().substring(0, 8) + ".json");
            }
        }
        return candidato;
    }

    public static String sanitizar(String nome) {
        String limpo = nome.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        while (limpo.endsWith(".") || limpo.endsWith(" ")) {
            limpo = limpo.substring(0, limpo.length() - 1);
        }
        return limpo.isBlank() ? "desconhecido" : limpo;
    }
}
