package net.aelysium.aelysiummod.sistemas.admin.discord;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class RetransmissorComandosDiscord {

    private static final ThreadLocal<Boolean> REENTRANT = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        if (REENTRANT.get()) return;

        ConfiguracaoDiscord.Dados cfg = ConfiguracaoDiscord.get();
        if (!cfg.ativo) return;
        if (cfg.webhookComandos == null || cfg.webhookComandos.isBlank()) return;

        CommandContextBuilder<CommandSourceStack> ctx = event.getParseResults().getContext();
        if (ctx == null) return;

        String comandoCompleto = event.getParseResults().getReader().getString();
        if (!ConfiguracaoDiscord.comandoPermitido(comandoCompleto)) return;

        CommandSourceStack source = ctx.getSource();
        boolean isCommandBlock = source.getEntity() == null && source.getPlayer() == null;
        if (isCommandBlock && !cfg.relatarCommandBlocks) return;

        String origem = isCommandBlock ? "CommandBlock" : source.getTextName();
        String comandoSemBarra = comandoCompleto.startsWith("/") ? comandoCompleto.substring(1) : comandoCompleto;
        String comandoComBarra = comandoCompleto.startsWith("/") ? comandoCompleto : "/" + comandoCompleto;

        if (!cfg.incluirResultados) {
            String mensagem = aplicar(cfg.formatoResultadoSucesso, origem, comandoSemBarra, comandoComBarra,
                    "Executado", "1", "", "");
            WebhookDiscord.enviar(cfg.webhookComandos, cfg.nomeComandos, cfg.avatarComandos, mensagem);
            return;
        }

        event.setCanceled(true);

        final CommandSource originalSource = source.source;
        final List<String> capturadas = new ArrayList<>();

        CommandSource interceptor = new CommandSource() {
            @Override
            public void sendSystemMessage(Component component) {
                capturadas.add(component.getString());
                if (originalSource != null) originalSource.sendSystemMessage(component);
            }
            @Override
            public boolean acceptsSuccess() {
                return originalSource == null || originalSource.acceptsSuccess();
            }
            @Override
            public boolean acceptsFailure() {
                return originalSource == null || originalSource.acceptsFailure();
            }
            @Override
            public boolean shouldInformAdmins() {
                return originalSource != null && originalSource.shouldInformAdmins();
            }
        };

        CommandSourceStack wrapped = source.withSource(interceptor).withCallback((success, result) -> {
            String formato = success ? cfg.formatoResultadoSucesso : cfg.formatoResultadoFalha;
            String status = success ? "Sucesso" : "Falha";

            String output = String.join("\n", capturadas);
            if (output.isEmpty()) {
                output = extrairConteudoVisivel(comandoSemBarra);
            }
            String outputBloco = output.isEmpty() ? "" : "\n```\n" + output + "\n```";

            String mensagemResultado = aplicar(formato, origem, comandoSemBarra, comandoComBarra,
                    status, String.valueOf(result), output, outputBloco);
            WebhookDiscord.enviar(cfg.webhookComandos, cfg.nomeComandos, cfg.avatarComandos, mensagemResultado);
        });

        REENTRANT.set(true);
        try {
            CommandDispatcher<CommandSourceStack> dispatcher = source.getServer().getCommands().getDispatcher();
            StringReader reader = new StringReader(comandoSemBarra);
            ParseResults<CommandSourceStack> novoParse = dispatcher.parse(reader, wrapped);
            dispatcher.execute(novoParse);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AelysiumMod.MOD_ID)
                    .error("Erro ao re-executar comando '{}': ", comandoCompleto, e);
            String output = String.join("\n", capturadas);
            if (output.isEmpty()) output = extrairConteudoVisivel(comandoSemBarra);
            String outputBloco = output.isEmpty() ? "" : "\n```\n" + output + "\n```";
            String mensagemResultado = aplicar(cfg.formatoResultadoFalha, origem, comandoSemBarra, comandoComBarra,
                    "Falha", "0", output, outputBloco);
            WebhookDiscord.enviar(cfg.webhookComandos, cfg.nomeComandos, cfg.avatarComandos, mensagemResultado);
        } finally {
            REENTRANT.set(false);
        }
    }

    private static String aplicar(String formato, String origem, String comando, String comandoCompleto,
                                  String status, String resultado, String output, String outputBloco) {
        if (formato == null) return "";
        return formato
                .replace("{origem}", origem == null ? "" : origem)
                .replace("{comando_completo}", comandoCompleto == null ? "" : comandoCompleto)
                .replace("{comando}", comando == null ? "" : comando)
                .replace("{status}", status == null ? "" : status)
                .replace("{resultado}", resultado == null ? "" : resultado)
                .replace("{output_bloco}", outputBloco == null ? "" : outputBloco)
                .replace("{output}", output == null ? "" : output);
    }

    private static String extrairConteudoVisivel(String comandoSemBarra) {
        if (comandoSemBarra == null) return "";
        String[] partes = comandoSemBarra.trim().split("\\s+", 2);
        if (partes.length < 2) return "";
        String raiz = partes[0].toLowerCase();
        String args = partes[1];

        switch (raiz) {
            case "say":
            case "me":
                return args;
            case "tellraw": {
                int sp = args.indexOf(' ');
                if (sp < 0) return "";
                return parseTextoComponent(args.substring(sp + 1).trim());
            }
            case "title":
            case "actionbar": {
                String[] p = args.split("\\s+", 3);
                if (p.length < 3) return "";
                String action = p[1].toLowerCase();
                if (!action.equals("title") && !action.equals("subtitle") && !action.equals("actionbar")) return "";
                return parseTextoComponent(p[2]);
            }
            default:
                return "";
        }
    }

    private static String parseTextoComponent(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.isEmpty()) return "";
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        try {
            JsonElement e = JsonParser.parseString(s);
            return extrairTextoElement(e);
        } catch (Exception ex) {
            return s;
        }
    }

    private static String extrairTextoElement(JsonElement e) {
        if (e == null || e.isJsonNull()) return "";
        if (e.isJsonPrimitive()) return e.getAsString();
        if (e.isJsonArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonElement x : e.getAsJsonArray()) sb.append(extrairTextoElement(x));
            return sb.toString();
        }
        if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();
            StringBuilder sb = new StringBuilder();
            if (o.has("text")) sb.append(o.get("text").getAsString());
            if (o.has("translate")) sb.append(o.get("translate").getAsString());
            if (o.has("extra")) sb.append(extrairTextoElement(o.get("extra")));
            return sb.toString();
        }
        return "";
    }
}