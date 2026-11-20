package net.aelysium.aelysiummod.sistemas.rp.raca.cliente;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

public final class PoseSentada {
    private static final int RAIZ = 0;
    private static final int CINTURA = 1;
    private static final int CORPO = 2;
    private static final int CABECA = 3;
    private static final int BRACO_D = 4;
    private static final int BRACO_E = 5;
    private static final int PERNA_D = 6;
    private static final int PERNA_E = 7;

    private static final int ROT_X = 0;
    private static final int ROT_Y = 1;
    private static final int ROT_Z = 2;
    private static final int POS_X = 3;
    private static final int POS_Y = 4;
    private static final int POS_Z = 5;

    private static final double[] PES = {0.0, 24.0, 0.0};
    private static final double[] QUADRIL = {0.0, 12.0, 0.0};
    private static final double CICLO = 15.0;

    private PoseSentada() {
    }

    public static void aplicar(PlayerModel<?> modelo, double segundos, double guinada, double arfagem) {
        double[][] pose = amostrar(segundos);
        pose[CABECA][ROT_Y] += guinada;
        pose[CABECA][ROT_X] += arfagem;
        ModelPart[] partes = {modelo.head, modelo.body, modelo.rightArm, modelo.leftArm, modelo.rightLeg, modelo.leftLeg};
        for (ModelPart parte : partes) {
            parte.resetPose();
        }

        double[][] raiz = osso(pose, RAIZ, PES);
        double[][] cintura = vezes(raiz, osso(pose, CINTURA, QUADRIL));
        ModelPart corpo = modelo.body;
        double[][] superior = vezes(cintura, osso(pose, CORPO, new double[]{corpo.x, corpo.y, corpo.z}));
        colocar(superior, corpo, 0, 0, 0, 0, 0, 0);
        filho(superior, pose, CABECA, modelo.head);
        filho(superior, pose, BRACO_D, modelo.rightArm);
        filho(superior, pose, BRACO_E, modelo.leftArm);
        filho(raiz, pose, PERNA_D, modelo.rightLeg);
        filho(raiz, pose, PERNA_E, modelo.leftLeg);

        modelo.hat.copyFrom(modelo.head);
        modelo.jacket.copyFrom(modelo.body);
        modelo.leftSleeve.copyFrom(modelo.leftArm);
        modelo.rightSleeve.copyFrom(modelo.rightArm);
        modelo.leftPants.copyFrom(modelo.leftLeg);
        modelo.rightPants.copyFrom(modelo.rightLeg);
    }

    private static double[][] amostrar(double s) {
        double[][] p = new double[8][6];
        double tras = inclinarParaTras(fracao(s / CICLO) * CICLO);
        double respiro = Math.sin(Math.PI * 2 * s / 3.8);
        double balanco = 0.5 - 0.5 * Math.cos(Math.PI * 2 * s / 11.0);
        double chute = 0.5 * Math.sin(Math.PI * 2 * s / 1.5);
        double alcance = misturar(3.0, 9.0, tras) * balanco * balanco;
        double chuteD = alcance * (0.5 + chute);
        double chuteE = alcance * (0.5 - chute);
        double olhar = (0.6 + 0.4 * tras) * (20.0 * Math.sin(Math.PI * 2 * s / 9.3) + 9.0 * Math.sin(Math.PI * 2 * s / 4.7 + 1.3));
        double inclinacao = misturar(9.0, -12.0, tras) + 0.8 * respiro;
        double abrir = 10.0 * Math.sin(Math.PI * tras);

        p[CINTURA][ROT_X] = inclinacao;
        p[CORPO][ROT_X] = 1.2 * respiro;
        p[CORPO][ROT_Y] = 0.2 * olhar;
        p[CABECA][ROT_X] = -0.75 * inclinacao + 4.0 * Math.sin(Math.PI * 2 * s / 6.1);
        p[CABECA][ROT_Y] = 0.8 * olhar;
        p[CABECA][ROT_Z] = 3.0 * Math.sin(Math.PI * 2 * s / 7.7);
        p[BRACO_D][ROT_X] = misturar(-40.0, 20.0, tras);
        p[BRACO_D][ROT_Z] = misturar(-7.0, 2.0, tras) + abrir;
        p[BRACO_D][POS_Y] = tras;
        p[BRACO_E][ROT_X] = misturar(-40.0, 20.0, tras);
        p[BRACO_E][ROT_Z] = misturar(7.0, -2.0, tras) - abrir;
        p[BRACO_E][POS_Y] = tras;
        p[PERNA_D][ROT_X] = -80.0 + chuteD;
        p[PERNA_D][ROT_Y] = 12.0;
        p[PERNA_D][ROT_Z] = 2.0;
        p[PERNA_E][ROT_X] = -80.0 + chuteE;
        p[PERNA_E][ROT_Y] = -12.0;
        p[PERNA_E][ROT_Z] = -2.0;
        return p;
    }

    private static double inclinarParaTras(double s) {
        if (s < 6.0) return 0.0;
        if (s < 7.5) return suavizar((s - 6.0) / 1.5);
        return s < 13.5 ? 1.0 : 1.0 - suavizar((s - 13.5) / 1.5);
    }

    private static double suavizar(double x) {
        return x * x * x * (x * (x * 6.0 - 15.0) + 10.0);
    }

    private static double misturar(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double fracao(double v) {
        return v - Math.floor(v);
    }

    private static void filho(double[][] pai, double[][] pose, int osso, ModelPart parte) {
        colocar(pai, parte,
                (float) pose[osso][POS_X], (float) pose[osso][POS_Y], (float) pose[osso][POS_Z],
                (float) Math.toRadians(pose[osso][ROT_X]), (float) Math.toRadians(pose[osso][ROT_Y]), (float) Math.toRadians(pose[osso][ROT_Z]));
    }

    private static void colocar(double[][] pai, ModelPart parte, float dx, float dy, float dz, float drx, float dry, float drz) {
        double[][] local = {
                rotacao(parte.xRot + drx, parte.yRot + dry, parte.zRot + drz),
                {parte.x + dx, parte.y + dy, parte.z + dz}
        };
        double[][] r = vezes(pai, local);
        double[] m = r[0];
        parte.x = (float) r[1][0];
        parte.y = (float) r[1][1];
        parte.z = (float) r[1][2];
        parte.xRot = (float) Math.atan2(m[7], m[8]);
        parte.yRot = (float) Math.asin(Math.max(-1.0, Math.min(1.0, -m[6])));
        parte.zRot = (float) Math.atan2(m[3], m[0]);
    }

    private static double[][] osso(double[][] pose, int osso, double[] pivo) {
        double[] r = rotacao(Math.toRadians(pose[osso][ROT_X]), Math.toRadians(pose[osso][ROT_Y]), Math.toRadians(pose[osso][ROT_Z]));
        double[] girado = aplicar(r, pivo);
        return new double[][]{r, {
                pose[osso][POS_X] + pivo[0] - girado[0],
                pose[osso][POS_Y] + pivo[1] - girado[1],
                pose[osso][POS_Z] + pivo[2] - girado[2]
        }};
    }

    private static double[] rotacao(double x, double y, double z) {
        double ca = Math.cos(x), sa = Math.sin(x);
        double cb = Math.cos(y), sb = Math.sin(y);
        double cc = Math.cos(z), sc = Math.sin(z);
        return new double[]{
                cc * cb, cc * sb * sa - sc * ca, cc * sb * ca + sc * sa,
                sc * cb, sc * sb * sa + cc * ca, sc * sb * ca - cc * sa,
                -sb, cb * sa, cb * ca
        };
    }

    private static double[] aplicar(double[] r, double[] v) {
        return new double[]{
                r[0] * v[0] + r[1] * v[1] + r[2] * v[2],
                r[3] * v[0] + r[4] * v[1] + r[5] * v[2],
                r[6] * v[0] + r[7] * v[1] + r[8] * v[2]
        };
    }

    private static double[][] vezes(double[][] a, double[][] b) {
        double[] produto = new double[9];
        for (int l = 0; l < 3; l++) {
            for (int c = 0; c < 3; c++) {
                produto[l * 3 + c] = a[0][l * 3] * b[0][c] + a[0][l * 3 + 1] * b[0][3 + c] + a[0][l * 3 + 2] * b[0][6 + c];
            }
        }
        double[] movido = aplicar(a[0], b[1]);
        return new double[][]{produto, {movido[0] + a[1][0], movido[1] + a[1][1], movido[2] + a[1][2]}};
    }
}
