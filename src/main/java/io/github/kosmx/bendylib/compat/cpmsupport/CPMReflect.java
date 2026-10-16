package io.github.kosmx.bendylib.compat.cpmsupport;

import java.lang.reflect.Field;

public final class CPMReflect {

    private static volatile boolean initialized = false;
    private static Field quadListField;
    private static Field vertexPositionsField;

    private static synchronized void ensureInit() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            Class<?> quadMeshClass = Class.forName("com.tom.cpm.shared.model.render.BoxRender$QuadMesh");
            Class<?> quadClass = Class.forName("com.tom.cpm.shared.model.render.BoxRender$Quad");
            quadListField = quadMeshClass.getDeclaredField("quadList");
            quadListField.setAccessible(true);
            vertexPositionsField = quadClass.getDeclaredField("vertexPositions");
            vertexPositionsField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            quadListField = null;
            vertexPositionsField = null;
        }
    }

    public static Object[] getQuadList(Object quadMesh) {
        ensureInit();
        if (quadListField == null) {
            return null;
        }
        try {
            return (Object[]) quadListField.get(quadMesh);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public static Object[] getVertexPositions(Object quad) {
        ensureInit();
        if (vertexPositionsField == null) {
            return null;
        }
        try {
            return (Object[]) vertexPositionsField.get(quad);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private CPMReflect() {
    }
}
