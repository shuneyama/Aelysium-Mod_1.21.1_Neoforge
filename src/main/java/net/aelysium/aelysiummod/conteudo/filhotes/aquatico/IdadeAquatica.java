package net.aelysium.aelysiummod.conteudo.filhotes.aquatico;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Squid;

public final class IdadeAquatica {

    public static final String CHAVE = "AelysiumIdadeAquatica";
    public static final int DURACAO = 24000;
    public static final ResourceLocation ID_ESCALA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "filhote_aquatico");

    private IdadeAquatica() {
    }

    public static boolean aplicavel(Entity entidade) {
        return entidade instanceof Squid || entidade instanceof Dolphin;
    }

    public static float chanceNascer(Entity entidade) {
        return entidade instanceof Dolphin ? 0.1F : 0.05F;
    }

    private static double escala(Entity entidade) {
        return entidade instanceof Dolphin ? 0.65D : 0.625D;
    }

    public static boolean ehFilhote(Entity entidade) {
        if (!(entidade instanceof LivingEntity vivo) || !aplicavel(entidade)) return false;
        AttributeInstance atributo = vivo.getAttribute(Attributes.SCALE);
        return atributo != null && atributo.hasModifier(ID_ESCALA);
    }

    public static void tornarFilhote(LivingEntity entidade, int ticks) {
        AttributeInstance atributo = entidade.getAttribute(Attributes.SCALE);
        if (atributo == null) return;
        atributo.removeModifier(ID_ESCALA);
        atributo.addPermanentModifier(new AttributeModifier(ID_ESCALA, escala(entidade) - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        entidade.getPersistentData().putInt(CHAVE, ticks);
    }

    public static void crescer(LivingEntity entidade) {
        AttributeInstance atributo = entidade.getAttribute(Attributes.SCALE);
        if (atributo != null) atributo.removeModifier(ID_ESCALA);
        entidade.getPersistentData().remove(CHAVE);
    }
}
