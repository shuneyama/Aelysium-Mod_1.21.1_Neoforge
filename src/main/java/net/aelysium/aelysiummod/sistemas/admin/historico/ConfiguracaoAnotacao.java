package net.aelysium.aelysiummod.sistemas.admin.historico;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ConfiguracaoAnotacao {

    private static List<String> tipos = Arrays.asList("alerta", "observacao", "elogio");
    private static String tipoBan = "alerta";
    private static int alertasParaBanTemp = 3;
    private static int duracaoBanTempHoras = 24;
    private static int alertasParaBanIp = 5;
    private static boolean autoBan = true;

    public static void load() {
        Path p = net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium.arquivoMigrado("anotacoes.toml", "anotacoes.toml");
        if (!Files.exists(p)) {
            save();
            return;
        }
        try {
            CommentedFileConfig cfg = CommentedFileConfig.builder(p).charset(StandardCharsets.UTF_8).build();
            cfg.load();
            tipos = cfg.getOrElse("tipos", tipos);
            tipoBan = cfg.getOrElse("tipo_ban", tipoBan);
            alertasParaBanTemp = cfg.getOrElse("alertas_para_ban_temp", alertasParaBanTemp);
            duracaoBanTempHoras = cfg.getOrElse("duracao_ban_temp_horas", duracaoBanTempHoras);
            alertasParaBanIp = cfg.getOrElse("alertas_para_ban_ip", alertasParaBanIp);
            autoBan = cfg.getOrElse("auto_ban", autoBan);
            cfg.close();
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar anotacoes.toml: ", e);
        }
    }

    public static void save() {
        Path p = net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium.arquivoMigrado("anotacoes.toml", "anotacoes.toml");
        try {
            Files.createDirectories(p.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("# Tipos de anotação disponíveis. Adicione ou remova livremente.\n");
            sb.append("tipos = ");
            appendTomlList(sb, tipos);
            sb.append("\n");
            sb.append("# Tipo que conta para os bans automáticos abaixo.\n");
            sb.append("tipo_ban = \"").append(tipoBan).append("\"\n\n");
            sb.append("# ==================== BAN AUTOMÁTICO ====================\n");
            sb.append("# Defina 0 para desabilitar cada limite independentemente.\n");
            sb.append("alertas_para_ban_temp = ").append(alertasParaBanTemp).append("\n");
            sb.append("duracao_ban_temp_horas = ").append(duracaoBanTempHoras).append("\n");
            sb.append("alertas_para_ban_ip = ").append(alertasParaBanIp).append("\n");
            sb.append("auto_ban = ").append(autoBan).append("\n");
            Files.writeString(p, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao salvar anotacoes.toml: ", e);
        }
    }

    private static void appendTomlList(StringBuilder sb, List<String> items) {
        if (items.isEmpty()) { sb.append("[]\n"); return; }
        sb.append("[\n");
        for (String s : items) sb.append("    \"").append(s.replace("\"", "\\\"")).append("\",\n");
        sb.append("]\n");
    }

    public static List<String> getTipos() { return tipos; }
    public static String getTipoBan() { return tipoBan; }
    public static int getAlertasParaBanTemp() { return alertasParaBanTemp; }
    public static int getDuracaoBanTempHoras() { return duracaoBanTempHoras; }
    public static int getAlertasParaBanIp() { return alertasParaBanIp; }
    public static boolean isAutoBan() { return autoBan; }
}
