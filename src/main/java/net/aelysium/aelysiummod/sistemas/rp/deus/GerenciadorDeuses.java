package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.aelysium.aelysiummod.sistemas.social.apelido.DadosApelido;
import net.aelysium.aelysiummod.sistemas.social.apelido.comando.ComandoApelido;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteListaDeuses;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class GerenciadorDeuses {
    public static final int PERMISSAO = 2;

    private GerenciadorDeuses() {
    }

    public static void definir(ServerPlayer jogador, TipoDeus deus) {
        if (FormaDivina.estaAtiva(jogador)) {
            FormaDivina.desativar(jogador);
        }
        jogador.setInvulnerable(deus.tem(TipoDeus.INVULNERAVEL));
        DadosDeus.get(jogador.serverLevel()).setDeus(jogador.getUUID(), deus);
        AtributosDeus.aplicar(jogador, deus, true);
        net.aelysium.aelysiummod.sistemas.rp.personagem.AtributosPersonagem.aplicar(jogador, net.aelysium.aelysiummod.sistemas.rp.personagem.AtributosPersonagem.chaveDeus(deus.id));
    }

    public static boolean remover(ServerPlayer jogador) {
        DadosDeus dados = DadosDeus.get(jogador.serverLevel());
        if (dados.getDeus(jogador.getUUID()) == TipoDeus.NONE) return false;
        if (FormaDivina.estaAtiva(jogador)) {
            FormaDivina.desativar(jogador);
        }
        dados.setDeus(jogador.getUUID(), TipoDeus.NONE);
        jogador.setInvulnerable(false);
        AtributosDeus.remover(jogador);
        net.aelysium.aelysiummod.sistemas.rp.personagem.AtributosPersonagem.removerDoJogador(jogador);
        if (!jogador.isCreative() && !jogador.isSpectator()) {
            jogador.getAbilities().mayfly = false;
            jogador.getAbilities().flying = false;
            jogador.onUpdateAbilities();
        }
        return true;
    }

    public static void escolher(ServerPlayer jogador, String id) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        TipoDeus deus = TipoDeus.fromId(id);
        TipoDeus anterior = DadosDeus.get(jogador.serverLevel()).getDeus(jogador.getUUID());
        if (deus == TipoDeus.NONE) {
            remover(jogador);
            ajustarIdentidade(jogador, anterior, null);
        } else {
            if (!net.aelysium.aelysiummod.sistemas.rp.personagem.GerenciadorPersonagens.SEM_PERSONAGEM.equals(net.aelysium.aelysiummod.sistemas.rp.personagem.GerenciadorPersonagens.atual(jogador))) {
                net.aelysium.aelysiummod.sistemas.rp.personagem.GerenciadorPersonagens.desvincular(jogador);
            }
            definir(jogador, deus);
            ajustarIdentidade(jogador, anterior, deus);
        }
        net.aelysium.aelysiummod.sistemas.rp.skin.GerenciadorSkins.sincronizar(jogador);
        enviarLista(jogador);
    }

    private static final String CHAVE_NICK_ANTERIOR = "AelysiumNickAnteriorDeus";

    private static void guardarNickAnterior(ServerPlayer jogador, DadosApelido.EntradaApelido atual) {
        var dados = jogador.getPersistentData();
        if (dados.contains(CHAVE_NICK_ANTERIOR)) return;
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        if (atual != null) {
            tag.putBoolean("tinha", true);
            tag.putString("prefix", atual.prefix());
            tag.putInt("prefixCor1", atual.prefixCor1());
            tag.putInt("prefixCor2", atual.prefixCor2());
            tag.putInt("prefixFormat", atual.prefixFormat());
            tag.putString("nick", atual.nick());
            tag.putInt("nickCor", atual.nickCor());
            tag.putInt("nickFormat", atual.nickFormat());
            tag.putString("suffix", atual.suffix());
            tag.putInt("suffixCor1", atual.suffixCor1());
            tag.putInt("suffixCor2", atual.suffixCor2());
            tag.putInt("suffixFormat", atual.suffixFormat());
        }
        dados.put(CHAVE_NICK_ANTERIOR, tag);
    }

    private static final String CHAVE_PRONOMES_ANTERIORES = "AelysiumPronomesAnterioresDeus";

    private static void guardarPronomesAnteriores(ServerPlayer jogador, String valor) {
        var dados = jogador.getPersistentData();
        if (dados.contains(CHAVE_PRONOMES_ANTERIORES)) return;
        dados.putString(CHAVE_PRONOMES_ANTERIORES, valor == null ? "" : valor);
    }

    private static final String CHAVE_INSIGNIA_ANTERIOR = "AelysiumInsigniaAnteriorDeus";

    private static void guardarInsigniaAnterior(ServerPlayer jogador, String valor) {
        var dados = jogador.getPersistentData();
        if (dados.contains(CHAVE_INSIGNIA_ANTERIOR)) return;
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        if (valor != null) {
            tag.putBoolean("tinha", true);
            tag.putString("valor", valor);
        }
        dados.put(CHAVE_INSIGNIA_ANTERIOR, tag);
    }

    private static void restaurarInsignia(ServerPlayer jogador, net.minecraft.server.MinecraftServer servidor, java.util.UUID uuid) {
        net.minecraft.nbt.CompoundTag tag = jogador.getPersistentData().getCompound(CHAVE_INSIGNIA_ANTERIOR);
        var insignias = PerfilJogador.insignia();
        if (tag.getBoolean("tinha")) {
            insignias.definirSubstituicao(servidor, uuid, tag.getString("valor"));
        } else {
            insignias.limparSubstituicao(servidor, uuid);
        }
    }

    private static DadosApelido.EntradaApelido lerNickAnterior(ServerPlayer jogador) {
        net.minecraft.nbt.CompoundTag tag = jogador.getPersistentData().getCompound(CHAVE_NICK_ANTERIOR);
        if (!tag.getBoolean("tinha")) return null;
        return new DadosApelido.EntradaApelido(
                tag.getString("prefix"), tag.getInt("prefixCor1"), tag.getInt("prefixCor2"), tag.getInt("prefixFormat"),
                tag.getString("nick"), tag.getInt("nickCor"), tag.getInt("nickFormat"),
                tag.getString("suffix"), tag.getInt("suffixCor1"), tag.getInt("suffixCor2"), tag.getInt("suffixFormat"));
    }

    private static void ajustarIdentidade(ServerPlayer jogador, TipoDeus anterior, TipoDeus novo) {
        var servidor = jogador.getServer();
        if (servidor == null) return;
        java.util.UUID uuid = jogador.getUUID();
        var insignias = PerfilJogador.insignia();
        String idInsignia = novo != null ? novo.badge : (anterior != null && anterior != TipoDeus.NONE ? anterior.badge : TipoInsignia.DEUS.id);
        DadosApelido apelidos = DadosApelido.get(servidor);
        DadosApelido.EntradaApelido atual = apelidos.getNickname(uuid);

        if (novo != null) {
            guardarNickAnterior(jogador, atual);
            guardarInsigniaAnterior(jogador, insignias.substituicao(servidor, uuid));
            insignias.definirSubstituicao(servidor, uuid, idInsignia);
            guardarPronomesAnteriores(jogador, apelidos.getPronomes(uuid));
            apelidos.setPronomes(uuid, novo.pronomes);
            liberarNick(servidor, apelidos, novo.displayName, uuid);
            DadosApelido.EntradaApelido entrada = atual == null
                    ? new DadosApelido.EntradaApelido("", 0xFFFFFF, -1, 0, novo.displayName, novo.color, 0, "", 0xFFFFFF, -1, 0)
                    : new DadosApelido.EntradaApelido(atual.prefix(), atual.prefixCor1(), atual.prefixCor2(), atual.prefixFormat(),
                            novo.displayName, novo.color, atual.nickFormat(),
                            atual.suffix(), atual.suffixCor1(), atual.suffixCor2(), atual.suffixFormat());
            apelidos.setNickname(uuid, entrada);
        } else {
            if (idInsignia.equals(insignias.substituicao(servidor, uuid))) {
                restaurarInsignia(jogador, servidor, uuid);
            }
            boolean aindaDoDeus = atual != null && anterior != null && anterior != TipoDeus.NONE && anterior.displayName.equals(atual.nick());
            if (aindaDoDeus) {
                DadosApelido.EntradaApelido salvo = lerNickAnterior(jogador);
                if (salvo == null) {
                    apelidos.removeNickname(uuid);
                } else {
                    if (!salvo.nick().isEmpty()) liberarNick(servidor, apelidos, salvo.nick(), uuid);
                    apelidos.setNickname(uuid, salvo);
                }
            }
            if (anterior != null && anterior != TipoDeus.NONE && jogador.getPersistentData().contains(CHAVE_PRONOMES_ANTERIORES)
                    && apelidos.getPronomes(uuid).equals(anterior.pronomes)) {
                apelidos.setPronomes(uuid, jogador.getPersistentData().getString(CHAVE_PRONOMES_ANTERIORES));
            }
            jogador.getPersistentData().remove(CHAVE_PRONOMES_ANTERIORES);
            jogador.getPersistentData().remove(CHAVE_NICK_ANTERIOR);
            jogador.getPersistentData().remove(CHAVE_INSIGNIA_ANTERIOR);
        }
        apelidos.syncToAll(servidor, uuid);
        ComandoApelido.refreshTabList(jogador);
    }

    private static void liberarNick(net.minecraft.server.MinecraftServer servidor, DadosApelido apelidos, String nick, java.util.UUID dono) {
        java.util.UUID anteriorDono = apelidos.releasePreviousOwner(nick, dono);
        if (anteriorDono == null) return;
        apelidos.syncToAll(servidor, anteriorDono);
        ServerPlayer anteriorJogador = servidor.getPlayerList().getPlayer(anteriorDono);
        if (anteriorJogador != null) ComandoApelido.refreshTabList(anteriorJogador);
    }

    public static void criar(ServerPlayer jogador, String nome, int cor, String badge, int corParticula, String pronomes, int poderes) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        String limpo = nome == null ? "" : nome.replaceAll("\\s+", "");
        if (limpo.isEmpty() || limpo.length() > RegistroDeuses.TAMANHO_MAXIMO_NOME || RegistroDeuses.gerarId(limpo).isEmpty()) {
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.deus.nome_invalido").withStyle(ChatFormatting.RED));
        } else if (RegistroDeuses.criar(limpo, cor & 0xFFFFFF, badge, corParticula, pronomes, poderes) == null) {
            jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.deus.deus_ja_existe", limpo).withStyle(ChatFormatting.RED));
        }
        enviarLista(jogador);
    }

    public static void editar(ServerPlayer jogador, String id, String nome, int cor, String badge, int corParticula, String pronomes, int poderes) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        TipoDeus deus = TipoDeus.fromId(id);
        String limpo = nome == null ? "" : nome.replaceAll("\\s+", "");
        if (deus == TipoDeus.NONE) {
            enviarLista(jogador);
            return;
        }
        if (limpo.isEmpty() || limpo.length() > RegistroDeuses.TAMANHO_MAXIMO_NOME || RegistroDeuses.gerarId(limpo).isEmpty()) {
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.deus.nome_invalido").withStyle(ChatFormatting.RED));
            enviarLista(jogador);
            return;
        }
        for (TipoDeus outro : TipoDeus.values()) {
            if (outro != deus && outro != TipoDeus.NONE && outro.displayName.equalsIgnoreCase(limpo)) {
                jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.deus.deus_ja_existe", limpo).withStyle(ChatFormatting.RED));
                enviarLista(jogador);
                return;
            }
        }
        String antigo = deus.displayName;
        String badgeAntiga = deus.badge;
        String pronomesAntigos = deus.pronomes;
        RegistroDeuses.editar(deus.id, limpo, cor, badge, corParticula, pronomes, poderes);
        var servidor = jogador.getServer();
        if (servidor != null) {
            DadosApelido apelidos = DadosApelido.get(servidor);
            for (ServerPlayer outro : servidor.getPlayerList().getPlayers()) {
                if (DadosDeus.get(outro.serverLevel()).getDeus(outro.getUUID()) != deus) continue;
                outro.setInvulnerable(deus.tem(TipoDeus.INVULNERAVEL));
                AtributosDeus.aplicar(outro, deus, false);
                if (!deus.tem(TipoDeus.PARTICULA) && FormaDivina.estaAtiva(outro)) FormaDivina.desativar(outro);
                if (badgeAntiga.equals(PerfilJogador.insignia().substituicao(servidor, outro.getUUID()))) {
                    PerfilJogador.insignia().definirSubstituicao(servidor, outro.getUUID(), deus.badge);
                }
                if (apelidos.getPronomes(outro.getUUID()).equals(pronomesAntigos)) {
                    apelidos.setPronomes(outro.getUUID(), deus.pronomes);
                    apelidos.syncToAll(servidor, outro.getUUID());
                    ComandoApelido.refreshTabList(outro);
                }
                DadosApelido.EntradaApelido atual = apelidos.getNickname(outro.getUUID());
                if (atual != null && atual.nick().equals(antigo)) ajustarIdentidade(outro, deus, deus);
            }
        }
        enviarLista(jogador);
    }

    public static void excluir(ServerPlayer jogador, String id) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        TipoDeus deus = TipoDeus.fromId(id);
        if (deus == TipoDeus.NONE) {
            enviarLista(jogador);
            return;
        }
        var servidor = jogador.getServer();
        if (servidor != null) {
            for (ServerPlayer outro : servidor.getPlayerList().getPlayers()) {
                if (DadosDeus.get(outro.serverLevel()).getDeus(outro.getUUID()) != deus) continue;
                remover(outro);
                ajustarIdentidade(outro, deus, null);
                net.aelysium.aelysiummod.sistemas.rp.skin.GerenciadorSkins.sincronizar(outro);
            }
        }
        net.aelysium.aelysiummod.sistemas.rp.skin.RegistroSkins.remover(net.aelysium.aelysiummod.sistemas.rp.skin.RegistroSkins.chave(net.aelysium.aelysiummod.sistemas.rp.skin.RegistroSkins.TIPO_DEUS, deus.id));
        net.aelysium.aelysiummod.sistemas.rp.personagem.AtributosPersonagem.limpar(net.aelysium.aelysiummod.sistemas.rp.personagem.AtributosPersonagem.chaveDeus(deus.id));
        RegistroDeuses.excluir(deus.id);
        enviarLista(jogador);
    }

    public static void enviarLista(ServerPlayer jogador) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        List<PacoteListaDeuses.Entrada> entradas = new ArrayList<>();
        for (TipoDeus deus : TipoDeus.values()) {
            if (deus != TipoDeus.NONE) entradas.add(new PacoteListaDeuses.Entrada(deus.id, deus.displayName, deus.color, deus.personalizado, deus.badge, deus.corParticula, deus.pronomes, deus.poderes));
        }
        String atual = DadosDeus.get(jogador.serverLevel()).getDeus(jogador.getUUID()).id;
        PacketDistributor.sendToPlayer(jogador, new PacoteListaDeuses(entradas, atual));
    }
}
