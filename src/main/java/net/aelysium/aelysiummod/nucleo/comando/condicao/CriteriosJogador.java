package net.aelysium.aelysiummod.nucleo.comando.condicao;

import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.aelysium.aelysiummod.sistemas.admin.vip.DadosVipJogadores;
import net.aelysium.aelysiummod.sistemas.rp.escola.DadosEscola;
import net.aelysium.aelysiummod.sistemas.rp.escola.ElementoMagico;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class CriteriosJogador {

    private CriteriosJogador() {
    }

    public static boolean raca(Entity entidade, TipoRaca raca) {
        return entidade instanceof ServerPlayer jogador && PerfilJogador.raca().raca(jogador.getUUID()) == raca;
    }

    public static boolean deus(Entity entidade, TipoDeus deus) {
        return entidade instanceof ServerPlayer jogador && PerfilJogador.deus().deus(jogador) == deus;
    }

    public static boolean live(Entity entidade) {
        return entidade instanceof ServerPlayer jogador && PerfilJogador.vidas().emLive(jogador.getServer(), jogador.getUUID());
    }

    public static boolean silenciado(Entity entidade) {
        return entidade instanceof ServerPlayer jogador && PerfilJogador.habilidades().estaMudo(jogador.getUUID());
    }

    public static boolean escola(Entity entidade, ElementoMagico elemento) {
        return entidade instanceof ServerPlayer jogador && elemento != null
                && DadosEscola.get(jogador.getServer()).possui(jogador.getUUID(), elemento);
    }

    public static boolean vip(Entity entidade, String vip) {
        return entidade instanceof ServerPlayer jogador && DadosVipJogadores.get(jogador.getServer()).possui(jogador.getUUID(), vip);
    }

    public static boolean regiao(Entity entidade, String nome) {
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiao(nome);
        return regiao != null && regiao.contemPosicao(entidade.blockPosition(), entidade.level().dimension().location());
    }
}
