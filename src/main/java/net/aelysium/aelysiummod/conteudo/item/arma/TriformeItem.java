package net.aelysium.aelysiummod.conteudo.item.arma;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.List;

public class TriformeItem extends SwordItem {

    private static final int ESPADA = 0;
    private static final int KATANA = 1;
    private static final int ADAGA = 2;
    private static final int FORMAS = 3;

    private static final double DANO_BASE = 3.0;
    private static final double VELOCIDADE_BASE = -2.4;
    private static final double AJUSTE_ALCANCE = 1.0;
    private static final double AJUSTE_VELOCIDADE = 0.3;

    private static final ResourceLocation ALCANCE_ID = ResourceLocation.fromNamespaceAndPath("aelysiummod", "triforme_alcance");

    public TriformeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public static ItemAttributeModifiers atributos(int forma) {
        double sinal = forma == KATANA ? 1.0 : forma == ADAGA ? -1.0 : 0.0;

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, DANO_BASE + Tiers.NETHERITE.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, VELOCIDADE_BASE - sinal * AJUSTE_VELOCIDADE, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);

        if (sinal != 0.0) {
            builder.add(Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(ALCANCE_ID, sinal * AJUSTE_ALCANCE, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }

        return builder.build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        int atual = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.DEFAULT).value();
        int passo = player.isShiftKeyDown() ? FORMAS - 1 : 1;
        int proxima = Math.floorMod(atual + passo, FORMAS);

        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(proxima));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, atributos(proxima));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0f, 2.0f);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.triforme1"));
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.triforme2"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.triforme_shift_down"));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
