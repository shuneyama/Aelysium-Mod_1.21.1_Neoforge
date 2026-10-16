package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.ConstantesMaozinha;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimacaoBracoMaozinha {

    private static class Estado {
        boolean ativo;
        boolean esquerdo;
        boolean temMira;
        float inicioTransicao;
        float pesoInicial;
        float peso;
        float miraX;
        float miraY;
        float ultimoTempo;
    }

    private static final Map<UUID, Estado> estados = new HashMap<>();

    private static boolean emInterface = false;

    public static void definirEmInterface(boolean valor) {
        emInterface = valor;
    }

    public static void aplicar(PlayerModel<?> modelo, Player jogador, float parcial, float tempo, Vec3 destino, boolean esquerdo) {
        Estado estado = estados.get(jogador.getUUID());
        if (emInterface) {
            if (estado != null) posar(modelo, estado);
            return;
        }
        boolean ativo = destino != null;
        if (estado == null) {
            if (!ativo) return;
            estado = new Estado();
            estado.ultimoTempo = tempo;
            estados.put(jogador.getUUID(), estado);
        }

        if (ativo && estado.ativo && estado.esquerdo != esquerdo) {
            estado.temMira = false;
            estado.peso = 0.0F;
            estado.ativo = false;
        }
        if (ativo) estado.esquerdo = esquerdo;
        if (ativo != estado.ativo) {
            estado.ativo = ativo;
            estado.inicioTransicao = tempo;
            estado.pesoInicial = estado.peso;
        }

        float passo = Mth.clamp(tempo - estado.ultimoTempo, 0.0F, 5.0F);
        estado.ultimoTempo = tempo;

        if (ativo) {
            mirar(estado, jogador, parcial, destino, passo);
            float progresso = Mth.clamp((tempo - estado.inicioTransicao) / ConstantesMaozinha.DURACAO_SUBIDA_BRACO_TICKS, 0.0F, 1.0F);
            estado.peso = estado.pesoInicial + (1.0F - estado.pesoInicial) * quique(progresso);
        } else {
            float progresso = Mth.clamp((tempo - estado.inicioTransicao) / ConstantesMaozinha.DURACAO_DESCIDA_BRACO_TICKS, 0.0F, 1.0F);
            estado.peso = estado.pesoInicial * (1.0F - suave(progresso));
            if (progresso >= 1.0F) {
                estados.remove(jogador.getUUID());
                return;
            }
        }

        posar(modelo, estado);
    }

    private static void posar(PlayerModel<?> modelo, Estado estado) {
        ModelPart braco = estado.esquerdo ? modelo.leftArm : modelo.rightArm;
        ModelPart manga = estado.esquerdo ? modelo.leftSleeve : modelo.rightSleeve;
        braco.xRot = braco.xRot + (estado.miraX - braco.xRot) * estado.peso;
        braco.yRot = braco.yRot + Mth.wrapDegrees((estado.miraY - braco.yRot) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD * estado.peso;
        braco.zRot = braco.zRot * (1.0F - Mth.clamp(estado.peso, 0.0F, 1.0F));
        manga.copyFrom(braco);
    }

    private static void mirar(Estado estado, Player jogador, float parcial, Vec3 destino, float passo) {
        Vec3 ombro = GeometriaMaozinha.ombro(jogador, parcial, estado.esquerdo);
        Vec3 direcao = destino.subtract(ombro);
        if (direcao.lengthSqr() < 1.0E-6) return;

        float[] angulos = GeometriaMaozinha.angulos(direcao.normalize(), GeometriaMaozinha.corpo(jogador, parcial));
        if (!estado.temMira) {
            estado.miraX = angulos[0];
            estado.miraY = angulos[1];
            estado.temMira = true;
            return;
        }

        float fator = (float) (1.0 - Math.pow(1.0 - ConstantesMaozinha.SUAVIZACAO_MIRA_BRACO_POR_TICK, passo));
        estado.miraX += (angulos[0] - estado.miraX) * fator;
        estado.miraY += Mth.wrapDegrees((angulos[1] - estado.miraY) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD * fator;
    }

    private static float quique(float progresso) {
        float c1 = ConstantesMaozinha.QUIQUE_BRACO;
        float c3 = c1 + 1.0F;
        float t = progresso - 1.0F;
        return 1.0F + c3 * t * t * t + c1 * t * t;
    }

    private static float suave(float progresso) {
        float t = 1.0F - progresso;
        return 1.0F - t * t * t;
    }

    public static void limpar() {
        estados.clear();
    }
}
