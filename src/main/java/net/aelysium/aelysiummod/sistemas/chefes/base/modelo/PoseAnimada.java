package net.aelysium.aelysiummod.sistemas.chefes.base.modelo;

import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao.AnimacaoBedrock;
import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao.CanalAnimacao;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

public class PoseAnimada {
    private static final double TEMPO_RETORNO = 5;

    private final Map<String, Instantaneo> instantaneos = new HashMap<>();

    public void aplicar(ModeloBedrock modelo, AnimacaoBedrock animacao, double tick, double agora) {
        for (OssoModelo osso : modelo.todos()) {
            Instantaneo estado = instantaneos.computeIfAbsent(osso.getName(), nome -> new Instantaneo(osso));
            AnimacaoBedrock.OssoAnimado canais = animacao != null ? animacao.ossos().get(osso.getName()) : null;
            CanalAnimacao rotacao = canais != null ? canais.rotacao() : null;
            CanalAnimacao posicao = canais != null ? canais.posicao() : null;
            CanalAnimacao escala = canais != null ? canais.escala() : null;

            if (rotacao != null) {
                osso.setRot(rotacao.x(tick) + osso.getRotInicialX(), rotacao.y(tick) + osso.getRotInicialY(), rotacao.z(tick) + osso.getRotInicialZ());
                estado.rot(osso.getRotX(), osso.getRotY(), osso.getRotZ());
                estado.rotAnimando = true;
            } else {
                if (estado.rotAnimando) {
                    estado.rotAnimando = false;
                    estado.rotParou = agora;
                }
                float fracao = fracao(agora, estado.rotParou);
                osso.setRot(Mth.lerp(fracao, estado.rx, osso.getRotInicialX()), Mth.lerp(fracao, estado.ry, osso.getRotInicialY()), Mth.lerp(fracao, estado.rz, osso.getRotInicialZ()));
                if (fracao >= 1) estado.rot(osso.getRotX(), osso.getRotY(), osso.getRotZ());
            }

            if (posicao != null) {
                osso.setPos(posicao.x(tick), posicao.y(tick), posicao.z(tick));
                estado.pos(osso.getPosX(), osso.getPosY(), osso.getPosZ());
                estado.posAnimando = true;
            } else {
                if (estado.posAnimando) {
                    estado.posAnimando = false;
                    estado.posParou = agora;
                }
                float fracao = fracao(agora, estado.posParou);
                osso.setPos(Mth.lerp(fracao, estado.px, 0), Mth.lerp(fracao, estado.py, 0), Mth.lerp(fracao, estado.pz, 0));
                if (fracao >= 1) estado.pos(osso.getPosX(), osso.getPosY(), osso.getPosZ());
            }

            if (escala != null) {
                osso.setEscala(escala.x(tick), escala.y(tick), escala.z(tick));
                estado.escala(osso.getEscalaX(), osso.getEscalaY(), osso.getEscalaZ());
                estado.escalaAnimando = true;
            } else {
                if (estado.escalaAnimando) {
                    estado.escalaAnimando = false;
                    estado.escalaParou = agora;
                }
                float fracao = fracao(agora, estado.escalaParou);
                osso.setEscala(Mth.lerp(fracao, estado.ex, 1), Mth.lerp(fracao, estado.ey, 1), Mth.lerp(fracao, estado.ez, 1));
                if (fracao >= 1) estado.escala(osso.getEscalaX(), osso.getEscalaY(), osso.getEscalaZ());
            }
        }
    }

    private static float fracao(double agora, double parou) {
        return (float) Math.min((agora - parou) / TEMPO_RETORNO, 1);
    }

    private static final class Instantaneo {
        private float rx;
        private float ry;
        private float rz;
        private float px;
        private float py;
        private float pz;
        private float ex = 1;
        private float ey = 1;
        private float ez = 1;
        private boolean rotAnimando;
        private boolean posAnimando;
        private boolean escalaAnimando;
        private double rotParou;
        private double posParou;
        private double escalaParou;

        private Instantaneo(OssoModelo osso) {
            rot(osso.getRotInicialX(), osso.getRotInicialY(), osso.getRotInicialZ());
        }

        private void rot(float x, float y, float z) {
            rx = x;
            ry = y;
            rz = z;
        }

        private void pos(float x, float y, float z) {
            px = x;
            py = y;
            pz = z;
        }

        private void escala(float x, float y, float z) {
            ex = x;
            ey = y;
            ez = z;
        }
    }
}
