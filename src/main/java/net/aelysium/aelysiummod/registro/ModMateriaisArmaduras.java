package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ModMateriaisArmaduras {
    public static final Holder<ArmorMaterial> CIANE_ARMOR_MATERIAL = register("ciane",
            Util.make(new EnumMap<>(ArmorItem.Type.class), attribute -> {
                attribute.put(ArmorItem.Type.BOOTS, 6);
                attribute.put(ArmorItem.Type.LEGGINGS, 9);
                attribute.put(ArmorItem.Type.CHESTPLATE, 9);
                attribute.put(ArmorItem.Type.HELMET, 6);
                attribute.put(ArmorItem.Type.BODY, 15);
            }), 16, 5.0f, 0.2f, () -> ModItens.CIANE_BARRA.get());

    public static final Holder<ArmorMaterial> CUECA_PREFEITO_ARMOR_MATERIAL = register("cueca_prefeito",
            Util.make(new EnumMap<>(ArmorItem.Type.class), attribute -> {
                attribute.put(ArmorItem.Type.BOOTS, 0);
                attribute.put(ArmorItem.Type.LEGGINGS, 1);
                attribute.put(ArmorItem.Type.CHESTPLATE, 0);
                attribute.put(ArmorItem.Type.HELMET, 0);
                attribute.put(ArmorItem.Type.BODY, 0);
            }), 15, 0.0f, 0.0f, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Items.STRING);


    private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> typeProtection,
                                                  int enchantability, float toughness, float knockbackResistance,
                                                  Supplier<Item> ingredientItem) {
        return register(name, typeProtection, enchantability, toughness, knockbackResistance, SoundEvents.ARMOR_EQUIP_NETHERITE, ingredientItem);
    }

    private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> typeProtection,
                                                  int enchantability, float toughness, float knockbackResistance,
                                                  Holder<SoundEvent> equipSound, Supplier<Item> ingredientItem) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, name);
        Supplier<Ingredient> ingredient = () -> Ingredient.of(ingredientItem.get());
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(location));

        EnumMap<ArmorItem.Type, Integer> typeMap = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            typeMap.put(type, typeProtection.get(type));
        }

        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, location,
                new ArmorMaterial(typeProtection, enchantability, equipSound, ingredient, layers, toughness, knockbackResistance));
    }
}