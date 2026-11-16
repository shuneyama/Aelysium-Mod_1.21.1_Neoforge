package net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm;

import java.lang.reflect.Field;

public final class ReflexaoCpm {

    private static boolean inicializado;
    private static Field campoListaQuads;
    private static Field campoVertices;

    private static synchronized void inicializar() {
        if (inicializado) {
            return;
        }
        inicializado = true;
        try {
            Class<?> classeMalha = Class.forName("com.tom.cpm.shared.model.render.BoxRender$QuadMesh");
            Class<?> classeQuad = Class.forName("com.tom.cpm.shared.model.render.BoxRender$Quad");
            campoListaQuads = classeMalha.getDeclaredField("quadList");
            campoListaQuads.setAccessible(true);
            campoVertices = classeQuad.getDeclaredField("vertexPositions");
            campoVertices.setAccessible(true);
        } catch (ReflectiveOperationException | RuntimeException e) {
            campoListaQuads = null;
            campoVertices = null;
        }
    }

    public static Object[] obterQuads(Object malha) {
        inicializar();
        return lerVetor(campoListaQuads, malha);
    }

    public static Object[] obterVertices(Object quad) {
        inicializar();
        return lerVetor(campoVertices, quad);
    }

    private static Object[] lerVetor(Field campo, Object alvo) {
        if (campo == null) {
            return null;
        }
        try {
            return (Object[]) campo.get(alvo);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private ReflexaoCpm() {
    }
}
