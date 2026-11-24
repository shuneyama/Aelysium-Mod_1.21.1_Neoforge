package net.aelysium.aelysiummod.sistemas.rp.skin;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class RegistroSkins {
    public record Skin(byte[] dados, String modelo) {
    }

    public static final String TIPO_DEUS = "deus";
    public static final String TIPO_PERSONAGEM = "personagem";

    private static final Map<String, Skin> SKINS = new ConcurrentHashMap<>();
    private static Path pasta;

    private RegistroSkins() {
    }

    public static String chave(String tipo, String id) {
        return tipo + ":" + id;
    }

    public static boolean tipoValido(String tipo) {
        return TIPO_DEUS.equals(tipo) || TIPO_PERSONAGEM.equals(tipo);
    }

    public static boolean idValido(String id) {
        return id != null && !id.isEmpty() && id.length() <= 64 && id.matches("[a-z0-9_]+");
    }

    public static synchronized void carregar(MinecraftServer servidor) {
        SKINS.clear();
        pasta = servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("skins");
        if (!Files.isDirectory(pasta)) return;
        try (Stream<Path> arquivos = Files.list(pasta)) {
            arquivos.filter(a -> a.getFileName().toString().endsWith(".skin")).forEach(arquivo -> {
                String nome = arquivo.getFileName().toString();
                String base = nome.substring(0, nome.length() - ".skin".length());
                int ponto = base.indexOf('.');
                if (ponto <= 0) return;
                try {
                    String modelo = "wide";
                    Path arquivoModelo = pasta.resolve(base + ".modelo");
                    if (Files.exists(arquivoModelo)) modelo = Files.readString(arquivoModelo, StandardCharsets.UTF_8).trim();
                    SKINS.put(chave(base.substring(0, ponto), base.substring(ponto + 1)), new Skin(Files.readAllBytes(arquivo), modelo));
                } catch (IOException e) {
                    LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar skin " + nome + ": ", e);
                }
            });
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao listar skins: ", e);
        }
    }

    public static Skin obter(String chave) {
        return chave == null ? null : SKINS.get(chave);
    }

    public static boolean existe(String chave) {
        return chave != null && SKINS.containsKey(chave);
    }

    private static String nomeBase(String chave) {
        return chave.replace(':', '.');
    }

    public static synchronized void definir(String chave, byte[] dados, String modelo) {
        SKINS.put(chave, new Skin(dados, modelo));
        if (pasta == null) return;
        try {
            Files.createDirectories(pasta);
            Files.write(pasta.resolve(nomeBase(chave) + ".skin"), dados);
            Files.writeString(pasta.resolve(nomeBase(chave) + ".modelo"), modelo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao salvar skin: ", e);
        }
    }

    public static synchronized boolean remover(String chave) {
        boolean tinha = SKINS.remove(chave) != null;
        if (pasta != null) {
            try {
                Files.deleteIfExists(pasta.resolve(nomeBase(chave) + ".skin"));
                Files.deleteIfExists(pasta.resolve(nomeBase(chave) + ".modelo"));
            } catch (IOException e) {
                LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao remover skin: ", e);
            }
        }
        return tinha;
    }

    @SubscribeEvent
    public static void aoPararServidor(ServerStoppedEvent evento) {
        SKINS.clear();
        pasta = null;
    }
}
