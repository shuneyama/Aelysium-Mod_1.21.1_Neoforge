package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FormasDecoracao {
    private static final String RECURSO = "/formas_decoracao.json";
    private static final Map<String, List<Variante>> DADOS = carregar();

    private record Variante(String[][] pares, VoxelShape forma) {
    }

    private FormasDecoracao() {
    }

    public static VoxelShape obter(String id, BlockState estado) {
        List<Variante> lista = DADOS.get(id);
        if (lista == null) {
            return Shapes.block();
        }
        for (Variante variante : lista) {
            if (combina(variante, estado)) {
                return variante.forma();
            }
        }
        return Shapes.block();
    }

    private static boolean combina(Variante variante, BlockState estado) {
        StateDefinition<Block, BlockState> definicao = estado.getBlock().getStateDefinition();
        for (String[] par : variante.pares()) {
            Property<?> propriedade = definicao.getProperty(par[0]);
            if (propriedade == null || !nomeValor(estado, propriedade).equals(par[1])) {
                return false;
            }
        }
        return true;
    }

    private static <T extends Comparable<T>> String nomeValor(BlockState estado, Property<T> propriedade) {
        return propriedade.getName(estado.getValue(propriedade));
    }

    private static Map<String, List<Variante>> carregar() {
        Map<String, List<Variante>> mapa = new HashMap<>();
        try (InputStream entrada = FormasDecoracao.class.getResourceAsStream(RECURSO)) {
            if (entrada == null) {
                return mapa;
            }
            JsonObject raiz = JsonParser.parseReader(new InputStreamReader(entrada, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> bloco : raiz.entrySet()) {
                List<Variante> lista = new ArrayList<>();
                for (JsonElement elemento : bloco.getValue().getAsJsonArray()) {
                    JsonObject objeto = elemento.getAsJsonObject();
                    lista.add(new Variante(pares(objeto.get("k").getAsString()), construir(objeto)));
                }
                mapa.put(bloco.getKey(), lista);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler " + RECURSO, e);
        }
        return mapa;
    }

    private static String[][] pares(String chave) {
        if (chave.isEmpty()) {
            return new String[0][];
        }
        String[] partes = chave.split(",");
        String[][] pares = new String[partes.length][];
        for (int i = 0; i < partes.length; i++) {
            pares[i] = partes[i].split("=");
        }
        return pares;
    }

    private static VoxelShape construir(JsonObject objeto) {
        int giroX = Math.floorMod(objeto.get("x").getAsInt() / 90, 4);
        int giroY = Math.floorMod(objeto.get("y").getAsInt() / 90, 4);
        VoxelShape forma = Shapes.empty();
        for (JsonElement elemento : objeto.getAsJsonArray("c")) {
            JsonArray caixa = elemento.getAsJsonArray();
            double[] a = {caixa.get(0).getAsDouble() - 8, caixa.get(1).getAsDouble() - 8, caixa.get(2).getAsDouble() - 8};
            double[] b = {caixa.get(3).getAsDouble() - 8, caixa.get(4).getAsDouble() - 8, caixa.get(5).getAsDouble() - 8};
            for (int i = 0; i < giroX; i++) {
                a = girarX(a);
                b = girarX(b);
            }
            for (int i = 0; i < giroY; i++) {
                a = girarY(a);
                b = girarY(b);
            }
            forma = Shapes.or(forma, Block.box(
                    Math.min(a[0], b[0]) + 8, Math.min(a[1], b[1]) + 8, Math.min(a[2], b[2]) + 8,
                    Math.max(a[0], b[0]) + 8, Math.max(a[1], b[1]) + 8, Math.max(a[2], b[2]) + 8));
        }
        return forma.optimize();
    }

    private static double[] girarX(double[] p) {
        return new double[]{p[0], p[2], -p[1]};
    }

    private static double[] girarY(double[] p) {
        return new double[]{-p[2], p[1], p[0]};
    }
}
