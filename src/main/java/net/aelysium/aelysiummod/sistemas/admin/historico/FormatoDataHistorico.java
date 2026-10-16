package net.aelysium.aelysiummod.sistemas.admin.historico;

import com.google.gson.JsonElement;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FormatoDataHistorico {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");

    public static String formatar(long ms) {
        if (ms <= 0) return "";
        return FORMATO.format(Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()));
    }

    public static String agora() {
        return formatar(System.currentTimeMillis());
    }

    public static long interpretar(JsonElement elemento) {
        if (elemento == null || elemento.isJsonNull()) return 0;
        try {
            if (elemento.getAsJsonPrimitive().isNumber()) return elemento.getAsLong();
            String texto = elemento.getAsString().trim();
            if (texto.isEmpty()) return 0;
            return LocalDateTime.parse(texto, FORMATO).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return 0;
        }
    }
}
