package net.aelysium.aelysiummod.sistemas.admin.discord;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfiguracaoDiscord {

    public static class SubstituicaoJogador {
        public String nome;
        public String avatarUrl;
        public String formato;
    }

    public static class Dados {
        public boolean ativo = false;

        public String webhookChat = "";
        public String webhookComandos = "";
        public String webhookSocialSpy = "";
        public String webhookAnotacoes = "";
        public String webhookMortes = "";

        public String formatoPadrao = "{message}";
        public String nomePadrao = "{player}";
        public String avatarPadrao = "https://mc-heads.net/avatar/{player}/128";

        public Map<String, SubstituicaoJogador> overridesPorJogador = new LinkedHashMap<>();

        public Set<String> comandosPermitidos = new LinkedHashSet<>();

        public String formatoResultadoSucesso = "Correto: **[{origem}]** `{comando_completo}`{output_bloco}";
        public String formatoResultadoFalha = "Errado: **[{origem}]** `{comando_completo}`{output_bloco}";
        public String nomeComandos = "Comandos";
        public String avatarComandos = "";

        public boolean socialSpyAtivo = false;
        public List<String> comandosSocialSpy = Arrays.asList("tell", "msg", "w", "teammsg", "tm");
        public String formatoSocialSpy = "**{remetente}** → **{destinatario}**: {mensagem}";
        public String nomeSocialSpy = "Social Spy";
        public String avatarSocialSpy = "";

        public boolean relatarCommandBlocks = false;
        public boolean incluirResultados = true;

        public boolean relatarMortes = true;
        public String formatoMorte = "☠️ {mensagem}";
        public String nomeMortes = "Mortes";
        public String avatarMortes = "";

        public String webhookTickets = "";
        public String cargoTicketId = "";
        public String formatoTicket = "🎫 **Novo Ticket** — **{player}**\n{mensagem}";
        public String nomeTickets = "Tickets";
        public String avatarTickets = "";
    }

    private static Dados data = new Dados();

    public static Path getConfigPath() {
        return net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium.arquivoMigrado("discord.toml", "discord.toml");
    }

    public static synchronized void load() {
        Path p = getConfigPath();
        if (!Files.exists(p)) {
            data = criarPadrao();
            save();
            return;
        }
        try {
            CommentedFileConfig cfg = CommentedFileConfig.builder(p).charset(StandardCharsets.UTF_8).build();
            cfg.load();

            data = new Dados();
            data.ativo = cfg.getOrElse("ativo", false);
            data.webhookChat = cfg.getOrElse("webhook_chat", "");
            data.webhookComandos = cfg.getOrElse("webhook_comandos", "");
            data.webhookSocialSpy = cfg.getOrElse("webhook_social_spy", "");
            data.webhookAnotacoes = cfg.getOrElse("webhook_anotacoes", "");
            data.webhookMortes = cfg.getOrElse("webhook_mortes", "");
            data.formatoPadrao = cfg.getOrElse("formato_padrao", "{message}");
            data.nomePadrao = cfg.getOrElse("nome_padrao", "{player}");
            data.avatarPadrao = cfg.getOrElse("avatar_padrao", "https://mc-heads.net/avatar/{player}/128");
            data.formatoResultadoSucesso = cfg.getOrElse("formato_sucesso", data.formatoResultadoSucesso);
            data.formatoResultadoFalha = cfg.getOrElse("formato_falha", data.formatoResultadoFalha);
            data.nomeComandos = cfg.getOrElse("nome_comandos", "Comandos");
            data.avatarComandos = cfg.getOrElse("avatar_comandos", "");
            data.relatarCommandBlocks = cfg.getOrElse("relatar_command_blocks", false);
            data.incluirResultados = cfg.getOrElse("incluir_resultados", true);
            data.socialSpyAtivo = cfg.getOrElse("social_spy_ativo", false);
            data.comandosSocialSpy = cfg.getOrElse("comandos_social_spy", Arrays.asList("tell", "msg", "w", "teammsg", "tm"));
            data.formatoSocialSpy = cfg.getOrElse("formato_social_spy", data.formatoSocialSpy);
            data.nomeSocialSpy = cfg.getOrElse("nome_social_spy", "Social Spy");
            data.avatarSocialSpy = cfg.getOrElse("avatar_social_spy", "");
            data.relatarMortes = cfg.getOrElse("relatar_mortes", true);
            data.formatoMorte = cfg.getOrElse("formato_morte", data.formatoMorte);
            data.nomeMortes = cfg.getOrElse("nome_mortes", "Mortes");
            data.avatarMortes = cfg.getOrElse("avatar_mortes", "");

            data.webhookTickets = cfg.getOrElse("webhook_tickets", "");
            data.cargoTicketId = cfg.getOrElse("cargo_ticket_id", "");
            data.formatoTicket = cfg.getOrElse("formato_ticket", data.formatoTicket);
            data.nomeTickets = cfg.getOrElse("nome_tickets", "Tickets");
            data.avatarTickets = cfg.getOrElse("avatar_tickets", "");

            List<String> permitidos = cfg.getOrElse("comandos_permitidos", Collections.emptyList());
            data.comandosPermitidos = new LinkedHashSet<>(permitidos);

            data.overridesPorJogador = new LinkedHashMap<>();
            Object overridesObj = cfg.get("overrides");
            if (overridesObj instanceof Config overridesCfg) {
                for (Config.Entry entry : overridesCfg.entrySet()) {
                    Object val = entry.getValue();
                    if (val instanceof Config playerCfg) {
                        SubstituicaoJogador override = new SubstituicaoJogador();
                        override.nome = playerCfg.getOrElse("nome", "");
                        override.avatarUrl = playerCfg.getOrElse("avatar_url", "");
                        override.formato = playerCfg.getOrElse("formato", "");
                        data.overridesPorJogador.put(entry.getKey(), override);
                    }
                }
            }

            cfg.close();
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar discord.toml: ", e);
        }
    }

    public static synchronized void save() {
        Path p = getConfigPath();
        try {
            Files.createDirectories(p.getParent());
            StringBuilder sb = new StringBuilder();

            sb.append("# Configuração do relay Discord do Aelysium Mod.\n");
            sb.append("# Edite e execute /discord recarregar para aplicar.\n");
            sb.append("ativo = ").append(data.ativo).append("\n\n");

            sb.append("# ==================== WEBHOOKS ====================\n");
            sb.append("# webhook_chat      → mensagens de chat do servidor\n");
            sb.append("# webhook_comandos  → comandos executados (invocação + resultado)\n");
            sb.append("# webhook_social_spy → mensagens privadas (tell/msg/w)\n");
            sb.append("# Deixe vazio para desativar o canal.\n");
            sb.append("webhook_chat = ").append(tomlStr(data.webhookChat)).append("\n");
            sb.append("webhook_comandos = ").append(tomlStr(data.webhookComandos)).append("\n");
            sb.append("webhook_social_spy = ").append(tomlStr(data.webhookSocialSpy)).append("\n");
            sb.append("# webhook_anotacoes → anotações adicionadas a jogadores\n");
            sb.append("webhook_anotacoes = ").append(tomlStr(data.webhookAnotacoes)).append("\n");
            sb.append("# webhook_mortes → mensagens de morte dos jogadores\n");
            sb.append("webhook_mortes = ").append(tomlStr(data.webhookMortes)).append("\n");
            sb.append("# webhook_tickets → mensagens do comando /aelysium ticket\n");
            sb.append("webhook_tickets = ").append(tomlStr(data.webhookTickets)).append("\n\n");

            sb.append("# ==================== FORMATO DO CHAT ====================\n");
            sb.append("# Placeholders: {player} = nome do jogador, {message} = mensagem enviada\n");
            sb.append("formato_padrao = ").append(tomlStr(data.formatoPadrao)).append("\n");
            sb.append("nome_padrao = ").append(tomlStr(data.nomePadrao)).append("\n");
            sb.append("avatar_padrao = ").append(tomlStr(data.avatarPadrao)).append("\n\n");

            sb.append("# ==================== WHITELIST DE COMANDOS ====================\n");
            sb.append("# Comandos relatados no webhook_comandos. Suporta prefixo de subcomandos:\n");
            sb.append("# \"aelysium banlist\" relata só esse subcomando; \"aelysium\" relata tudo.\n");
            sb.append("comandos_permitidos = ");
            appendTomlList(sb, new ArrayList<>(data.comandosPermitidos));
            sb.append("\n");

            sb.append("# ==================== FORMATO DE COMANDOS ====================\n");
            sb.append("# Placeholders: {origem}, {comando}, {comando_completo}, {status},\n");
            sb.append("#               {resultado}, {output}, {output_bloco}\n");
            sb.append("formato_sucesso = ").append(tomlStr(data.formatoResultadoSucesso)).append("\n");
            sb.append("formato_falha = ").append(tomlStr(data.formatoResultadoFalha)).append("\n");
            sb.append("nome_comandos = ").append(tomlStr(data.nomeComandos)).append("\n");
            sb.append("avatar_comandos = ").append(tomlStr(data.avatarComandos)).append("\n");
            sb.append("relatar_command_blocks = ").append(data.relatarCommandBlocks).append("\n");
            sb.append("incluir_resultados = ").append(data.incluirResultados).append("\n\n");

            sb.append("# ==================== SOCIAL SPY ====================\n");
            sb.append("# Captura mensagens privadas (tell/msg/w) independente da whitelist acima.\n");
            sb.append("social_spy_ativo = ").append(data.socialSpyAtivo).append("\n");
            sb.append("comandos_social_spy = ");
            appendTomlList(sb, data.comandosSocialSpy);
            sb.append("formato_social_spy = ").append(tomlStr(data.formatoSocialSpy)).append("\n");
            sb.append("nome_social_spy = ").append(tomlStr(data.nomeSocialSpy)).append("\n");
            sb.append("avatar_social_spy = ").append(tomlStr(data.avatarSocialSpy)).append("\n\n");

            sb.append("# ==================== MORTES ====================\n");
            sb.append("# Captura as mensagens de morte dos jogadores (ex: \"Fulano morreu de fome\").\n");
            sb.append("# Placeholders: {mensagem}\n");
            sb.append("relatar_mortes = ").append(data.relatarMortes).append("\n");
            sb.append("formato_morte = ").append(tomlStr(data.formatoMorte)).append("\n");
            sb.append("nome_mortes = ").append(tomlStr(data.nomeMortes)).append("\n");
            sb.append("avatar_mortes = ").append(tomlStr(data.avatarMortes)).append("\n\n");

            sb.append("# ==================== TICKETS ====================\n");
            sb.append("# /aelysium ticket <mensagem> — qualquer jogador pode usar.\n");
            sb.append("# cargo_ticket_id → ID do cargo do Discord a ser mencionado (clique direito no\n");
            sb.append("# cargo com o modo desenvolvedor ativado > Copiar ID). Deixe vazio pra não marcar ninguém.\n");
            sb.append("# Placeholders do formato: {player}, {mensagem}\n");
            sb.append("cargo_ticket_id = ").append(tomlStr(data.cargoTicketId)).append("\n");
            sb.append("formato_ticket = ").append(tomlStr(data.formatoTicket)).append("\n");
            sb.append("nome_tickets = ").append(tomlStr(data.nomeTickets)).append("\n");
            sb.append("avatar_tickets = ").append(tomlStr(data.avatarTickets)).append("\n");

            if (!data.overridesPorJogador.isEmpty()) {
                sb.append("\n# ==================== OVERRIDES POR JOGADOR ====================\n");
                sb.append("# Personalize como as mensagens de um jogador aparecem no Discord.\n");
                sb.append("# A chave pode ser o nome do jogador OU o UUID (com ou sem hifens).\n");
                for (Map.Entry<String, SubstituicaoJogador> entry : data.overridesPorJogador.entrySet()) {
                    SubstituicaoJogador o = entry.getValue();
                    sb.append("\n[overrides.").append(tomlKey(entry.getKey())).append("]\n");
                    sb.append("nome = ").append(tomlStr(o.nome == null ? "" : o.nome)).append("\n");
                    sb.append("avatar_url = ").append(tomlStr(o.avatarUrl == null ? "" : o.avatarUrl)).append("\n");
                    sb.append("formato = ").append(tomlStr(o.formato == null ? "" : o.formato)).append("\n");
                }
            }

            Files.writeString(p, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao salvar discord.toml: ", e);
        }
    }

    private static Dados criarPadrao() {
        Dados d = new Dados();

        SubstituicaoJogador exemplo = new SubstituicaoJogador();
        exemplo.nome = "[Admin] {player}";
        exemplo.avatarUrl = "https://mc-heads.net/avatar/{player}/128";
        exemplo.formato = "**{message}**";
        d.overridesPorJogador.put("ExemploNickname", exemplo);

        d.comandosPermitidos.addAll(Arrays.asList(
                "gamemode", "give", "tp", "ban", "kick", "op", "deop",
                "say", "tellraw", "title", "aelysium banlist"
        ));
        return d;
    }

    private static String tomlStr(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    private static String tomlKey(String key) {
        if (key.matches("[a-zA-Z0-9_-]+")) return key;
        return "\"" + key.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static void appendTomlList(StringBuilder sb, List<String> items) {
        if (items.isEmpty()) {
            sb.append("[]\n");
            return;
        }
        sb.append("[\n");
        for (String s : items) {
            sb.append("    ").append(tomlStr(s)).append(",\n");
        }
        sb.append("]\n");
    }

    public static Dados get() {
        return data;
    }

    public static SubstituicaoJogador getOverridePorNome(String nome) {
        if (nome == null) return null;
        SubstituicaoJogador o = data.overridesPorJogador.get(nome);
        if (o != null) return o;
        for (Map.Entry<String, SubstituicaoJogador> e : data.overridesPorJogador.entrySet()) {
            if (e.getKey().equalsIgnoreCase(nome)) return e.getValue();
        }
        return null;
    }

    public static SubstituicaoJogador getOverridePorUuid(UUID uuid) {
        if (uuid == null) return null;
        String comHifens = uuid.toString();
        String semHifens = comHifens.replace("-", "");
        SubstituicaoJogador o = data.overridesPorJogador.get(comHifens);
        if (o != null) return o;
        return data.overridesPorJogador.get(semHifens);
    }

    public static boolean comandoPermitido(String comandoCompleto) {
        if (comandoCompleto == null) return false;
        String c = comandoCompleto.startsWith("/") ? comandoCompleto.substring(1) : comandoCompleto;
        c = c.toLowerCase().trim();
        for (String entrada : data.comandosPermitidos) {
            if (entrada == null) continue;
            String e = entrada.toLowerCase().trim();
            if (!e.isEmpty() && (c.equals(e) || c.startsWith(e + " "))) return true;
        }
        return false;
    }

    public static boolean isComandoSocialSpy(String raizComando) {
        if (raizComando == null || data.comandosSocialSpy == null) return false;
        String r = raizComando.toLowerCase();
        for (String c : data.comandosSocialSpy) {
            if (c != null && c.equalsIgnoreCase(r)) return true;
        }
        return false;
    }
}
