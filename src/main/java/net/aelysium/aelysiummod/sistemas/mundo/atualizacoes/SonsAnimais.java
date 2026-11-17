package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SonsAnimais {

    public static final List<String> GATO = List.of("classic", "royal");
    public static final List<String> PORCO = List.of("classic", "big", "mini");
    public static final List<String> VACA = List.of("classic", "moody");
    public static final List<String> GALINHA = List.of("classic", "picky");

    private static final Map<String, SoundEvent> CACHE = new HashMap<>();
    private static final Set<String> EXISTENTES = Set.of(
            "entity.baby_cat.ambient", "entity.baby_cat.beg_for_food", "entity.baby_cat.death", "entity.baby_cat.eat",
            "entity.baby_cat.hiss", "entity.baby_cat.hurt", "entity.baby_cat.purr", "entity.baby_cat.purreow", "entity.baby_cat.stray_ambient",
            "entity.baby_chicken.ambient", "entity.baby_chicken.death", "entity.baby_chicken.hurt", "entity.baby_chicken.step",
            "entity.baby_horse.ambient", "entity.baby_horse.angry", "entity.baby_horse.breathe", "entity.baby_horse.death",
            "entity.baby_horse.eat", "entity.baby_horse.hurt", "entity.baby_horse.land", "entity.baby_horse.step",
            "entity.baby_pig.ambient", "entity.baby_pig.death", "entity.baby_pig.eat", "entity.baby_pig.hurt", "entity.baby_pig.step",
            "entity.cat_royal.ambient", "entity.cat_royal.beg_for_food", "entity.cat_royal.death", "entity.cat_royal.eat",
            "entity.cat_royal.hiss", "entity.cat_royal.hurt", "entity.cat_royal.purr", "entity.cat_royal.purreow", "entity.cat_royal.stray_ambient",
            "entity.chicken_picky.ambient", "entity.chicken_picky.death", "entity.chicken_picky.hurt",
            "entity.cow_moody.ambient", "entity.cow_moody.death", "entity.cow_moody.hurt", "entity.cow_moody.step",
            "entity.pig_big.ambient", "entity.pig_big.death", "entity.pig_big.eat", "entity.pig_big.hurt",
            "entity.pig_mini.ambient", "entity.pig_mini.death", "entity.pig_mini.eat", "entity.pig_mini.hurt"
    );

    private SonsAnimais() {
    }

    public static SoundEvent trocar(Entity entidade, String animal, List<String> variantes, String tipo, SoundEvent original) {
        String nome = null;
        if (entidade instanceof LivingEntity vivo && vivo.isBaby() && EXISTENTES.contains("entity.baby_" + animal + "." + tipo)) {
            nome = "entity.baby_" + animal + "." + tipo;
        } else if (!variantes.isEmpty()) {
            String variante = variantes.get(Math.floorMod(entidade.getUUID().hashCode(), variantes.size()));
            if (!variante.equals("classic")) {
                String candidato = "entity." + animal + "_" + variante + "." + tipo;
                if (EXISTENTES.contains(candidato)) nome = candidato;
            }
        }
        if (nome == null) return original;
        return CACHE.computeIfAbsent(nome, n -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, n)));
    }
}
