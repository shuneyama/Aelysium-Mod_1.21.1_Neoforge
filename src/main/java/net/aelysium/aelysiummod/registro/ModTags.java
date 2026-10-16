package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Blocos {
        public static final TagKey<Block> INCORRECT_FOR_ADM_TOOL = createTag("incorrect_adm_tool");
        public static final TagKey<Block> INCORRECT_FOR_CIANE_TOOL = createTag("incorrect_ciane_tool");
        public static final TagKey<Block> NEEDS_CIANE_TOOL = createTag("needs_ciane_tool");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, name));
        }
    }

    public static class Itens {
        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, name));
        }
    }
}