package net.aelysium.aelysiummod.sistemas.midia.tocar.cliente;

import net.aelysium.aelysiummod.sistemas.midia.tocar.rede.PacotesSom;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@OnlyIn(Dist.CLIENT)
public class ManipuladorTocarCliente {

    private static final double SEGUNDOS_FADE_IN = 2.0;
    private static final double SEGUNDOS_FADE_OUT = 1.0;
    private static final double SEGUNDOS_BUFFER_LINHA = 0.25;
    private static final int QUADROS_POR_BLOCO = 1024;

    private static final Map<String, byte[]> cache = new ConcurrentHashMap<>();
    private static final Map<String, BufferMontagem> montagem = new ConcurrentHashMap<>();
    private static final List<Reproducao> reproducoes = new CopyOnWriteArrayList<>();

    private static class Reproducao {
        volatile SourceDataLine linha;
        volatile boolean parando;
        volatile boolean encerrar;
    }

    public static void handleIniciar(PacotesSom.PacoteIniciarSom payload, IPayloadContext context) {
        cache.remove(payload.nome());
        montagem.put(payload.nome(), new BufferMontagem(payload.nome(), payload.totalSize()));
    }

    public static void handleChunk(PacotesSom.PacoteChunkSom payload, IPayloadContext context) {
        BufferMontagem buf = montagem.get(payload.nome());
        if (buf == null) return;
        buf.addChunk(payload.chunkIndex(), payload.data());
    }

    public static void handleParar(PacotesSom.PacoteParar payload, IPayloadContext context) {
        for (Reproducao reproducao : reproducoes) reproducao.parando = true;
    }

    public static void pararTudo() {
        for (Reproducao reproducao : reproducoes) {
            reproducao.encerrar = true;
            SourceDataLine linha = reproducao.linha;
            if (linha != null) {
                try { linha.stop(); linha.flush(); linha.close(); } catch (Exception ignored) {}
            }
        }
        reproducoes.clear();
        montagem.clear();
        cache.clear();
    }

    public static void handleTocar(PacotesSom.PacoteTocarSom payload, IPayloadContext context) {
        String nome = payload.nome();
        byte[] dados = cache.get(nome);

        if (dados == null) {
            BufferMontagem buf = montagem.remove(nome);
            if (buf == null || !buf.isCompleto()) return;
            dados = buf.getDados();
            cache.put(nome, dados);
        }

        final byte[] dadosFinais = dados;
        final float volumeServidor = payload.volume();
        final boolean loop = payload.loop();
        final float transicao = payload.transicaoSegundos();
        Thread thread = new Thread(() -> reproduzirOgg(dadosFinais, volumeServidor, loop, transicao), "aelysium-som-" + nome);
        thread.setDaemon(true);
        thread.start();
    }

    private static void reproduzirOgg(byte[] dados, float volume, boolean loop, float transicaoSegundos) {
        Reproducao reproducao = new Reproducao();
        reproducoes.add(reproducao);
        ByteBuffer buffer = MemoryUtil.memAlloc(dados.length);
        try {
            buffer.put(dados);
            buffer.flip();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer error = stack.mallocInt(1);
                long decoder = STBVorbis.stb_vorbis_open_memory(buffer, error, null);
                if (decoder == 0) return;

                STBVorbisInfo info = STBVorbisInfo.malloc(stack);
                STBVorbis.stb_vorbis_get_info(decoder, info);

                int channels = info.channels();
                int sampleRate = info.sample_rate();

                int totalQuadros = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
                ShortBuffer pcm = MemoryUtil.memAllocShort(totalQuadros * channels);

                try {
                    STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
                    STBVorbis.stb_vorbis_close(decoder);

                    AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
                    DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, format);
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(lineInfo);
                    int bytesPorQuadro = channels * 2;
                    int bytesBufferLinha = Math.max(4096, (int) (sampleRate * SEGUNDOS_BUFFER_LINHA) * bytesPorQuadro);
                    line.open(format, bytesBufferLinha);
                    line.start();
                    reproducao.linha = line;

                    try {
                        long quadrosTransicao = loop ? Math.min((long) (transicaoSegundos * sampleRate), totalQuadros / 2) : 0;
                        long passo = loop ? Math.max(1, totalQuadros - quadrosTransicao) : totalQuadros;
                        byte[] bloco = new byte[QUADROS_POR_BLOCO * bytesPorQuadro];
                        double quadrosFadeIn = Math.max(1.0, sampleRate * SEGUNDOS_FADE_IN);
                        double quadrosFadeOut = Math.max(1.0, sampleRate * SEGUNDOS_FADE_OUT);
                        long quadroInicioFadeOut = -1;
                        double ganhoNoInicioDoFadeOut = 1.0;
                        long tempo = 0;
                        boolean terminouFade = false;

                        while (!reproducao.encerrar && !terminouFade) {
                            float vol = Math.max(0.0f, volume * (float) ConfiguracaoSomClienteAelysium.getVolumeNormalizado());
                            int quadrosNoBloco = QUADROS_POR_BLOCO;
                            if (!loop) {
                                long restantes = totalQuadros - tempo;
                                if (restantes <= 0) break;
                                quadrosNoBloco = (int) Math.min(quadrosNoBloco, restantes);
                            }

                            int escritos = 0;
                            for (int f = 0; f < quadrosNoBloco; f++) {
                                long tf = tempo + f;
                                double ganhoGlobal = Math.min(1.0, tf / quadrosFadeIn);
                                if (reproducao.parando) {
                                    if (quadroInicioFadeOut < 0) {
                                        quadroInicioFadeOut = tf;
                                        ganhoNoInicioDoFadeOut = ganhoGlobal;
                                    }
                                    double restante = 1.0 - (tf - quadroInicioFadeOut) / quadrosFadeOut;
                                    if (restante <= 0.0) {
                                        terminouFade = true;
                                        break;
                                    }
                                    ganhoGlobal = ganhoNoInicioDoFadeOut * restante;
                                }

                                long iteracao = tf / passo;
                                long pos = tf - iteracao * passo;
                                double ganhoAtual = 1.0;
                                double ganhoAnterior = 0.0;
                                if (quadrosTransicao > 0 && iteracao >= 1 && pos < quadrosTransicao) {
                                    double x = (double) pos / quadrosTransicao;
                                    ganhoAtual = Math.sin(x * Math.PI / 2.0);
                                    ganhoAnterior = Math.cos(x * Math.PI / 2.0);
                                }

                                for (int c = 0; c < channels; c++) {
                                    double amostra = pcm.get((int) pos * channels + c) * ganhoAtual;
                                    if (ganhoAnterior > 0.0) {
                                        long posAnterior = pos + passo;
                                        if (posAnterior < totalQuadros) amostra += pcm.get((int) posAnterior * channels + c) * ganhoAnterior;
                                    }
                                    int escalado = (int) Math.round(amostra * vol * ganhoGlobal);
                                    if (escalado > Short.MAX_VALUE) escalado = Short.MAX_VALUE;
                                    if (escalado < Short.MIN_VALUE) escalado = Short.MIN_VALUE;
                                    int indice = (escritos * channels + c) * 2;
                                    bloco[indice] = (byte) (escalado & 0xFF);
                                    bloco[indice + 1] = (byte) ((escalado >> 8) & 0xFF);
                                }
                                escritos++;
                            }
                            if (escritos > 0) line.write(bloco, 0, escritos * bytesPorQuadro);
                            tempo += escritos;
                        }
                        if (!reproducao.encerrar) line.drain();
                    } finally {
                        line.close();
                    }
                } finally {
                    MemoryUtil.memFree(pcm);
                }
            }
        } catch (Exception ignored) {
        } finally {
            reproducoes.remove(reproducao);
            MemoryUtil.memFree(buffer);
        }
    }

    private static class BufferMontagem {
        private final String nome;
        private final int totalSize;
        private final Map<Integer, byte[]> chunks = new HashMap<>();
        private int recebidos = 0;

        BufferMontagem(String nome, int totalSize) {
            this.nome = nome;
            this.totalSize = totalSize;
        }

        void addChunk(int index, byte[] data) {
            chunks.put(index, data);
            recebidos += data.length;
        }

        boolean isCompleto() {
            return recebidos >= totalSize;
        }

        byte[] getDados() {
            byte[] resultado = new byte[totalSize];
            int pos = 0;
            int i = 0;
            while (chunks.containsKey(i)) {
                byte[] chunk = chunks.get(i);
                int len = Math.min(chunk.length, totalSize - pos);
                System.arraycopy(chunk, 0, resultado, pos, len);
                pos += len;
                i++;
            }
            return pos < totalSize ? Arrays.copyOf(resultado, pos) : resultado;
        }
    }
}
