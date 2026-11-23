package net.aelysium.aelysiummod.nucleo.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class DanoTooltipCliente {
    private static final String CHAVE_IGUAL = "attribute.modifier.equals.0";

    private DanoTooltipCliente() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void aoMontarTooltip(ItemTooltipEvent evento) {
        ItemStack pilha = evento.getItemStack();
        ItemEnchantments encantos = pilha.getEnchantments();
        if (encantos.isEmpty()) return;
        List<Component> linhas = evento.getToolTip();
        String chaveAtaque = Attributes.ATTACK_DAMAGE.value().getDescriptionId();
        for (int i = 0; i < linhas.size(); i++) {
            TranslatableContents conteudo = achar(linhas.get(i));
            if (conteudo == null || !CHAVE_IGUAL.equals(conteudo.getKey())) continue;
            Object[] args = conteudo.getArgs();
            if (args.length < 2 || !(args[1] instanceof Component nome)) continue;
            if (!(nome.getContents() instanceof TranslatableContents nomeTrad) || !chaveAtaque.equals(nomeTrad.getKey())) continue;
            double base;
            try {
                base = Double.parseDouble(String.valueOf(args[0]));
            } catch (NumberFormatException e) {
                continue;
            }
            RandomSource aleatorio = RandomSource.create();
            float valor = (float) base;
            List<Component> condicionais = new ArrayList<>();
            for (var entrada : encantos.entrySet()) {
                Holder<Enchantment> encanto = entrada.getKey();
                int nivel = entrada.getIntValue();
                for (ConditionalEffect<EnchantmentValueEffect> efeito : encanto.value().getEffects(EnchantmentEffectComponents.DAMAGE)) {
                    if (efeito.requirements().isEmpty()) {
                        valor = efeito.effect().process(nivel, aleatorio, valor);
                    } else {
                        float extra = efeito.effect().process(nivel, aleatorio, 0.0F);
                        if (extra > 0.0F && !ehFlecha(encanto)) {
                            condicionais.add(Component.literal("  +" + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(extra) + " ")
                                    .append(Enchantment.getFullname(encanto, nivel))
                                    .withStyle(ChatFormatting.GRAY));
                        }
                    }
                }
            }
            if (valor != (float) base) {
                MutableComponent nova = CommonComponents.space()
                        .append(Component.translatable(CHAVE_IGUAL, ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(valor), nome))
                        .withStyle(ChatFormatting.DARK_GREEN);
                linhas.set(i, nova);
            }
            linhas.addAll(i + 1, condicionais);
            return;
        }
    }

    private static boolean ehFlecha(Holder<Enchantment> encanto) {
        return encanto.is(net.minecraft.world.item.enchantment.Enchantments.POWER);
    }

    private static TranslatableContents achar(Component linha) {
        if (linha.getContents() instanceof TranslatableContents t) return t;
        for (Component filho : linha.getSiblings()) {
            if (filho.getContents() instanceof TranslatableContents t) return t;
        }
        return null;
    }
}
