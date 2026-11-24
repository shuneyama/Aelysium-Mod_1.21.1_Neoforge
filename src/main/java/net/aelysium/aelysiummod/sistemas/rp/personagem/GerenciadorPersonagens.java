package net.aelysium.aelysiummod.sistemas.rp.personagem;

import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.DadosDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.GerenciadorDeuses;
import net.aelysium.aelysiummod.nucleo.jogador.ConsultaInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.rp.personagem.rede.PacoteListaPersonagens;
import net.aelysium.aelysiummod.sistemas.rp.raca.AnimacaoRevelacaoRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.DadosRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.GerenciadorRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.TemporizadorRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteVooValkyria;
import net.aelysium.aelysiummod.sistemas.social.apelido.DadosApelido;
import net.aelysium.aelysiummod.sistemas.social.apelido.comando.ComandoApelido;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;
import java.util.UUID;

public final class GerenciadorPersonagens {

    public static final int PERMISSAO = 2;
    public static final String SEM_PERSONAGEM = "none";

    private static final String CHAVE = "AelysiumPersonagem";

    private GerenciadorPersonagens() {
    }

    private static CompoundTag dados(ServerPlayer jogador) {
        return jogador.getPersistentData().getCompound(CHAVE);
    }

    public static String atual(ServerPlayer jogador) {
        String id = dados(jogador).getString("atual");
        if (id.isEmpty() || RegistroPersonagens.buscar(id) == null) return SEM_PERSONAGEM;
        return id;
    }

    public static void escolher(ServerPlayer jogador, String id) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        if (id == null || id.isEmpty() || SEM_PERSONAGEM.equalsIgnoreCase(id)) {
            desvincular(jogador);
        } else {
            Personagem personagem = RegistroPersonagens.buscar(id);
            if (personagem != null && !personagem.id().equalsIgnoreCase(dados(jogador).getString("atual"))) {
                if (DadosDeus.get(jogador.serverLevel()).getDeus(jogador.getUUID()) != TipoDeus.NONE) {
                    GerenciadorDeuses.escolher(jogador, "none");
                }
                adotar(jogador, personagem);
            }
        }
        net.aelysium.aelysiummod.sistemas.rp.skin.GerenciadorSkins.sincronizar(jogador);
        enviarLista(jogador);
    }

    public static void salvar(ServerPlayer jogador, Personagem bruto) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        Personagem salvo = RegistroPersonagens.salvarOuCriar(bruto);
        if (salvo == null) {
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.personagem.nome_invalido").withStyle(ChatFormatting.RED));
            enviarLista(jogador);
            return;
        }
        MinecraftServer servidor = jogador.getServer();
        if (servidor != null) {
            for (ServerPlayer outro : servidor.getPlayerList().getPlayers()) {
                if (salvo.id().equalsIgnoreCase(dados(outro).getString("atual"))) {
                    aplicar(outro, salvo, true);
                }
            }
        }
        enviarLista(jogador);
    }

    public static void excluir(ServerPlayer jogador, String id) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        MinecraftServer servidor = jogador.getServer();
        if (servidor != null) {
            for (ServerPlayer outro : servidor.getPlayerList().getPlayers()) {
                if (id.equalsIgnoreCase(dados(outro).getString("atual"))) {
                    desvincular(outro);
                    net.aelysium.aelysiummod.sistemas.rp.skin.GerenciadorSkins.sincronizar(outro);
                }
            }
        }
        AtributosPersonagem.limpar(id);
        net.aelysium.aelysiummod.sistemas.rp.skin.RegistroSkins.remover(net.aelysium.aelysiummod.sistemas.rp.skin.RegistroSkins.chave(net.aelysium.aelysiummod.sistemas.rp.skin.RegistroSkins.TIPO_PERSONAGEM, id));
        RegistroPersonagens.excluir(id);
        enviarLista(jogador);
    }

    public static void enviarLista(ServerPlayer jogador) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        PacketDistributor.sendToPlayer(jogador, new PacoteListaPersonagens(RegistroPersonagens.todos(), atual(jogador)));
    }

    private static void adotar(ServerPlayer jogador, Personagem personagem) {
        CompoundTag tag = jogador.getPersistentData().getCompound(CHAVE);
        MinecraftServer servidor = jogador.getServer();
        if (servidor == null) return;
        if (!tag.contains("salvo")) tag.put("salvo", capturarEstado(jogador, servidor));
        tag.putString("atual", personagem.id());
        jogador.getPersistentData().put(CHAVE, tag);
        aplicar(jogador, personagem, false);
    }

    private static CompoundTag capturarEstado(ServerPlayer jogador, MinecraftServer servidor) {
        UUID uuid = jogador.getUUID();
        CompoundTag salvo = new CompoundTag();
        DadosApelido apelidos = DadosApelido.get(servidor);
        DadosApelido.EntradaApelido nick = apelidos.getNickname(uuid);
        if (nick != null) {
            salvo.putBoolean("temNick", true);
            salvo.putString("prefix", nick.prefix());
            salvo.putInt("prefixCor1", nick.prefixCor1());
            salvo.putInt("prefixCor2", nick.prefixCor2());
            salvo.putInt("prefixFormat", nick.prefixFormat());
            salvo.putString("nick", nick.nick());
            salvo.putInt("nickCor", nick.nickCor());
            salvo.putInt("nickFormat", nick.nickFormat());
            salvo.putString("suffix", nick.suffix());
            salvo.putInt("suffixCor1", nick.suffixCor1());
            salvo.putInt("suffixCor2", nick.suffixCor2());
            salvo.putInt("suffixFormat", nick.suffixFormat());
        }
        salvo.putString("pronomes", apelidos.getPronomes(uuid));
        String badge = PerfilJogador.insignia().substituicao(servidor, uuid);
        if (badge != null) {
            salvo.putBoolean("temBadge", true);
            salvo.putString("badge", badge);
        }
        salvo.putString("raca", TemporizadorRaca.getCachedRace(uuid).id);
        return salvo;
    }

    private static String badgeEfetivo(Personagem personagem) {
        return ConsultaInsignia.AUTOMATICA.equals(personagem.badge()) ? null : personagem.badge();
    }

    private static void aplicar(ServerPlayer jogador, Personagem personagem, boolean reaplicacao) {
        MinecraftServer servidor = jogador.getServer();
        if (servidor == null) return;
        UUID uuid = jogador.getUUID();
        DadosApelido apelidos = DadosApelido.get(servidor);

        AtributosPersonagem.aplicar(jogador, personagem.id());
        liberarNick(servidor, apelidos, personagem.nome(), uuid);
        apelidos.setNickname(uuid, new DadosApelido.EntradaApelido(
                personagem.prefixo(), personagem.prefixoCor1(), personagem.prefixoCor2(), personagem.prefixoFormato(),
                personagem.nome(), personagem.nickCor(), personagem.nickFormato(),
                personagem.sufixo(), personagem.sufixoCor1(), personagem.sufixoCor2(), personagem.sufixoFormato()));
        apelidos.setPronomes(uuid, personagem.pronomes());

        String badge = badgeEfetivo(personagem);
        if (badge == null) {
            PerfilJogador.insignia().limparSubstituicao(servidor, uuid);
        } else {
            PerfilJogador.insignia().definirSubstituicao(servidor, uuid, badge);
        }

        CompoundTag tag = jogador.getPersistentData().getCompound(CHAVE);
        tag.putString("nickAplicado", personagem.nome());
        tag.putString("pronomesAplicado", personagem.pronomes());
        tag.putBoolean("temBadgeAplicado", badge != null);
        tag.putString("badgeAplicado", badge == null ? "" : badge);
        tag.putString("racaAplicada", personagem.raca());
        jogador.getPersistentData().put(CHAVE, tag);

        apelidos.syncToAll(servidor, uuid);
        ComandoApelido.refreshTabList(jogador);

        TipoRaca raca = TipoRaca.fromId(personagem.raca());
        if (raca != TipoRaca.NONE) trocarRaca(jogador, raca);
    }

    public static void desvincular(ServerPlayer jogador) {
        AtributosPersonagem.removerDoJogador(jogador);
        MinecraftServer servidor = jogador.getServer();
        CompoundTag tag = jogador.getPersistentData().getCompound(CHAVE);
        if (servidor == null || !tag.contains("salvo")) {
            jogador.getPersistentData().remove(CHAVE);
            return;
        }
        UUID uuid = jogador.getUUID();
        CompoundTag salvo = tag.getCompound("salvo");
        DadosApelido apelidos = DadosApelido.get(servidor);

        DadosApelido.EntradaApelido atualNick = apelidos.getNickname(uuid);
        if (atualNick != null && atualNick.nick().equals(tag.getString("nickAplicado"))) {
            if (salvo.getBoolean("temNick")) {
                DadosApelido.EntradaApelido anterior = new DadosApelido.EntradaApelido(
                        salvo.getString("prefix"), salvo.getInt("prefixCor1"), salvo.getInt("prefixCor2"), salvo.getInt("prefixFormat"),
                        salvo.getString("nick"), salvo.getInt("nickCor"), salvo.getInt("nickFormat"),
                        salvo.getString("suffix"), salvo.getInt("suffixCor1"), salvo.getInt("suffixCor2"), salvo.getInt("suffixFormat"));
                if (!anterior.nick().isEmpty()) liberarNick(servidor, apelidos, anterior.nick(), uuid);
                apelidos.setNickname(uuid, anterior);
            } else {
                apelidos.removeNickname(uuid);
            }
        }

        if (apelidos.getPronomes(uuid).equals(tag.getString("pronomesAplicado"))) {
            apelidos.setPronomes(uuid, salvo.getString("pronomes"));
        }

        String badgeAtual = PerfilJogador.insignia().substituicao(servidor, uuid);
        String badgeAplicado = tag.getBoolean("temBadgeAplicado") ? tag.getString("badgeAplicado") : null;
        if (Objects.equals(badgeAtual, badgeAplicado)) {
            if (salvo.getBoolean("temBadge")) {
                PerfilJogador.insignia().definirSubstituicao(servidor, uuid, salvo.getString("badge"));
            } else {
                PerfilJogador.insignia().limparSubstituicao(servidor, uuid);
            }
        }

        TipoRaca aplicada = TipoRaca.fromId(tag.getString("racaAplicada"));
        TipoRaca anterior = TipoRaca.fromId(salvo.getString("raca"));
        TipoRaca atualRaca = TemporizadorRaca.getCachedRace(uuid);
        if (aplicada != TipoRaca.NONE && atualRaca == aplicada && anterior != atualRaca) {
            if (anterior == TipoRaca.NONE) {
                removerRaca(jogador);
            } else {
                trocarRaca(jogador, anterior);
            }
        }

        jogador.getPersistentData().remove(CHAVE);
        apelidos.syncToAll(servidor, uuid);
        ComandoApelido.refreshTabList(jogador);
    }

    private static void trocarRaca(ServerPlayer jogador, TipoRaca nova) {
        TipoRaca atual = TemporizadorRaca.getCachedRace(jogador.getUUID());
        if (atual == nova) return;
        GerenciadorRaca.applyRace(jogador, nova);
        DadosRaca.get(jogador.serverLevel()).setRace(jogador.getUUID(), nova);
        TemporizadorRaca.updateCache(jogador.getUUID(), nova);
        if (nova != TipoRaca.VALKYRIA) PacketDistributor.sendToPlayer(jogador, new PacoteVooValkyria(false, 0, 0));
    }

    private static void removerRaca(ServerPlayer jogador) {
        GerenciadorRaca.applyRace(jogador, TipoRaca.NONE);
        DadosRaca.get(jogador.serverLevel()).setRace(jogador.getUUID(), TipoRaca.NONE);
        TemporizadorRaca.updateCache(jogador.getUUID(), TipoRaca.NONE);
        PacketDistributor.sendToPlayer(jogador, new PacoteVooValkyria(false, 0, 0));
    }

    private static void liberarNick(MinecraftServer servidor, DadosApelido apelidos, String nick, UUID dono) {
        UUID anteriorDono = apelidos.releasePreviousOwner(nick, dono);
        if (anteriorDono == null) return;
        apelidos.syncToAll(servidor, anteriorDono);
        ServerPlayer anteriorJogador = servidor.getPlayerList().getPlayer(anteriorDono);
        if (anteriorJogador != null) ComandoApelido.refreshTabList(anteriorJogador);
    }
}
