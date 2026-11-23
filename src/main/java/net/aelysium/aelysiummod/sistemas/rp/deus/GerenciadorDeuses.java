package net.aelysium.aelysiummod.sistemas.rp.deus;

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
        jogador.setInvulnerable(true);
        DadosDeus.get(jogador.serverLevel()).setDeus(jogador.getUUID(), deus);
        AtributosDeus.aplicar(jogador);
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
        if (deus == TipoDeus.NONE) {
            remover(jogador);
        } else {
            definir(jogador, deus);
        }
        enviarLista(jogador);
    }

    public static void criar(ServerPlayer jogador, String nome, int cor) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        String limpo = nome == null ? "" : nome.trim();
        if (limpo.isEmpty() || limpo.length() > RegistroDeuses.TAMANHO_MAXIMO_NOME || RegistroDeuses.gerarId(limpo).isEmpty()) {
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.deus.nome_invalido").withStyle(ChatFormatting.RED));
        } else if (RegistroDeuses.criar(limpo, cor & 0xFFFFFF) == null) {
            jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.deus.deus_ja_existe", limpo).withStyle(ChatFormatting.RED));
        }
        enviarLista(jogador);
    }

    public static void enviarLista(ServerPlayer jogador) {
        if (!jogador.hasPermissions(PERMISSAO)) return;
        List<PacoteListaDeuses.Entrada> entradas = new ArrayList<>();
        for (TipoDeus deus : TipoDeus.values()) {
            if (deus != TipoDeus.NONE) entradas.add(new PacoteListaDeuses.Entrada(deus.id, deus.displayName, deus.color));
        }
        String atual = DadosDeus.get(jogador.serverLevel()).getDeus(jogador.getUUID()).id;
        PacketDistributor.sendToPlayer(jogador, new PacoteListaDeuses(entradas, atual));
    }
}
