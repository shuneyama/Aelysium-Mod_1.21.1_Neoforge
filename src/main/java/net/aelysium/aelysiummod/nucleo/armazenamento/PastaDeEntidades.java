package net.aelysium.aelysiummod.nucleo.armazenamento;

import net.aelysium.aelysiummod.nucleo.jogador.ArquivosJogador;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PastaDeEntidades {

    public static final String EXTENSAO = ".snbt";
    private static final Logger LOGGER = LoggerFactory.getLogger("AelysiumArmazenamento");
    private static final Map<Path, String> ULTIMO_CONTEUDO = new ConcurrentHashMap<>();

    public static Map<String, CompoundTag> ler(Path pasta) {
        Map<String, CompoundTag> lidos = new LinkedHashMap<>();
        if (!Files.isDirectory(pasta)) return lidos;
        try (var stream = Files.list(pasta)) {
            for (Path arquivo : stream.filter(p -> p.getFileName().toString().endsWith(EXTENSAO)).sorted().toList()) {
                try {
                    String texto = Files.readString(arquivo, StandardCharsets.UTF_8);
                    String nome = arquivo.getFileName().toString();
                    lidos.put(nome.substring(0, nome.length() - EXTENSAO.length()), TagParser.parseTag(texto));
                    ULTIMO_CONTEUDO.put(arquivo, texto);
                } catch (Exception e) {
                    LOGGER.error("Erro ao ler {}: ", arquivo, e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Erro ao listar {}: ", pasta, e);
        }
        return lidos;
    }

    public static void gravarTodos(Path pasta, Map<String, CompoundTag> entidades) {
        try {
            Files.createDirectories(pasta);
            Set<Path> mantidos = new HashSet<>();
            Set<String> usados = new HashSet<>();
            for (var entrada : entidades.entrySet()) {
                String base = ArquivosJogador.sanitizar(entrada.getKey());
                String nome = base;
                for (int n = 2; !usados.add(nome.toLowerCase()); n++) nome = base + "_" + n;
                Path destino = pasta.resolve(nome + EXTENSAO);
                mantidos.add(destino);

                String texto = new SnbtPrinterTagVisitor().visit(entrada.getValue());
                if (texto.equals(ULTIMO_CONTEUDO.get(destino)) && Files.exists(destino)) continue;
                Path temporario = destino.resolveSibling(nome + ".tmp");
                Files.writeString(temporario, texto, StandardCharsets.UTF_8);
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
                ULTIMO_CONTEUDO.put(destino, texto);
            }
            try (var stream = Files.list(pasta)) {
                for (Path arquivo : stream.filter(p -> p.getFileName().toString().endsWith(EXTENSAO)).toList()) {
                    if (!mantidos.contains(arquivo)) {
                        Files.deleteIfExists(arquivo);
                        ULTIMO_CONTEUDO.remove(arquivo);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Erro ao gravar {}: ", pasta, e);
        }
    }

    public static void apagarDatMigrado(Path dat, Path pasta, int esperados) {
        if (!Files.exists(dat)) return;
        try (var stream = Files.list(pasta)) {
            long gravados = stream.filter(p -> p.getFileName().toString().endsWith(EXTENSAO)).count();
            if (gravados >= esperados) {
                Files.deleteIfExists(dat);
                LOGGER.info("{} convertido para {} e apagado.", dat.getFileName(), pasta);
            } else {
                LOGGER.error("{} nao foi apagado: so {} de {} arquivos foram gravados.", dat.getFileName(), gravados, esperados);
            }
        } catch (IOException e) {
            LOGGER.error("Erro ao conferir {}: ", pasta, e);
        }
    }

    public static List<CompoundTag> lerDatAntigo(Path dat, String chaveDaLista) {
        List<CompoundTag> lista = new ArrayList<>();
        if (!Files.exists(dat)) return lista;
        try {
            CompoundTag principal = NbtIo.readCompressed(dat, NbtAccounter.unlimitedHeap());
            ListTag tags = principal.getList(chaveDaLista, Tag.TAG_COMPOUND);
            for (int i = 0; i < tags.size(); i++) lista.add(tags.getCompound(i));
        } catch (IOException e) {
            LOGGER.error("Erro ao ler {}: ", dat, e);
        }
        return lista;
    }
}
