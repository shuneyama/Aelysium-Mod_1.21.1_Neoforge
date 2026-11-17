package net.aelysium.aelysiummod.conteudo.filhotes.aquatico;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TamanhoSalmao {

    public static final String CHAVE = "AelysiumTamanhoSalmao";
    public static final int PEQUENO = 0;
    public static final int MEDIO = 1;
    public static final int GRANDE = 2;
    public static final ResourceLocation ID_ESCALA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "tamanho_salmao");

    private TamanhoSalmao() {
    }

    public static int sortear(RandomSource aleatorio) {
        int valor = aleatorio.nextInt(95);
        if (valor < 30) return PEQUENO;
        if (valor < 80) return MEDIO;
        return GRANDE;
    }

    public static int obter(LivingEntity entidade) {
        AttributeInstance atributo = entidade.getAttribute(Attributes.SCALE);
        if (atributo == null) return MEDIO;
        AttributeModifier modificador = atributo.getModifier(ID_ESCALA);
        if (modificador == null) return MEDIO;
        return modificador.amount() < 0 ? PEQUENO : GRANDE;
    }

    public static void definir(LivingEntity entidade, int variante) {
        AttributeInstance atributo = entidade.getAttribute(Attributes.SCALE);
        if (atributo == null) return;
        atributo.removeModifier(ID_ESCALA);
        if (variante == PEQUENO) {
            atributo.addPermanentModifier(new AttributeModifier(ID_ESCALA, -0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (variante == GRANDE) {
            atributo.addPermanentModifier(new AttributeModifier(ID_ESCALA, 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
