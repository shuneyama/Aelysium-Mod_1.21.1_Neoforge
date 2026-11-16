package net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide.Cuboide;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide.DadosCuboide;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class CuboideDobravel implements Cuboide, Dobravel, IteravelPosicoes {

    private final FaceDobravel[] faces;
    private final PosicaoMemorizada[] posicoes;
    private final float centroX;
    private final float centroY;
    private final float centroZ;
    private final Direction direcao;
    private final Plano planoBase;
    private final Plano planoOposto;
    private final float alturaTotal;

    private float dobra;
    private float eixoDobra;

    private CuboideDobravel(FaceDobravel[] faces, PosicaoMemorizada[] posicoes, float centroX, float centroY, float centroZ, Direction direcao, Plano planoBase, Plano planoOposto, float alturaTotal) {
        this.faces = faces;
        this.posicoes = posicoes;
        this.centroX = centroX;
        this.centroY = centroY;
        this.centroZ = centroZ;
        this.direcao = direcao;
        this.planoBase = planoBase;
        this.planoOposto = planoOposto;
        this.alturaTotal = alturaTotal;
        this.dobrar(0, 0);
    }

    public void dobrar(float eixo, float valor) {
        this.dobra = valor;
        this.eixoDobra = eixo;
        this.aplicarDobra(eixo, valor, this);
    }

    public float obterDobra() {
        return dobra;
    }

    public float obterEixoDobra() {
        return eixoDobra;
    }

    @Override
    public Direction direcaoDobra() {
        return direcao;
    }

    @Override
    public float centroDobraX() {
        return centroX;
    }

    @Override
    public float centroDobraY() {
        return centroY;
    }

    @Override
    public float centroDobraZ() {
        return centroZ;
    }

    @Override
    public Plano planoBase() {
        return planoBase;
    }

    @Override
    public Plano planoOposto() {
        return planoOposto;
    }

    @Override
    public float alturaDobra() {
        return alturaTotal;
    }

    @Override
    public void percorrerPosicoes(Consumer<PosicaoComOrigem> consumidor) {
        for (PosicaoMemorizada posicao : posicoes) {
            consumidor.accept(posicao);
        }
    }

    @Override
    public void renderizar(PoseStack.Pose pose, VertexConsumer consumidor, int luz, int sobreposicao, int cor) {
        for (FaceDobravel face : faces) {
            face.renderizar(pose, consumidor, luz, sobreposicao, cor);
        }
    }

    @Override
    public void copiarEstado(Cuboide outro) {
        if (outro instanceof CuboideDobravel dobravel) {
            this.dobrar(dobravel.eixoDobra, dobravel.dobra);
        }
    }

    public static CuboideDobravel criar(DadosCuboide dados, Direction direcao) {
        List<FaceDobravel> faces = new ArrayList<>();
        Map<Vector3f, PosicaoMemorizada> posicoes = new HashMap<>();

        float minX = dados.x;
        float minY = dados.y;
        float minZ = dados.z;
        float maxX = dados.x + dados.tamanhoX;
        float maxY = dados.y + dados.tamanhoY;
        float maxZ = dados.z + dados.tamanhoZ;
        float externoMinX = minX - dados.extraX;
        float externoMinY = minY - dados.extraY;
        float externoMinZ = minZ - dados.extraZ;
        float externoMaxX = maxX + dados.extraX;
        float externoMaxY = maxY + dados.extraY;
        float externoMaxZ = maxZ + dados.extraZ;
        if (dados.espelhado) {
            float temporario = externoMinX;
            externoMinX = externoMaxX;
            externoMaxX = temporario;
        }

        Vector3f canto1 = new Vector3f(externoMinX, externoMinY, externoMinZ);
        Vector3f canto2 = new Vector3f(externoMaxX, externoMinY, externoMinZ);
        Vector3f canto3 = new Vector3f(externoMaxX, externoMaxY, externoMinZ);
        Vector3f canto4 = new Vector3f(externoMinX, externoMaxY, externoMinZ);
        Vector3f canto5 = new Vector3f(externoMinX, externoMinY, externoMaxZ);
        Vector3f canto6 = new Vector3f(externoMaxX, externoMinY, externoMaxZ);
        Vector3f canto7 = new Vector3f(externoMaxX, externoMaxY, externoMaxZ);
        Vector3f canto8 = new Vector3f(externoMinX, externoMaxY, externoMaxZ);

        int u0 = dados.u;
        int u1 = (int) (dados.u + dados.tamanhoZ);
        int u2 = (int) (dados.u + dados.tamanhoZ + dados.tamanhoX);
        int u3 = (int) (dados.u + dados.tamanhoZ + dados.tamanhoX + dados.tamanhoX);
        int u4 = (int) (dados.u + dados.tamanhoZ + dados.tamanhoX + dados.tamanhoZ);
        int u5 = (int) (dados.u + dados.tamanhoZ + dados.tamanhoX + dados.tamanhoZ + dados.tamanhoX);
        int v0 = dados.v;
        int v1 = (int) (dados.v + dados.tamanhoZ);
        int v2 = (int) (dados.v + dados.tamanhoZ + dados.tamanhoY);

        criarFaces(faces, posicoes, canto6, canto5, canto2, u1, v0, u2, v1, dados);
        criarFaces(faces, posicoes, canto3, canto4, canto7, u2, v1, u3, v0, dados);
        criarFaces(faces, posicoes, canto1, canto5, canto4, u0, v1, u1, v2, dados);
        criarFaces(faces, posicoes, canto2, canto1, canto3, u1, v1, u2, v2, dados);
        criarFaces(faces, posicoes, canto6, canto2, canto7, u2, v1, u4, v2, dados);
        criarFaces(faces, posicoes, canto5, canto6, canto8, u4, v1, u5, v2, dados);

        Vector3f pivo = new Vector3f();
        if (dados.pivo >= 0) {
            float tamanho = direcao.step().mul(maxX - minX, maxY - minY, maxZ - minZ).length();
            if (dados.pivo <= tamanho) {
                pivo = direcao.step().mul(tamanho - (dados.pivo * 2));
                canto7.sub(pivo);
            }
        }

        Plano planoA = new Plano(direcao.step(), canto7);
        Plano planoB = new Plano(direcao.step(), canto1);
        boolean positiva = direcao == Direction.UP || direcao == Direction.SOUTH || direcao == Direction.EAST;
        float alturaTotal = direcao.step().dot(canto7) - direcao.step().dot(canto1);
        float centroX = (dados.tamanhoX + dados.x + dados.x - pivo.x()) / 2;
        float centroY = (dados.tamanhoY + dados.y + dados.y - pivo.y()) / 2;
        float centroZ = (dados.tamanhoZ + dados.z + dados.z - pivo.z()) / 2;

        return new CuboideDobravel(
                faces.toArray(new FaceDobravel[0]),
                posicoes.values().toArray(new PosicaoMemorizada[0]),
                centroX, centroY, centroZ,
                direcao,
                positiva ? planoA : planoB,
                positiva ? planoB : planoA,
                alturaTotal);
    }

    private static void criarFaces(List<FaceDobravel> faces, Map<Vector3f, PosicaoMemorizada> posicoes, Vector3f origem, Vector3f extremoU, Vector3f extremoV, int u1, int v1, int u2, int v2, DadosCuboide dados) {
        int passoU = u2 < u1 ? 1 : -1;
        int passoV = v1 < v2 ? 1 : -1;
        for (int localU = u2; localU != u1; localU += passoU) {
            for (int localV = v1; localV != v2; localV += passoV) {
                int proximoU = localU + passoU;
                int proximoV = localV + passoV;
                PosicaoMemorizada p0 = obterOuCriar(posicoes, interpolar(origem, extremoU, extremoV, u2, v1, u1, v2, proximoU, localV));
                PosicaoMemorizada p1 = obterOuCriar(posicoes, interpolar(origem, extremoU, extremoV, u2, v1, u1, v2, proximoU, proximoV));
                PosicaoMemorizada p2 = obterOuCriar(posicoes, interpolar(origem, extremoU, extremoV, u2, v1, u1, v2, localU, proximoV));
                PosicaoMemorizada p3 = obterOuCriar(posicoes, interpolar(origem, extremoU, extremoV, u2, v1, u1, v2, localU, localV));
                faces.add(new FaceDobravel(new PosicaoMemorizada[]{p3, p0, p1, p2}, proximoU, localV, localU, proximoV, dados.larguraTextura, dados.alturaTextura, dados.espelhado));
            }
        }
    }

    private static Vector3f interpolar(Vector3f origem, Vector3f extremoU, Vector3f extremoV, int u1, int v1, int u2, int v2, int u, int v) {
        Vector3f deslocamentoU = new Vector3f(extremoU).sub(origem).mul(((float) u - u1) / (u2 - u1));
        Vector3f deslocamentoV = new Vector3f(extremoV).sub(origem).mul(((float) v - v1) / (v2 - v1));
        return new Vector3f(origem).add(deslocamentoU).add(deslocamentoV);
    }

    private static PosicaoMemorizada obterOuCriar(Map<Vector3f, PosicaoMemorizada> posicoes, Vector3f posicao) {
        return posicoes.computeIfAbsent(posicao, PosicaoMemorizada::new);
    }
}
