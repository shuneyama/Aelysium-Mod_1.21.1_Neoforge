package net.aelysium.aelysiummod.sistemas.admin.discord;

import com.google.gson.JsonObject;
import net.aelysium.aelysiummod.AelysiumMod;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WebhookDiscord {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AelysiumDiscordWebhook");
        t.setDaemon(true);
        return t;
    });

    public static void enviar(String webhookUrl, String username, String avatarUrl, String content) {
        enviarInterno(webhookUrl, username, avatarUrl, content, null);
    }

    public static void enviarComMencaoCargo(String webhookUrl, String username, String avatarUrl, String content, String cargoId) {
        enviarInterno(webhookUrl, username, avatarUrl, content, cargoId);
    }

    private static void enviarInterno(String webhookUrl, String username, String avatarUrl, String content, String cargoIdMencionado) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;
        if (content == null) content = "";
        if (content.length() > 1900) content = content.substring(0, 1900) + "...";

        JsonObject body = new JsonObject();
        body.addProperty("content", content);
        if (username != null && !username.isBlank()) body.addProperty("username", username);
        if (avatarUrl != null && !avatarUrl.isBlank()) body.addProperty("avatar_url", avatarUrl);

        JsonObject allowed = new JsonObject();
        if (cargoIdMencionado != null && !cargoIdMencionado.isBlank()) {
            com.google.gson.JsonArray roles = new com.google.gson.JsonArray();
            roles.add(cargoIdMencionado);
            allowed.add("roles", roles);
            allowed.add("parse", new com.google.gson.JsonArray());
        } else {
            allowed.add("parse", new com.google.gson.JsonArray());
        }
        body.add("allowed_mentions", allowed);

        final String jsonBody = body.toString();
        final String url = webhookUrl;

        EXECUTOR.submit(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "AelysiumMod/1.0")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
                HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
                int sc = resp.statusCode();
                if (sc >= 400) {
                    org.slf4j.LoggerFactory.getLogger(AelysiumMod.MOD_ID)
                            .warn("Webhook Discord retornou {}: {}", sc, resp.body());
                }
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(AelysiumMod.MOD_ID)
                        .error("Falha ao enviar webhook Discord: ", e);
            }
        });
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}