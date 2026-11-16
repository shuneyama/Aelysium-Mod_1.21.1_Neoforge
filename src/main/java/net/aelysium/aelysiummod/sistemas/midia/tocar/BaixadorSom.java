package net.aelysium.aelysiummod.sistemas.midia.tocar;

import net.aelysium.aelysiummod.nucleo.texto.SemConversao;
import net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium;
import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BaixadorSom {

    private static final Logger LOGGER = LoggerFactory.getLogger("AelysiumSons");

    private static final Set<String> HOSTS_YOUTUBE = Set.of("youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com", "youtu.be");
    private static final Set<String> HOSTS_SPOTIFY = Set.of("open.spotify.com");
    private static final int DURACAO_MAXIMA_SEGUNDOS = 480;
    private static final long TAMANHO_MAXIMO_BYTES = 40L * 1024 * 1024;
    private static final long TEMPO_LIMITE_SEGUNDOS = 240;
    private static final int TOTAL_FASES = 5;
    private static final AtomicBoolean OCUPADO = new AtomicBoolean(false);

    private static final Pattern META_TITULO = Pattern.compile("<meta[^>]+property=\"og:title\"[^>]+content=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern META_DESCRICAO = Pattern.compile("<meta[^>]+property=\"og:description\"[^>]+content=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern PROGRESSO_YTDLP = Pattern.compile("\\[download\\]\\s+([0-9]+(?:\\.[0-9]+)?)%");

    private static class ErroBaixar extends Exception {
        ErroBaixar(String mensagem) {
            super(mensagem);
        }
    }

    private static class Progresso {
        private final CommandSourceStack origem;
        private final MinecraftServer servidor;
        private int fase;
        private String nomeFase = "";
        private int ultimoMarco = -1;

        Progresso(CommandSourceStack origem) {
            this.origem = origem;
            this.servidor = origem.getServer();
        }

        void fase(int numero, String nome) {
            fase = numero;
            nomeFase = nome;
            ultimoMarco = -1;
            enviarChat("§e[" + numero + "/" + TOTAL_FASES + "] §f" + nome + "...");
        }

        void percentual(double valor) {
            int percentual = (int) Math.max(0, Math.min(100, Math.floor(valor)));
            int marco = percentual / 10 * 10;
            if (marco > 0 && marco > ultimoMarco) {
                ultimoMarco = marco;
                enviarChat("§7    " + nomeFase + ": §a" + marco + "%");
            }
        }

        private void enviarChat(String texto) {
            servidor.execute(() -> SemConversao.executar(() -> origem.sendSuccess(() -> Component.literal(texto), false)));
        }
    }

    public static void iniciar(CommandSourceStack origem, String url, String nomeEscolhido) {
        MinecraftServer servidor = origem.getServer();
        URI uri;
        try {
            uri = validarUrl(url);
        } catch (ErroBaixar e) {
            origem.sendFailure(Component.literal("§c" + e.getMessage()));
            return;
        }
        if (!OCUPADO.compareAndSet(false, true)) {
            origem.sendFailure(Component.literal("§cJá existe um download de som em andamento. Aguarde terminar."));
            return;
        }

        Progresso progresso = new Progresso(origem);
        Thread fio = new Thread(() -> {
            try {
                String nomeFinal = executar(uri, nomeEscolhido, progresso);
                servidor.execute(() -> SemConversao.executar(() -> origem.sendSuccess(() -> Component.literal("§a[" + TOTAL_FASES + "/" + TOTAL_FASES + "] Pronto! Som salvo como §f" + nomeFinal
                        + "§a. Use /aelysium sons tocar " + nomeFinal + " ..."), false)));
            } catch (ErroBaixar e) {
                servidor.execute(() -> SemConversao.executar(() -> origem.sendFailure(Component.literal("§c" + e.getMessage()))));
            } catch (Exception e) {
                LOGGER.error("Falha ao baixar som: ", e);
                servidor.execute(() -> SemConversao.executar(() -> origem.sendFailure(Component.literal("§cFalha inesperada ao baixar o som: " + e.getMessage()))));
            } finally {
                OCUPADO.set(false);
            }
        }, "aelysium-baixar-som");
        fio.setDaemon(true);
        fio.start();
    }

    private static URI validarUrl(String url) throws ErroBaixar {
        try {
            URI uri = URI.create(url.trim());
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            if (!"https".equalsIgnoreCase(uri.getScheme())) throw new ErroBaixar("Use um link https do YouTube ou do Spotify.");
            if (!HOSTS_YOUTUBE.contains(host) && !HOSTS_SPOTIFY.contains(host)) {
                throw new ErroBaixar("Só links do YouTube ou do Spotify são aceitos.");
            }
            if (HOSTS_SPOTIFY.contains(host) && (uri.getPath() == null || !uri.getPath().contains("/track/"))) {
                throw new ErroBaixar("No Spotify só links de faixas (/track/...) são aceitos.");
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new ErroBaixar("Link inválido.");
        }
    }

    private static String executar(URI uri, String nomeEscolhido, Progresso progresso) throws Exception {
        progresso.fase(1, "Verificando as ferramentas do servidor");
        String ytdlp = localizarFerramenta("yt-dlp");
        String ffmpeg = localizarFerramenta("ffmpeg");

        progresso.fase(2, "Buscando as informações do áudio");
        boolean spotify = HOSTS_SPOTIFY.contains(uri.getHost().toLowerCase(Locale.ROOT));
        String alvo = spotify ? "ytsearch1:" + buscaDoSpotify(uri) : uri.toString();

        Path temporario = Files.createTempDirectory("aelysium-som-");
        try {
            List<String> metadados = executarComando(temporario, "metadados",
                    List.of(ytdlp, "--no-playlist", "--no-warnings", "--skip-download",
                            "--print", "%(title)s", "--print", "%(duration)s", alvo), null);
            int total = metadados.size();
            String titulo = total >= 2 ? metadados.get(total - 2).trim() : "som";
            double duracao = -1;
            if (total >= 2) {
                try {
                    duracao = Double.parseDouble(metadados.get(total - 1).trim());
                    if (duracao > DURACAO_MAXIMA_SEGUNDOS) {
                        throw new ErroBaixar("O áudio passa de " + (DURACAO_MAXIMA_SEGUNDOS / 60) + " minutos, que é o limite.");
                    }
                } catch (NumberFormatException ignored) {}
            }

            progresso.fase(3, "Baixando o áudio");
            executarComando(temporario, "download",
                    List.of(ytdlp, "--no-playlist", "--no-warnings", "--newline", "-f", "bestaudio/best",
                            "--max-filesize", "80M", "-o", temporario.resolve("entrada.%(ext)s").toString(), alvo),
                    linha -> {
                        Matcher m = PROGRESSO_YTDLP.matcher(linha);
                        if (m.find()) progresso.percentual(Double.parseDouble(m.group(1)));
                    });

            Path entrada;
            try (Stream<Path> arquivos = Files.list(temporario)) {
                entrada = arquivos.filter(p -> p.getFileName().toString().startsWith("entrada."))
                        .findFirst().orElseThrow(() -> new ErroBaixar("O yt-dlp não baixou nenhum áudio."));
            }

            progresso.fase(4, "Convertendo para .ogg");
            Path saida = temporario.resolve("saida.ogg");
            double duracaoConversao = duracao > 0 ? Math.min(duracao, DURACAO_MAXIMA_SEGUNDOS) : -1;
            executarComando(temporario, "conversao",
                    List.of(ffmpeg, "-y", "-nostats", "-progress", "pipe:1", "-i", entrada.toString(), "-vn",
                            "-t", String.valueOf(DURACAO_MAXIMA_SEGUNDOS),
                            "-c:a", "libvorbis", "-q:a", "4", "-ar", "44100", saida.toString()),
                    linha -> {
                        if (duracaoConversao > 0 && linha.startsWith("out_time_us=")) {
                            try {
                                double segundos = Long.parseLong(linha.substring("out_time_us=".length()).trim()) / 1_000_000.0;
                                progresso.percentual(segundos / duracaoConversao * 100.0);
                            } catch (NumberFormatException ignored) {}
                        }
                    });
            if (!Files.exists(saida) || Files.size(saida) == 0) throw new ErroBaixar("A conversão para .ogg falhou.");
            if (Files.size(saida) > TAMANHO_MAXIMO_BYTES) throw new ErroBaixar("O arquivo final passou do limite de tamanho.");

            progresso.fase(5, "Salvando na pasta de sons");
            String base = limparNome(nomeEscolhido != null && !nomeEscolhido.isBlank() ? nomeEscolhido : titulo);
            Path pasta = GerenciadorSom.getPastaSons();
            Files.createDirectories(pasta);
            String nome = base + ".ogg";
            for (int n = 2; Files.exists(pasta.resolve(nome)); n++) nome = base + "_" + n + ".ogg";
            Files.move(saida, pasta.resolve(nome), StandardCopyOption.REPLACE_EXISTING);
            return nome;
        } finally {
            apagarPasta(temporario);
        }
    }

    private static String buscaDoSpotify(URI uri) throws Exception {
        HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest pedido = HttpRequest.newBuilder(URI.create("https://open.spotify.com" + uri.getPath()))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept-Language", "en")
                .GET().build();
        String html = cliente.send(pedido, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();

        Matcher titulo = META_TITULO.matcher(html);
        if (!titulo.find()) throw new ErroBaixar("Não consegui ler os dados dessa faixa do Spotify.");
        String musica = desescaparHtml(titulo.group(1));
        String artista = "";
        Matcher descricao = META_DESCRICAO.matcher(html);
        if (descricao.find()) {
            String texto = desescaparHtml(descricao.group(1));
            int corte = texto.indexOf(" · ");
            artista = corte > 0 ? texto.substring(0, corte) : "";
        }
        String consulta = (artista + " " + musica).trim();
        return consulta.length() > 150 ? consulta.substring(0, 150) : consulta;
    }

    private static String desescaparHtml(String texto) {
        return texto.replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&#x27;", "'")
                .replace("&lt;", "<").replace("&gt;", ">");
    }

    private static String limparNome(String bruto) {
        String semAcento = Normalizer.normalize(bruto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String limpo = semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (limpo.length() > 48) limpo = limpo.substring(0, 48).replaceAll("_+$", "");
        return limpo.isEmpty() ? "som" : limpo;
    }

    private static String localizarFerramenta(String nome) throws ErroBaixar {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        String arquivo = windows ? nome + ".exe" : nome;
        List<String> candidatos = new ArrayList<>();
        if (PastaAelysium.pronta()) candidatos.add(PastaAelysium.pasta("ferramentas").resolve(arquivo).toAbsolutePath().toString());
        candidatos.add(AelysiumDiretorioConfiguracao.get().resolve("ffmpeg").resolve(arquivo).toAbsolutePath().toString());
        candidatos.add(nome);

        for (String candidato : candidatos) {
            java.io.File arquivoCandidato = new java.io.File(candidato);
            if (arquivoCandidato.isFile() && !arquivoCandidato.canExecute()) arquivoCandidato.setExecutable(true);
            if (funciona(candidato, nome.equals("ffmpeg") ? "-version" : "--version")) return candidato;
        }
        throw new ErroBaixar("O servidor não encontrou o " + nome + ". Coloque o " + arquivo
                + " em world/aelysium/ferramentas ou instale-o no PATH do servidor.");
    }

    private static boolean funciona(String executavel, String argumento) {
        try {
            Process processo = new ProcessBuilder(executavel, argumento)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!processo.waitFor(15, TimeUnit.SECONDS)) {
                processo.destroyForcibly();
                return false;
            }
            return processo.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static List<String> executarComando(Path pasta, String etapa, List<String> comando, Consumer<String> aoLinha) throws Exception {
        ProcessBuilder construtor = new ProcessBuilder(comando)
                .directory(pasta.toFile())
                .redirectErrorStream(true);
        construtor.environment().put("PYTHONUTF8", "1");
        construtor.environment().put("PYTHONIOENCODING", "utf-8");
        Process processo = construtor.start();

        List<String> linhas = java.util.Collections.synchronizedList(new ArrayList<>());
        Thread leitor = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(processo.getInputStream(), StandardCharsets.UTF_8))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    if (linha.isBlank()) continue;
                    linhas.add(linha);
                    if (aoLinha != null) {
                        try {
                            aoLinha.accept(linha);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (IOException ignored) {}
        }, "aelysium-baixar-som-leitor");
        leitor.setDaemon(true);
        leitor.start();

        if (!processo.waitFor(TEMPO_LIMITE_SEGUNDOS, TimeUnit.SECONDS)) {
            processo.descendants().forEach(ProcessHandle::destroyForcibly);
            processo.destroyForcibly();
            throw new ErroBaixar("A etapa de " + etapa + " passou do tempo limite.");
        }
        leitor.join(5000);

        List<String> copia = new ArrayList<>(linhas);
        if (processo.exitValue() != 0) {
            String ultima = copia.isEmpty() ? "sem detalhes" : copia.get(copia.size() - 1);
            LOGGER.error("Etapa {} falhou: {}", etapa, copia);
            throw new ErroBaixar("A etapa de " + etapa + " falhou: " + ultima);
        }
        return copia;
    }

    private static void apagarPasta(Path pasta) {
        try (Stream<Path> arquivos = Files.walk(pasta)) {
            arquivos.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }
}
