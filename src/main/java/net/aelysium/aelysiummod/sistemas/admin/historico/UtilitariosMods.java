package net.aelysium.aelysiummod.sistemas.admin.historico;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UtilitariosMods {

    private static final Map<String, String> CACHE_SHA1 = new ConcurrentHashMap<>();

    public static List<ModCliente> coletar(boolean hash) {
        List<ModCliente> lista = new ArrayList<>();
        for (ModInfo mod : FMLLoader.getLoadingModList().getMods()) {
            String id = mod.getModId();
            if (id == null) continue;
            Path arquivo = caminho(mod);
            String nomeArquivo = arquivo != null && arquivo.getFileName() != null ? arquivo.getFileName().toString() : "";
            String sha1 = hash && arquivo != null ? sha1(arquivo) : "";
            lista.add(new ModCliente(id, String.valueOf(mod.getVersion()), nomeArquivo, sha1));
        }
        return lista;
    }

    public static String versaoMinecraft() {
        try {
            return FMLLoader.versionInfo().mcVersion();
        } catch (Throwable e) {
            return "";
        }
    }

    public static String versaoLoader() {
        try {
            return FMLLoader.versionInfo().neoForgeVersion();
        } catch (Throwable e) {
            return "";
        }
    }

    private static Path caminho(ModInfo mod) {
        try {
            return mod.getOwningFile().getFile().getFilePath();
        } catch (Throwable e) {
            return null;
        }
    }

    private static String sha1(Path arquivo) {
        try {
            if (!Files.isRegularFile(arquivo)) return "";
            String chave = arquivo.toUri() + "|" + Files.size(arquivo) + "|" + Files.getLastModifiedTime(arquivo).toMillis();
            String existente = CACHE_SHA1.get(chave);
            if (existente != null) return existente;

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(arquivo)) {
                byte[] buffer = new byte[65536];
                int lidos;
                while ((lidos = in.read(buffer)) > 0) digest.update(buffer, 0, lidos);
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : digest.digest()) hex.append(String.format("%02x", b));
            CACHE_SHA1.put(chave, hex.toString());
            return hex.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
