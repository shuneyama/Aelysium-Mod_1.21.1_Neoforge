package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModNiveisFerramenta {
    public static final Tier ADM = new SimpleTier(ModTags.Blocos.INCORRECT_FOR_ADM_TOOL,
            1400, 4f, 3f, 28, () -> Ingredient.of(ModItens.ESPADA_ADM));
    public static final Tier CIANE = new SimpleTier(ModTags.Blocos.INCORRECT_FOR_CIANE_TOOL,
            2512, 6f, 3f, 28, () -> Ingredient.of(ModItens.CIANE_BARRA));
}