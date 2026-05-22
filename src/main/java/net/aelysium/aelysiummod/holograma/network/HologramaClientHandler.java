package net.aelysium.aelysiummod.holograma.network;

import net.aelysium.aelysiummod.holograma.network.HologramaPackets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.aelysium.aelysiummod.holograma.HologramaCorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramaClientHandler {

    private static final Map<String, List<Display.TextDisplay>> hologramasAtivos = new ConcurrentHashMap<>();

    private static EntityDataAccessor<Component> DATA_TEXT;
    private static EntityDataAccessor<Integer> DATA_BACKGROUND_COLOR;
    private static EntityDataAccessor<Byte> DATA_BILLBOARD;
    private static EntityDataAccessor<Float> DATA_VIEW_RANGE;
    private static EntityDataAccessor<Float> DATA_SHADOW_RADIUS;

    private static boolean accessorsInicializados = false;

    @SuppressWarnings("unchecked")
    private static void inicializarAccessors() {
        if (accessorsInicializados) return;
        try {
            for (Field f : Display.TextDisplay.class.getDeclaredFields()) {
                f.setAccessible(true);
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                if (f.getType() != EntityDataAccessor.class) continue;

                Object val = f.get(null);
                if (val == null) continue;

                String name = f.getName();
                if (name.contains("TEXT") && !name.contains("OPACITY") && !name.contains("WIDTH")
                        && !name.contains("BACKGROUND") && !name.contains("SHADOW")
                        && !name.contains("LINE") && !name.contains("ALIGN")) {
                    DATA_TEXT = (EntityDataAccessor<Component>) val;
                } else if (name.contains("BACKGROUND")) {
                    DATA_BACKGROUND_COLOR = (EntityDataAccessor<Integer>) val;
                }
            }

            for (Field f : Display.class.getDeclaredFields()) {
                f.setAccessible(true);
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                if (f.getType() != EntityDataAccessor.class) continue;

                Object val = f.get(null);
                if (val == null) continue;

                String name = f.getName();
                if (name.contains("BILLBOARD")) {
                    DATA_BILLBOARD = (EntityDataAccessor<Byte>) val;
                } else if (name.contains("VIEW_RANGE")) {
                    DATA_VIEW_RANGE = (EntityDataAccessor<Float>) val;
                } else if (name.contains("SHADOW_RADIUS")) {
                    DATA_SHADOW_RADIUS = (EntityDataAccessor<Float>) val;
                }
            }

            accessorsInicializados = true;
        } catch (Exception e) {
            System.err.println("[AelysiumHolograma] Erro ao inicializar accessors de Display: " + e.getMessage());
        }
    }

    public static void handleSpawn(SpawnHologramaS2C payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level == null) return;

            removerEntidades(payload.nome());
            criarEntidades(level, payload.nome(), payload.x(), payload.y(), payload.z(),
                    payload.linhas(), payload.offsets());
        });
    }

    public static void handleDestroy(DestroyHologramaS2C payload, IPayloadContext context) {
        context.enqueueWork(() -> removerEntidades(payload.nome()));
    }

    public static void handleUpdate(UpdateHologramaS2C payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level == null) return;

            removerEntidades(payload.nome());
            criarEntidades(level, payload.nome(), payload.x(), payload.y(), payload.z(),
                    payload.linhas(), payload.offsets());
        });
    }

    private static void criarEntidades(ClientLevel level, String nome, double x, double y, double z,
                                       List<String> linhas, List<Double> offsets) {
        if (linhas.isEmpty()) return;
        inicializarAccessors();

        List<Display.TextDisplay> entities = new ArrayList<>();

        double alturaTotal = 0;
        for (double offset : offsets) {
            alturaTotal += offset;
        }

        double posY = y + alturaTotal;

        for (int i = 0; i < linhas.size(); i++) {
            String textoLinha = linhas.get(i);
            double offset = offsets.get(i);

            posY -= offset;

            Display.TextDisplay textDisplay = EntityType.TEXT_DISPLAY.create(level);
            if (textDisplay == null) continue;

            textDisplay.setPos(x, posY, z);

            var entityData = textDisplay.getEntityData();

            if (DATA_TEXT != null) {
                entityData.set(DATA_TEXT, HologramaCorParser.parse(textoLinha));
            }
            if (DATA_BACKGROUND_COLOR != null) {
                entityData.set(DATA_BACKGROUND_COLOR, 0x40000000);
            }
            if (DATA_BILLBOARD != null) {
                entityData.set(DATA_BILLBOARD, (byte) 3);
            }
            if (DATA_VIEW_RANGE != null) {
                entityData.set(DATA_VIEW_RANGE, 1.0f);
            }
            if (DATA_SHADOW_RADIUS != null) {
                entityData.set(DATA_SHADOW_RADIUS, 0.0f);
            }

            level.addEntity(textDisplay);
            entities.add(textDisplay);
        }

        hologramasAtivos.put(nome.toLowerCase(), entities);
    }

    private static void removerEntidades(String nome) {
        List<Display.TextDisplay> entities = hologramasAtivos.remove(nome.toLowerCase());
        if (entities != null) {
            for (Display.TextDisplay entity : entities) {
                entity.discard();
            }
        }
    }

    public static void limparTudo() {
        for (List<Display.TextDisplay> entities : hologramasAtivos.values()) {
            for (Display.TextDisplay entity : entities) {
                entity.discard();
            }
        }
        hologramasAtivos.clear();
    }
}