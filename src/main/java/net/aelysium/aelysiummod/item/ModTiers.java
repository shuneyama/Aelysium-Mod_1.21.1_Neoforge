package net.aelysium.aelysiummod.item;

import net.aelysium.aelysiummod.util.ModTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModTiers {
    public static final Tier ADM = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_ADM_TOOL,
            1400, 4f, 3f, 28, () -> Ingredient.of(ModItens.ESPADA_ADM));
    public static final Tier CIANE = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_CIANE_TOOL,
            2512, 6f, 3f, 28, () -> Ingredient.of(ModItens.CIANE_BARRA));
}