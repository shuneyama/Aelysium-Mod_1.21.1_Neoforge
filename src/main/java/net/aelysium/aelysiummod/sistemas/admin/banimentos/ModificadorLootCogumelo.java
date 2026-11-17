package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.aelysium.aelysiummod.registro.ModModificadoresLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class ModificadorLootCogumelo extends LootModifier {

    private static final ResourceLocation TABELA = ResourceLocation.withDefaultNamespace("entities/zombie");

    public static final MapCodec<ModificadorLootCogumelo> CODEC =
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, ModificadorLootCogumelo::new));

    public ModificadorLootCogumelo(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!TABELA.equals(context.getQueriedLootTableId())) return loot;
        Entity entidade = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entidade == null || !(entidade.getVehicle() instanceof ZombieHorse)) return loot;
        int quantidade = context.getRandom().nextInt(2);
        if (context.getParamOrNull(LootContextParams.ATTACKING_ENTITY) instanceof LivingEntity atacante) {
            int saque = EnchantmentHelper.getEnchantmentLevel(context.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), atacante);
            if (saque > 0) quantidade += Math.round(saque * context.getRandom().nextFloat());
        }
        if (quantidade > 0) loot.add(new ItemStack(Items.RED_MUSHROOM, quantidade));
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModModificadoresLoot.ZOMBIE_HORSE_MUSHROOM.get();
    }
}
