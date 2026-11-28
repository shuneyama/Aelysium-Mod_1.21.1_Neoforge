package net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CarregadorAnimacoes {

    private CarregadorAnimacoes() {
    }

    public static Map<String, AnimacaoBedrock> carregar(JsonObject raiz) {
        Map<String, AnimacaoBedrock> animacoes = new HashMap<>();
        JsonObject todas = raiz.has("animations") ? raiz.getAsJsonObject("animations") : new JsonObject();

        for (Map.Entry<String, JsonElement> entrada : todas.entrySet()) {
            JsonObject animacao = entrada.getValue().getAsJsonObject();
            Map<String, AnimacaoBedrock.OssoAnimado> ossos = new HashMap<>();
            JsonObject ossosJson = animacao.has("bones") ? animacao.getAsJsonObject("bones") : new JsonObject();
            double maior = 0;

            for (Map.Entry<String, JsonElement> osso : ossosJson.entrySet()) {
                JsonObject canais = osso.getValue().getAsJsonObject();
                CanalAnimacao rotacao = canal(canais.get("rotation"), true);
                CanalAnimacao posicao = canal(canais.get("position"), false);
                CanalAnimacao escala = canal(canais.get("scale"), false);
                ossos.put(osso.getKey(), new AnimacaoBedrock.OssoAnimado(rotacao, posicao, escala));
                for (CanalAnimacao c : new CanalAnimacao[]{rotacao, posicao, escala}) {
                    if (c != null) maior = Math.max(maior, c.ultimoTempo());
                }
            }

            double duracao = animacao.has("animation_length") ? animacao.get("animation_length").getAsDouble() * 20d : (maior == 0 ? Double.MAX_VALUE : maior);
            animacoes.put(entrada.getKey(), new AnimacaoBedrock(entrada.getKey(), duracao, Map.copyOf(ossos)));
        }

        return Map.copyOf(animacoes);
    }

    private record Entrada(double tempo, JsonElement valor) {
    }

    private static List<Entrada> entradas(JsonElement elemento) {
        List<Entrada> lista = new ArrayList<>();
        if (elemento instanceof JsonPrimitive primitivo) {
            JsonArray array = new JsonArray(3);
            array.add(primitivo);
            array.add(primitivo);
            array.add(primitivo);
            lista.add(new Entrada(0, array));
            return lista;
        }
        if (elemento instanceof JsonArray array) {
            lista.add(new Entrada(0, array));
            return lista;
        }
        if (!(elemento instanceof JsonObject objeto)) throw new JsonParseException("Quadro-chave invalido: " + elemento);
        if (objeto.has("vector")) {
            lista.add(new Entrada(0, objeto));
            return lista;
        }
        for (Map.Entry<String, JsonElement> entrada : objeto.entrySet()) {
            double tempo = tempo(entrada.getKey());
            if (entrada.getValue() instanceof JsonObject quadro && !quadro.has("vector")) {
                bedrock(tempo, quadro, lista);
                continue;
            }
            lista.add(new Entrada(tempo, entrada.getValue()));
        }
        return lista;
    }

    private static void bedrock(double tempo, JsonObject quadro, List<Entrada> lista) {
        boolean adicionou = false;
        if (quadro.has("pre")) {
            adicionou = true;
            lista.add(new Entrada(tempo == 0 ? tempo : tempo - 0.001d, extrair(quadro.get("pre"))));
        }
        if (quadro.has("post")) {
            JsonArray valores = extrair(quadro.get("post"));
            if (quadro.has("lerp_mode")) {
                JsonObject comCurva = new JsonObject();
                comCurva.add("vector", valores);
                comCurva.add("easing", quadro.get("lerp_mode"));
                lista.add(new Entrada(tempo, comCurva));
            } else {
                lista.add(new Entrada(tempo, valores));
            }
            return;
        }
        if (!adicionou) throw new JsonParseException("Quadro-chave invalido: " + quadro);
    }

    private static JsonArray extrair(JsonElement quadro) {
        if (quadro.isJsonArray()) return quadro.getAsJsonArray();
        JsonObject objeto = quadro.getAsJsonObject();
        if (objeto.has("vector")) return objeto.getAsJsonArray("vector");
        if (objeto.has("pre")) return objeto.getAsJsonArray("pre");
        return objeto.getAsJsonArray("post");
    }

    private static CanalAnimacao canal(JsonElement elemento, boolean rotacao) {
        if (elemento == null) return null;
        List<Entrada> lista = entradas(elemento);
        if (lista.isEmpty()) return null;

        List<QuadroChave> xs = new ArrayList<>();
        List<QuadroChave> ys = new ArrayList<>();
        List<QuadroChave> zs = new ArrayList<>();
        double[] anterior = null;
        double tempoAnterior = 0;

        for (Entrada entrada : lista) {
            JsonElement valor = entrada.valor();
            JsonArray vetor = valor instanceof JsonArray array ? array : valor.getAsJsonObject().getAsJsonArray("vector");
            double[] atual = {numero(vetor.get(0)), numero(vetor.get(1)), numero(vetor.get(2))};
            if (rotacao) {
                atual[0] = Math.toRadians(-atual[0]);
                atual[1] = Math.toRadians(-atual[1]);
                atual[2] = Math.toRadians(atual[2]);
            }
            JsonObject objeto = valor instanceof JsonObject o ? o : null;
            Suavizacao suavizacao = objeto != null && objeto.has("easing") && objeto.get("easing").isJsonPrimitive() && objeto.get("easing").getAsJsonPrimitive().isString()
                    ? Suavizacao.porNome(objeto.get("easing").getAsString())
                    : Suavizacao.LINEAR;
            Double argumento = objeto != null && objeto.has("easingArgs") && !objeto.getAsJsonArray("easingArgs").isEmpty()
                    ? objeto.getAsJsonArray("easingArgs").get(0).getAsDouble()
                    : null;
            double duracao = (entrada.tempo() - tempoAnterior) * 20;
            double[] inicio = anterior == null ? atual : anterior;

            xs.add(quadro(duracao, inicio[0], atual[0], suavizacao, argumento));
            ys.add(quadro(duracao, inicio[1], atual[1], suavizacao, argumento));
            zs.add(quadro(duracao, inicio[2], atual[2], suavizacao, argumento));
            anterior = atual;
            tempoAnterior = entrada.tempo();
        }

        return new CanalAnimacao(spline(xs), spline(ys), spline(zs));
    }

    private static QuadroChave quadro(double duracao, double inicio, double fim, Suavizacao suavizacao, Double argumento) {
        return new QuadroChave(duracao, inicio, fim, suavizacao.curva(argumento), suavizacao.isSpline(), inicio, fim);
    }

    private static QuadroChave[] spline(List<QuadroChave> quadros) {
        if (quadros.size() == 1) {
            QuadroChave q = quadros.getFirst();
            return new QuadroChave[]{new QuadroChave(q.duracao(), q.inicio(), q.fim(), Suavizacao.LINEAR.curva(null), false, q.inicio(), q.fim())};
        }
        QuadroChave[] resultado = new QuadroChave[quadros.size()];
        for (int i = 0; i < quadros.size(); i++) {
            QuadroChave q = quadros.get(i);
            if (q.spline()) {
                double antes = i < 2 ? q.inicio() : quadros.get(i - 2).fim();
                double depois = i + 1 >= quadros.size() ? q.fim() : quadros.get(i + 1).fim();
                q = new QuadroChave(q.duracao(), q.inicio(), q.fim(), q.curva(), true, antes, depois);
            }
            resultado[i] = q;
        }
        return resultado;
    }

    private static double numero(JsonElement elemento) {
        if (elemento.isJsonPrimitive() && elemento.getAsJsonPrimitive().isNumber()) return elemento.getAsDouble();
        try {
            return Double.parseDouble(elemento.getAsString().trim());
        } catch (NumberFormatException e) {
            throw new JsonParseException("Expressoes Molang nao sao suportadas: " + elemento);
        }
    }

    private static double tempo(String texto) {
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
