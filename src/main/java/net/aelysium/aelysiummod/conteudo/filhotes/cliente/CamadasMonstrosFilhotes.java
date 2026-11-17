package net.aelysium.aelysiummod.conteudo.filhotes.cliente;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.MalhasMonstrosFilhotes;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.EnumMap;
import java.util.Map;

public final class CamadasMonstrosFilhotes {

    private static final EquipmentSlot[] PARTES = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static final ModelLayerLocation ZUMBI = UtilFilhotes.camada("zumbi_filhote");
    public static final ModelLayerLocation ZUMBI_EXTERNA = UtilFilhotes.camada("zumbi_filhote_externa");
    public static final ModelLayerLocation ALDEAO_ZUMBI = UtilFilhotes.camada("aldeao_zumbi_filhote");
    public static final ModelLayerLocation ALDEAO = UtilFilhotes.camada("aldeao_filhote");
    public static final ModelLayerLocation PIGLIN = UtilFilhotes.camada("piglin_filhote");
    public static final ModelLayerLocation HOGLIN = UtilFilhotes.camada("hoglin_filhote");

    public static final Map<EquipmentSlot, ModelLayerLocation> ARMADURA = mapa("armadura_filhote_");
    public static final Map<EquipmentSlot, ModelLayerLocation> ARMADURA_PIGLIN = mapa("armadura_piglin_filhote_");

    private CamadasMonstrosFilhotes() {
    }

    private static Map<EquipmentSlot, ModelLayerLocation> mapa(String prefixo) {
        Map<EquipmentSlot, ModelLayerLocation> mapa = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot parte : PARTES) {
            mapa.put(parte, UtilFilhotes.camada(prefixo + parte.getName()));
        }
        return mapa;
    }

    public static void registrar(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ZUMBI, () -> MalhasMonstrosFilhotes.zumbi(CubeDeformation.NONE));
        event.registerLayerDefinition(ZUMBI_EXTERNA, () -> MalhasMonstrosFilhotes.zumbi(new CubeDeformation(0.25F)));
        event.registerLayerDefinition(ALDEAO_ZUMBI, MalhasMonstrosFilhotes::aldeaoZumbi);
        event.registerLayerDefinition(ALDEAO, MalhasMonstrosFilhotes::aldeao);
        event.registerLayerDefinition(PIGLIN, MalhasMonstrosFilhotes::piglin);
        event.registerLayerDefinition(HOGLIN, MalhasMonstrosFilhotes::hoglin);
        CubeDeformation externa = new CubeDeformation(-0.1F, 0.5F, 0.3F);
        CubeDeformation interna = new CubeDeformation(-0.1F, 0.3F, 0.3F);
        CubeDeformation piglin = new CubeDeformation(0.7F);
        for (EquipmentSlot parte : PARTES) {
            CubeDeformation g = parte == EquipmentSlot.LEGS ? interna : externa;
            event.registerLayerDefinition(ARMADURA.get(parte), () -> MalhasMonstrosFilhotes.armadura(parte, g, 0.0F, 0.0F, 0.0F));
            event.registerLayerDefinition(ARMADURA_PIGLIN.get(parte), () -> MalhasMonstrosFilhotes.armadura(parte, piglin, 0.5F, -0.5F, 0.0F));
        }
    }
}
