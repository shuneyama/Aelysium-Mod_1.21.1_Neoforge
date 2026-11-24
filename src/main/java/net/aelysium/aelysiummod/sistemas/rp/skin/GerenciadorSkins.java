package net.aelysium.aelysiummod.sistemas.rp.skin;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.DadosDeus;
import net.aelysium.aelysiummod.sistemas.rp.personagem.GerenciadorPersonagens;
import net.aelysium.aelysiummod.sistemas.rp.personagem.RegistroPersonagens;
import net.aelysium.aelysiummod.sistemas.rp.skin.rede.PacoteSkinJogador;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class GerenciadorSkins {
    public static final int PERMISSAO = 2;
    public static final int TAMANHO_PARTE = 30000;
    public static final int TAMANHO_MAXIMO_CPM = 700_000;
    public static final int TAMANHO_MAXIMO_PNG = 65_535;
    private static final int PRIMEIRO_BYTE_CPM = 83;

    private static final class Montagem {
        final byte[][] partes;
        int recebidas;
        String modelo;

        Montagem(int total, String modelo) {
            this.partes = new byte[total][];
            this.modelo = modelo;
        }
    }

    private static final Map<UUID, Map<String, Montagem>> MONTAGENS = new ConcurrentHashMap<>();

    private GerenciadorSkins() {
    }

    public static String chaveAtiva(ServerPlayer jogador) {
        TipoDeus deus = DadosDeus.get(jogador.serverLevel()).getDeus(jogador.getUUID());
        if (deus != TipoDeus.NONE) return RegistroSkins.chave(RegistroSkins.TIPO_DEUS, deus.id);
        String personagem = GerenciadorPersonagens.atual(jogador);
        if (!GerenciadorPersonagens.SEM_PERSONAGEM.equals(personagem)) return RegistroSkins.chave(RegistroSkins.TIPO_PERSONAGEM, personagem);
        return null;
    }

    private static PacoteSkinJogador pacote(ServerPlayer jogador) {
        RegistroSkins.Skin skin = RegistroSkins.obter(chaveAtiva(jogador));
        return new PacoteSkinJogador(jogador.getUUID(), skin == null ? "" : skin.modelo(), skin == null ? new byte[0] : skin.dados());
    }

    public static void sincronizar(ServerPlayer jogador) {
        PacketDistributor.sendToAllPlayers(pacote(jogador));
    }

    public static void sincronizarUsuarios(MinecraftServer servidor, String chave) {
        for (ServerPlayer p : servidor.getPlayerList().getPlayers()) {
            if (chave.equals(chaveAtiva(p))) sincronizar(p);
        }
    }

    @SubscribeEvent
    public static void aoEntrar(PlayerEvent.PlayerLoggedInEvent evento) {
        if (!(evento.getEntity() instanceof ServerPlayer entrou)) return;
        MinecraftServer servidor = entrou.getServer();
        if (servidor == null) return;
        for (ServerPlayer outro : servidor.getPlayerList().getPlayers()) {
            if (outro != entrou && RegistroSkins.existe(chaveAtiva(outro))) {
                PacketDistributor.sendToPlayer(entrou, pacote(outro));
            }
        }
        if (RegistroSkins.existe(chaveAtiva(entrou))) sincronizar(entrou);
    }

    @SubscribeEvent
    public static void aoSair(PlayerEvent.PlayerLoggedOutEvent evento) {
        MONTAGENS.remove(evento.getEntity().getUUID());
    }

    private static boolean alvoExiste(String tipo, String id) {
        if (RegistroSkins.TIPO_DEUS.equals(tipo)) return TipoDeus.fromId(id) != TipoDeus.NONE;
        return RegistroPersonagens.buscar(id) != null;
    }

    public static void enviarInfo(ServerPlayer jogador, String tipo, String id) {
        RegistroSkins.Skin skin = RegistroSkins.obter(RegistroSkins.chave(tipo, id));
        PacketDistributor.sendToPlayer(jogador, new net.aelysium.aelysiummod.sistemas.rp.skin.rede.PacoteInfoSkin(
                tipo, id, skin != null, skin == null ? "wide" : skin.modelo(),
                skin != null && skin.dados().length > 0 && (skin.dados()[0] & 0xFF) == PRIMEIRO_BYTE_CPM));
    }

    public static void solicitarInfo(ServerPlayer jogador, String tipo, String id) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        if (!RegistroSkins.tipoValido(tipo) || !RegistroSkins.idValido(id)) return;
        enviarInfo(jogador, tipo, id);
    }

    public static void definirEstilo(ServerPlayer jogador, String tipo, String id, String modelo) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        if (!RegistroSkins.tipoValido(tipo) || !RegistroSkins.idValido(id)) return;
        String chave = RegistroSkins.chave(tipo, id);
        RegistroSkins.Skin skin = RegistroSkins.obter(chave);
        String novo = "slim".equals(modelo) ? "slim" : "wide";
        if (skin != null && !novo.equals(skin.modelo())) {
            RegistroSkins.definir(chave, skin.dados(), novo);
            MinecraftServer servidor = jogador.getServer();
            if (servidor != null) sincronizarUsuarios(servidor, chave);
        }
        enviarInfo(jogador, tipo, id);
    }

    private static void avisar(ServerPlayer jogador, String chave, ChatFormatting cor, Object... args) {
        jogador.sendSystemMessage(Component.translatable(chave, args).withStyle(cor));
    }

    public static void receberParte(ServerPlayer jogador, String tipo, String id, int indice, int total, String modelo, byte[] parte) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        if (!RegistroSkins.tipoValido(tipo) || !RegistroSkins.idValido(id) || !alvoExiste(tipo, id)) return;
        int maximoPartes = TAMANHO_MAXIMO_CPM / TAMANHO_PARTE + 2;
        if (total < 1 || total > maximoPartes || indice < 0 || indice >= total || parte.length > TAMANHO_PARTE) return;
        String chave = RegistroSkins.chave(tipo, id);
        Map<String, Montagem> doJogador = MONTAGENS.computeIfAbsent(jogador.getUUID(), k -> new HashMap<>());
        Montagem m = doJogador.get(chave);
        if (m == null || m.partes.length != total || indice == 0) {
            m = new Montagem(total, modelo);
            doJogador.put(chave, m);
        }
        if (m.partes[indice] == null) m.recebidas++;
        m.partes[indice] = parte;
        if (m.recebidas < total) return;
        doJogador.remove(chave);

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        for (byte[] p : m.partes) saida.writeBytes(p);
        byte[] dados = saida.toByteArray();
        String modeloFinal;
        if (dados.length > 0 && (dados[0] & 0xFF) == PRIMEIRO_BYTE_CPM) {
            if (dados.length > TAMANHO_MAXIMO_CPM) {
                avisar(jogador, "mensagem.aelysiummod.skin.invalida", ChatFormatting.RED);
                return;
            }
            modeloFinal = "slim".equals(m.modelo) ? "slim" : "wide";
        } else {
            boolean png = dados.length > 8 && (dados[0] & 0xFF) == 0x89 && dados[1] == 'P' && dados[2] == 'N' && dados[3] == 'G';
            if (!png || dados.length > TAMANHO_MAXIMO_PNG) {
                avisar(jogador, "mensagem.aelysiummod.skin.invalida", ChatFormatting.RED);
                return;
            }
            modeloFinal = "slim".equals(m.modelo) ? "slim" : "wide";
        }
        RegistroSkins.definir(chave, dados, modeloFinal);
        MinecraftServer servidor = jogador.getServer();
        if (servidor != null) sincronizarUsuarios(servidor, chave);
        avisar(jogador, "mensagem.aelysiummod.skin.definida", ChatFormatting.GREEN);
        enviarInfo(jogador, tipo, id);
    }

    public static void remover(ServerPlayer jogador, String tipo, String id) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        if (!RegistroSkins.tipoValido(tipo) || !RegistroSkins.idValido(id)) return;
        String chave = RegistroSkins.chave(tipo, id);
        if (RegistroSkins.remover(chave)) {
            MinecraftServer servidor = jogador.getServer();
            if (servidor != null) sincronizarUsuarios(servidor, chave);
            avisar(jogador, "mensagem.aelysiummod.skin.removida", ChatFormatting.YELLOW);
        }
        enviarInfo(jogador, tipo, id);
    }
}
