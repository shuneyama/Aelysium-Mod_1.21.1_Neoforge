package net.aelysium.aelysiummod.nucleo.combate;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class DefesaAelysium {
    public static final float ARMADURA_MAXIMA = 30.0F;
    public static final float RESISTENCIA_MAXIMA = 20.0F;

    private static final float LIMIAR_DANO = 20.0F;
    private static final float BASE = 10.0F;
    private static final float DIVISOR_BASE = 2.0F;
    private static final float DIVISOR_RESISTENCIA = 0.3F;
    private static final int INTERVALO_LIMITE = 20;

    private DefesaAelysium() {
    }

    public static float fracaoRecebida(float dano, float armadura, float resistencia) {
        float arm = Mth.clamp(armadura, 0.0F, ARMADURA_MAXIMA);
        float res = Mth.clamp(resistencia, 0.0F, RESISTENCIA_MAXIMA);
        float a = dano < LIMIAR_DANO ? BASE : BASE + (dano - LIMIAR_DANO) / (DIVISOR_BASE + DIVISOR_RESISTENCIA * res);
        return a / (a + arm);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void aoReceberDano(LivingIncomingDamageEvent evento) {
        DamageSource fonte = evento.getSource();
        if (fonte.is(DamageTypeTags.BYPASSES_ARMOR)) return;
        LivingEntity entidade = evento.getEntity();
        evento.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reducaoVanilla) -> {
            float dano = container.getNewDamage();
            float armadura = (float) entidade.getArmorValue();
            float resistencia = (float) entidade.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            float protecao = 1.0F - fracaoRecebida(dano, armadura, resistencia);
            ItemStack arma = fonte.getWeaponItem();
            if (arma != null && entidade.level() instanceof ServerLevel nivel) {
                protecao = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(nivel, arma, entidade, fonte, protecao), 0.0F, 1.0F);
            }
            return dano * protecao;
        });
    }

    @SubscribeEvent
    public static void aoTickarEntidade(EntityTickEvent.Post evento) {
        if (!(evento.getEntity() instanceof LivingEntity vivo)) return;
        if (vivo.level().isClientSide || vivo.tickCount % INTERVALO_LIMITE != 0) return;
        limitar(vivo, Attributes.ARMOR, ARMADURA_MAXIMA);
        limitar(vivo, Attributes.ARMOR_TOUGHNESS, RESISTENCIA_MAXIMA);
    }

    private static void limitar(LivingEntity vivo, Holder<Attribute> atributo, float maximo) {
        AttributeInstance instancia = vivo.getAttribute(atributo);
        if (instancia != null && instancia.getBaseValue() > maximo) {
            instancia.setBaseValue(maximo);
        }
    }
}
