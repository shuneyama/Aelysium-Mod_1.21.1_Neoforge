package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public final class AtributosDeus {
    public static final double VIDA_ALVO = 400.0;

    private static final ResourceLocation MOD_VIDA = id("deus_max_health");
    private static final ResourceLocation MOD_VOO = id("deus_creative_flight");
    private static final String ATRIBUTO_VOO = "neoforge:creative_flight";
    private static final List<ResourceLocation> MODS_ANTIGOS = List.of(id("deus_armor"), id("deus_armor_toughness"), id("deus_attack_damage"), id("deus_spell_power"));
    private static final String ATRIBUTO_MAGIA = "irons_spellbooks:spell_power";

    private AtributosDeus() {
    }

    private static ResourceLocation id(String caminho) {
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, caminho);
    }

    public static void aplicar(ServerPlayer jogador) {
        limparAntigos(jogador);
        ajustarVida(jogador);
        AttributeInstance voo = atributo(jogador, ATRIBUTO_VOO);
        if (voo != null) {
            voo.removeModifier(MOD_VOO);
            voo.addPermanentModifier(new AttributeModifier(MOD_VOO, 1.0, AttributeModifier.Operation.ADD_VALUE));
        }
        jogador.getAbilities().mayfly = true;
        jogador.onUpdateAbilities();
        jogador.setHealth(jogador.getMaxHealth());
        jogador.getFoodData().setFoodLevel(20);
        jogador.getFoodData().setSaturation(20.0F);
        jogador.getFoodData().setExhaustion(0.0F);
    }

    public static void remover(ServerPlayer jogador) {
        limparAntigos(jogador);
        AttributeInstance vida = jogador.getAttribute(Attributes.MAX_HEALTH);
        if (vida != null) vida.removeModifier(MOD_VIDA);
        AttributeInstance voo = atributo(jogador, ATRIBUTO_VOO);
        if (voo != null) voo.removeModifier(MOD_VOO);
        if (jogador.getHealth() > jogador.getMaxHealth()) {
            jogador.setHealth(jogador.getMaxHealth());
        }
    }

    private static void ajustarVida(ServerPlayer jogador) {
        AttributeInstance vida = jogador.getAttribute(Attributes.MAX_HEALTH);
        if (vida == null) return;
        vida.removeModifier(MOD_VIDA);
        double semModificador = vida.getValue();
        vida.addPermanentModifier(new AttributeModifier(MOD_VIDA, 1.0, AttributeModifier.Operation.ADD_VALUE));
        double inclinacao = vida.getValue() - semModificador;
        vida.removeModifier(MOD_VIDA);
        double quantidade = inclinacao > 0.0 ? (VIDA_ALVO - semModificador) / inclinacao : VIDA_ALVO - semModificador;
        vida.addPermanentModifier(new AttributeModifier(MOD_VIDA, quantidade, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void limparAntigos(ServerPlayer jogador) {
        AttributeInstance armadura = jogador.getAttribute(Attributes.ARMOR);
        AttributeInstance resistencia = jogador.getAttribute(Attributes.ARMOR_TOUGHNESS);
        AttributeInstance dano = jogador.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance magia = atributo(jogador, ATRIBUTO_MAGIA);
        for (ResourceLocation antigo : MODS_ANTIGOS) {
            if (armadura != null) armadura.removeModifier(antigo);
            if (resistencia != null) resistencia.removeModifier(antigo);
            if (dano != null) dano.removeModifier(antigo);
            if (magia != null) magia.removeModifier(antigo);
        }
    }

    private static AttributeInstance atributo(ServerPlayer jogador, String idAtributo) {
        try {
            Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(idAtributo)).orElse(null);
            if (holder == null || !jogador.getAttributes().hasAttribute(holder)) return null;
            return jogador.getAttribute(holder);
        } catch (Exception e) {
            return null;
        }
    }
}
