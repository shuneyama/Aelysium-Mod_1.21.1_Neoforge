package net.aelysium.aelysiummod.sistemas.chefes.base.modelo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CarregadorModelo {
    private static final Direction[] DIRECOES = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN};

    private CarregadorModelo() {
    }

    public static ModeloBedrock carregar(JsonObject raiz) {
        JsonObject geometria = raiz.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        JsonObject descricao = geometria.has("description") ? geometria.getAsJsonObject("description") : new JsonObject();
        float larguraTextura = descricao.has("texture_width") ? descricao.get("texture_width").getAsFloat() : 64f;
        float alturaTextura = descricao.has("texture_height") ? descricao.get("texture_height").getAsFloat() : 64f;
        JsonArray ossosJson = geometria.has("bones") ? geometria.getAsJsonArray("bones") : new JsonArray();

        Map<String, OssoModelo> ossos = new LinkedHashMap<>();
        Map<String, String> pais = new LinkedHashMap<>();

        for (JsonElement elemento : ossosJson) {
            JsonObject osso = elemento.getAsJsonObject();
            String nome = osso.get("name").getAsString();
            double[] pivo = vetor(osso, "pivot");
            double[] rotacao = vetor(osso, "rotation");
            boolean oculto = osso.has("neverRender") && osso.get("neverRender").getAsBoolean();
            OssoModelo novo = new OssoModelo(nome,
                    (float) -pivo[0], (float) pivo[1], (float) pivo[2],
                    (float) Math.toRadians(-rotacao[0]), (float) Math.toRadians(-rotacao[1]), (float) Math.toRadians(rotacao[2]),
                    oculto);
            Double inflarOsso = osso.has("inflate") ? osso.get("inflate").getAsDouble() : null;

            if (osso.has("cubes")) {
                for (JsonElement cubo : osso.getAsJsonArray("cubes")) {
                    novo.getCubos().add(cubo(cubo.getAsJsonObject(), inflarOsso, larguraTextura, alturaTextura));
                }
            }

            ossos.put(nome, novo);
            if (osso.has("parent")) pais.put(nome, osso.get("parent").getAsString());
        }

        List<OssoModelo> raizes = new ArrayList<>();
        for (OssoModelo osso : ossos.values()) {
            OssoModelo pai = pais.containsKey(osso.getName()) ? ossos.get(pais.get(osso.getName())) : null;
            if (pai != null) pai.getFilhos().add(osso);
            else raizes.add(osso);
        }

        return new ModeloBedrock(List.copyOf(raizes), Map.copyOf(ossos));
    }

    private static CuboModelo cubo(JsonObject cubo, Double inflarOsso, float larguraTextura, float alturaTextura) {
        boolean espelhado = cubo.has("mirror") && cubo.get("mirror").getAsBoolean();
        double inflar = cubo.has("inflate") ? cubo.get("inflate").getAsDouble() / 16d : (inflarOsso == null ? 0 : inflarOsso / 16d);
        double[] tamanho = vetor(cubo, "size");
        double[] origem = vetor(cubo, "origin");
        double[] rotacao = vetor(cubo, "rotation");
        double[] pivo = vetor(cubo, "pivot");

        double ox = -(origem[0] + tamanho[0]) / 16d;
        double oy = origem[1] / 16d;
        double oz = origem[2] / 16d;
        double tx = tamanho[0] / 16d;
        double ty = tamanho[1] / 16d;
        double tz = tamanho[2] / 16d;

        VerticeModelo[] v = {
                vertice(ox - inflar, oy - inflar, oz - inflar),
                vertice(ox - inflar, oy - inflar, oz + tz + inflar),
                vertice(ox - inflar, oy + ty + inflar, oz - inflar),
                vertice(ox - inflar, oy + ty + inflar, oz + tz + inflar),
                vertice(ox + tx + inflar, oy + ty + inflar, oz - inflar),
                vertice(ox + tx + inflar, oy + ty + inflar, oz + tz + inflar),
                vertice(ox + tx + inflar, oy - inflar, oz - inflar),
                vertice(ox + tx + inflar, oy - inflar, oz + tz + inflar)
        };

        JsonElement uv = cubo.get("uv");
        QuadModelo[] quads = new QuadModelo[6];
        for (int i = 0; i < DIRECOES.length; i++) {
            quads[i] = quad(v, DIRECOES[i], uv, tamanho, espelhado, larguraTextura, alturaTextura);
        }

        return new CuboModelo(quads,
                (float) -pivo[0], (float) pivo[1], (float) pivo[2],
                (float) Math.toRadians(-rotacao[0]), (float) Math.toRadians(-rotacao[1]), (float) Math.toRadians(rotacao[2]),
                (float) tamanho[0], (float) tamanho[1], (float) tamanho[2]);
    }

    private static QuadModelo quad(VerticeModelo[] v, Direction direcao, JsonElement uv, double[] tamanho, boolean espelhado, float larguraTextura, float alturaTextura) {
        if (uv != null && uv.isJsonObject()) {
            String chave = direcao.getName();
            JsonObject faces = uv.getAsJsonObject();
            if (!faces.has(chave)) return null;
            JsonObject face = faces.getAsJsonObject(chave);
            double[] coordenadas = vetor2(face, "uv");
            double[] tamanhoUv = vetor2(face, "uv_size");
            int rotacaoUv = face.has("uv_rotation") ? (Math.abs(face.get("uv_rotation").getAsInt()) % 360) / 90 : 0;
            return montar(vertices(v, direcao, false, espelhado), (float) coordenadas[0], (float) coordenadas[1], (float) tamanhoUv[0], (float) tamanhoUv[1], rotacaoUv, larguraTextura, alturaTextura, espelhado, direcao);
        }

        double[] caixa = uv != null && uv.isJsonArray() ? vetor2(uv.getAsJsonArray()) : new double[2];
        double sx = Math.floor(tamanho[0]);
        double sy = Math.floor(tamanho[1]);
        double sz = Math.floor(tamanho[2]);
        double[][] dados = switch (direcao) {
            case WEST -> new double[][]{{caixa[0] + sz + sx, caixa[1] + sz}, {sz, sy}};
            case EAST -> new double[][]{{caixa[0], caixa[1] + sz}, {sz, sy}};
            case NORTH -> new double[][]{{caixa[0] + sz, caixa[1] + sz}, {sx, sy}};
            case SOUTH -> new double[][]{{caixa[0] + sz + sx + sz, caixa[1] + sz}, {sx, sy}};
            case UP -> new double[][]{{caixa[0] + sz, caixa[1]}, {sx, sz}};
            case DOWN -> new double[][]{{caixa[0] + sz + sx, caixa[1] + sz}, {sx, -sz}};
        };

        return montar(vertices(v, direcao, true, espelhado), (float) dados[0][0], (float) dados[0][1], (float) dados[1][0], (float) dados[1][1], 0, larguraTextura, alturaTextura, espelhado, direcao);
    }

    private static VerticeModelo[] vertices(VerticeModelo[] v, Direction direcao, boolean caixa, boolean espelhado) {
        VerticeModelo[] oeste = {v[3], v[2], v[0], v[1]};
        VerticeModelo[] leste = {v[4], v[5], v[7], v[6]};
        VerticeModelo[] cima = {v[3], v[5], v[4], v[2]};
        VerticeModelo[] baixo = {v[0], v[6], v[7], v[1]};
        return switch (direcao) {
            case WEST -> espelhado ? leste : oeste;
            case EAST -> espelhado ? oeste : leste;
            case NORTH -> new VerticeModelo[]{v[2], v[4], v[6], v[0]};
            case SOUTH -> new VerticeModelo[]{v[5], v[3], v[1], v[7]};
            case UP -> espelhado && !caixa ? baixo : cima;
            case DOWN -> espelhado && !caixa ? cima : baixo;
        };
    }

    private static QuadModelo montar(VerticeModelo[] vertices, float u, float v, float tamanhoU, float tamanhoV, int rotacaoUv, float larguraTextura, float alturaTextura, boolean espelhado, Direction direcao) {
        float uFinal = (u + tamanhoU) / larguraTextura;
        float vFinal = (v + tamanhoV) / alturaTextura;
        u /= larguraTextura;
        v /= alturaTextura;
        float nx = direcao.getStepX();
        float ny = direcao.getStepY();
        float nz = direcao.getStepZ();

        if (!espelhado) {
            float temporario = uFinal;
            uFinal = u;
            u = temporario;
        } else {
            nx = -nx;
        }

        float[] uvs = switch (rotacaoUv) {
            case 1 -> new float[]{uFinal, v, uFinal, vFinal, u, vFinal, u, v};
            case 2 -> new float[]{uFinal, vFinal, u, vFinal, u, v, uFinal, v};
            case 3 -> new float[]{u, vFinal, u, v, uFinal, v, uFinal, vFinal};
            default -> new float[]{u, v, uFinal, v, uFinal, vFinal, u, vFinal};
        };

        VerticeModelo[] resultado = new VerticeModelo[4];
        for (int i = 0; i < 4; i++) {
            resultado[i] = vertices[i].comUv(uvs[i * 2], uvs[i * 2 + 1]);
        }

        return new QuadModelo(resultado, nx, ny, nz);
    }

    private static VerticeModelo vertice(double x, double y, double z) {
        return new VerticeModelo((float) x, (float) y, (float) z, 0, 0);
    }

    private static double[] vetor(JsonObject objeto, String chave) {
        if (!objeto.has(chave)) return new double[3];
        JsonArray array = objeto.getAsJsonArray(chave);
        if (array.size() == 1) {
            double valor = array.get(0).getAsDouble();
            return new double[]{valor, valor, valor};
        }
        return new double[]{
                array.size() > 0 ? array.get(0).getAsDouble() : 0,
                array.size() > 1 ? array.get(1).getAsDouble() : 0,
                array.size() > 2 ? array.get(2).getAsDouble() : 0
        };
    }

    private static double[] vetor2(JsonObject objeto, String chave) {
        return objeto.has(chave) ? vetor2(objeto.getAsJsonArray(chave)) : new double[2];
    }

    private static double[] vetor2(JsonArray array) {
        return new double[]{
                array.size() > 0 ? array.get(0).getAsDouble() : 0,
                array.size() > 1 ? array.get(1).getAsDouble() : 0
        };
    }
}
