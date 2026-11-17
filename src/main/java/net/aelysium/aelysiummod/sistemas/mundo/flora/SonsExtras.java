package net.aelysium.aelysiummod.sistemas.mundo.flora;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SonsExtras {

    public static final SoundEvent LITTER_BREAK = evento("block.leaf_litter.break");
    public static final SoundEvent LITTER_FALL = evento("block.leaf_litter.fall");
    public static final SoundEvent LITTER_HIT = evento("block.leaf_litter.hit");
    public static final SoundEvent LITTER_PLACE = evento("block.leaf_litter.place");
    public static final SoundEvent LITTER_STEP = evento("block.leaf_litter.step");
    public static final SoundEvent CACTUS_FLOWER_BREAK = evento("block.cactus_flower.break");
    public static final SoundEvent CACTUS_FLOWER_PLACE = evento("block.cactus_flower.place");
    public static final SoundEvent FIREFLY_IDLE = evento("block.firefly_bush.idle");
    public static final SoundEvent LEAD_BREAK = evento("item.lead.break");
    public static final SoundEvent LEAD_TIED = evento("item.lead.tied");
    public static final SoundEvent LEAD_UNTIED = evento("item.lead.untied");
    public static final SoundEvent SHEARS_SNIP = evento("item.shears.snip");
    public static final SoundEvent SELA_RETIRADA = evento("item.saddle.unequip");
    public static final SoundEvent ARMADURA_RETIRADA = evento("item.horse_armor.unequip");
    public static final SoundEvent TAPETE_RETIRADO = evento("item.llama_carpet.unequip");
    public static final SoundEvent IRON_BREAK = evento("block.iron.break");
    public static final SoundEvent IRON_FALL = evento("block.iron.fall");
    public static final SoundEvent IRON_HIT = evento("block.iron.hit");
    public static final SoundEvent IRON_PLACE = evento("block.iron.place");
    public static final SoundEvent IRON_STEP = evento("block.iron.step");

    public static final SoundType LEAF_LITTER = new SoundType(1.0F, 1.0F, LITTER_BREAK, LITTER_STEP, LITTER_PLACE, LITTER_HIT, LITTER_FALL);
    public static final SoundType CACTUS_FLOWER = new SoundType(1.0F, 1.0F, CACTUS_FLOWER_BREAK, SoundEvents.EMPTY, CACTUS_FLOWER_PLACE, SoundEvents.EMPTY, SoundEvents.EMPTY);
    public static final SoundType IRON = new SoundType(1.0F, 1.0F, IRON_BREAK, IRON_STEP, IRON_PLACE, IRON_HIT, IRON_FALL);

    public static final String[] VARIANTES_LOBO = {"classic", "big", "cute", "puglin", "angry", "grumpy", "sad"};
    public static final Map<String, Map<String, SoundEvent>> LOBOS = new LinkedHashMap<>();
    public static final Map<String, SoundEvent> FILHOTE = new LinkedHashMap<>();

    static {
        for (String variante : VARIANTES_LOBO) {
            if (variante.equals("classic")) continue;
            Map<String, SoundEvent> sons = new LinkedHashMap<>();
            for (String tipo : new String[]{"ambient", "death", "growl", "hurt", "pant", "whine"}) {
                sons.put(tipo, evento("entity.wolf_" + variante + "." + tipo));
            }
            LOBOS.put(variante, sons);
        }
        for (String tipo : new String[]{"ambient", "death", "growl", "hurt", "pant", "step", "whine"}) {
            FILHOTE.put(tipo, evento("entity.baby_wolf." + tipo));
        }
    }

    private SonsExtras() {
    }

    private static SoundEvent evento(String nome) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, nome));
    }
}
