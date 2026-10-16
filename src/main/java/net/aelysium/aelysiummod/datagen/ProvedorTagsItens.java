package net.aelysium.aelysiummod.datagen;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ProvedorTagsItens extends ItemTagsProvider {
    public static final TagKey<Item> ENCANTAVEL_SAQUEIO = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "enchantable/looting"));

    public ProvedorTagsItens(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, AelysiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.PICKAXES)
                .add(ModItens.CIANE_PICARETA.get());
        tag(ItemTags.AXES)
                .add(ModItens.CIANE_MACHADO.get());
        tag(ItemTags.SHOVELS)
                .add(ModItens.CIANE_PA.get());
        tag(ItemTags.HOES)
                .add(ModItens.CIANE_ENXADA.get());
        tag(ItemTags.SWORDS)
                .add(ModItens.CIANE_ESPADA.get())
                .add(ModItens.ESPADA_ESPECIAL.get())
                .add(ModItens.PARAR.get())
                .add(ModItens.CANO.get())
                .add(ModItens.EXPERIMENTO_646.get())
                .add(ModItens.PROTOCOLO_GENESIS.get())
                .add(ModItens.TRIFORME.get())
                .add(ModItens.ULTIMO_SUSPIRO.get())
                .add(ModItens.SLYNDOR_SYLVAN.get())
                .add(ModItens.AMBROSE.get())
                .add(ModItens.PURPLE_MOON.get());
        tag(ItemTags.HEAD_ARMOR)
                .add(ModItens.CIANE_CAPACETE.get());
        tag(ItemTags.CHEST_ARMOR)
                .add(ModItens.CIANE_PEITORAL.get());
        tag(ItemTags.LEG_ARMOR)
                .add(ModItens.CIANE_CALCINHA.get());
        tag(ItemTags.FOOT_ARMOR)
                .add(ModItens.CIANE_BOTAS.get());

        tag(ItemTags.TRIDENT_ENCHANTABLE)
                .add(ModItens.TRIDENTE_CORAL.get());

        tag(ENCANTAVEL_SAQUEIO)
                .addTag(ItemTags.SWORD_ENCHANTABLE)
                .add(ModItens.TRIDENTE_CORAL.get());

        tag(ItemTags.DURABILITY_ENCHANTABLE)
                .add(ModItens.ESCUDO_ABISMO.get())
                .add(ModItens.TRIDENTE_CORAL.get())
                .add(ModItens.CIANE_ESPADA.get())
                .add(ModItens.CIANE_PICARETA.get())
                .add(ModItens.CIANE_MACHADO.get())
                .add(ModItens.CIANE_PA.get())
                .add(ModItens.CIANE_ENXADA.get())
                .add(ModItens.CIANE_CAPACETE.get())
                .add(ModItens.CIANE_PEITORAL.get())
                .add(ModItens.CIANE_CALCINHA.get())
                .add(ModItens.CIANE_BOTAS.get());

        tag(ItemTags.VANISHING_ENCHANTABLE)
                .add(ModItens.CIANE_ESPADA.get())
                .add(ModItens.CIANE_PICARETA.get())
                .add(ModItens.CIANE_MACHADO.get())
                .add(ModItens.CIANE_PA.get())
                .add(ModItens.CIANE_ENXADA.get())
                .add(ModItens.CIANE_CAPACETE.get())
                .add(ModItens.CIANE_PEITORAL.get())
                .add(ModItens.CIANE_CALCINHA.get())
                .add(ModItens.CIANE_BOTAS.get());

        tag(ItemTags.EQUIPPABLE_ENCHANTABLE)
                .add(ModItens.CIANE_CAPACETE.get())
                .add(ModItens.CIANE_PEITORAL.get())
                .add(ModItens.CIANE_CALCINHA.get())
                .add(ModItens.CIANE_BOTAS.get());

        tag(ItemTags.SWORD_ENCHANTABLE)
                .add(ModItens.CIANE_ESPADA.get())
                .add(ModItens.EXPERIMENTO_646.get())
                .add(ModItens.PROTOCOLO_GENESIS.get())
                .add(ModItens.TRIFORME.get())
                .add(ModItens.ULTIMO_SUSPIRO.get())
                .add(ModItens.SLYNDOR_SYLVAN.get())
                .add(ModItens.AMBROSE.get())
                .add(ModItens.PURPLE_MOON.get());

        tag(ItemTags.SHARP_WEAPON_ENCHANTABLE)
                .add(ModItens.CIANE_ESPADA.get())
                .add(ModItens.CIANE_MACHADO.get())
                .add(ModItens.EXPERIMENTO_646.get())
                .add(ModItens.PROTOCOLO_GENESIS.get())
                .add(ModItens.TRIFORME.get())
                .add(ModItens.ULTIMO_SUSPIRO.get())
                .add(ModItens.SLYNDOR_SYLVAN.get())
                .add(ModItens.AMBROSE.get())
                .add(ModItens.PURPLE_MOON.get());

        tag(ItemTags.WEAPON_ENCHANTABLE)
                .add(ModItens.CIANE_ESPADA.get())
                .add(ModItens.CIANE_MACHADO.get())
                .add(ModItens.EXPERIMENTO_646.get())
                .add(ModItens.PROTOCOLO_GENESIS.get())
                .add(ModItens.TRIFORME.get())
                .add(ModItens.ULTIMO_SUSPIRO.get())
                .add(ModItens.SLYNDOR_SYLVAN.get())
                .add(ModItens.AMBROSE.get())
                .add(ModItens.PURPLE_MOON.get());

        tag(ItemTags.FIRE_ASPECT_ENCHANTABLE)
                .add(ModItens.CIANE_ESPADA.get())
                .add(ModItens.EXPERIMENTO_646.get())
                .add(ModItens.PROTOCOLO_GENESIS.get())
                .add(ModItens.TRIFORME.get())
                .add(ModItens.ULTIMO_SUSPIRO.get())
                .add(ModItens.SLYNDOR_SYLVAN.get())
                .add(ModItens.AMBROSE.get())
                .add(ModItens.PURPLE_MOON.get());

        tag(ItemTags.MINING_ENCHANTABLE)
                .add(ModItens.CIANE_PICARETA.get())
                .add(ModItens.CIANE_MACHADO.get())
                .add(ModItens.CIANE_PA.get())
                .add(ModItens.CIANE_ENXADA.get());

        tag(ItemTags.MINING_LOOT_ENCHANTABLE)
                .add(ModItens.CIANE_PICARETA.get())
                .add(ModItens.CIANE_PA.get())
                .add(ModItens.CIANE_MACHADO.get());

        tag(ItemTags.ARMOR_ENCHANTABLE)
                .add(ModItens.CIANE_CAPACETE.get())
                .add(ModItens.CIANE_PEITORAL.get())
                .add(ModItens.CIANE_CALCINHA.get())
                .add(ModItens.CIANE_BOTAS.get());

        tag(ItemTags.HEAD_ARMOR_ENCHANTABLE)
                .add(ModItens.CIANE_CAPACETE.get());

        tag(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                .add(ModItens.CIANE_PEITORAL.get());

        tag(ItemTags.LEG_ARMOR_ENCHANTABLE)
                .add(ModItens.CIANE_CALCINHA.get());

        tag(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                .add(ModItens.CIANE_BOTAS.get());
    }
}