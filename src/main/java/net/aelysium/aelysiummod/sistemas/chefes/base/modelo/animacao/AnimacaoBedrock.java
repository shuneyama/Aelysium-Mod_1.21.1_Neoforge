package net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao;

import java.util.Map;

public record AnimacaoBedrock(String nome, double duracao, Map<String, OssoAnimado> ossos) {

    public record OssoAnimado(CanalAnimacao rotacao, CanalAnimacao posicao, CanalAnimacao escala) {
    }
}
