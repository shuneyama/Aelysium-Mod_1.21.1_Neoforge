package net.aelysium.aelysiummod.raca;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class RaceManager {

    private static final ResourceLocation MOD_HEALTH       = rl("race_max_health");
    private static final ResourceLocation MOD_ARMOR        = rl("race_armor");
    private static final ResourceLocation MOD_ARMOR_TOUGH  = rl("race_armor_toughness");
    private static final ResourceLocation MOD_ATTACK       = rl("race_attack_damage");
    private static final ResourceLocation MOD_ATTACK_SPEED = rl("race_attack_speed");
    private static final ResourceLocation MOD_SPEED        = rl("race_movement_speed");
    private static final ResourceLocation MOD_LUCK         = rl("race_luck");
    private static final ResourceLocation MOD_FALL         = rl("race_fall_damage");
    private static final ResourceLocation MOD_BURN         = rl("race_burning_time");
    private static final ResourceLocation MOD_SWIM         = rl("race_swim_speed");
    private static final ResourceLocation MOD_SUBMERGED    = rl("race_submerged_mining");
    private static final ResourceLocation MOD_SPELL_POWER  = rl("race_spell_power");
    private static final ResourceLocation MOD_MANA_REGEN   = rl("race_mana_regen");
    private static final ResourceLocation MOD_XP           = rl("race_xp_bonus");

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("aelysiummod", path);
    }

    public static void applyRace(ServerPlayer player, RaceType race) {
        removeAllModifiers(player);

        if (race != RaceType.VALKYRIA) {
            resetFlight(player);
        }

        switch (race) {
            case HUMANO -> {
                addMod(player, Attributes.MAX_HEALTH, MOD_HEALTH, 4.0);
                addMod(player, Attributes.ARMOR_TOUGHNESS, MOD_ARMOR_TOUGH, 4.0);
                addMod(player, Attributes.LUCK, MOD_LUCK, 3.0);
                addMod(player, Attributes.ATTACK_SPEED, MOD_ATTACK_SPEED, 0.5);
                addModSoft(player, "irons_spellbooks:spell_power", MOD_SPELL_POWER, -2.0);
                addModSoft(player, "apothic_attributes:experience_gained", MOD_XP, 1.0);
            }
            case ELVARIN -> {
                addModSoft(player, "irons_spellbooks:spell_power", MOD_SPELL_POWER, 2.0);
                addModSoft(player, "irons_spellbooks:mana_regen", MOD_MANA_REGEN, 0.5);
            }
            case DRACONO -> {
                addMod(player, Attributes.MAX_HEALTH, MOD_HEALTH, 4.0);
                addMod(player, Attributes.ARMOR, MOD_ARMOR, 2.0);
                addMod(player, Attributes.ATTACK_SPEED, MOD_ATTACK_SPEED, 0.5);
                addModMul(player, "minecraft:generic.fall_damage_multiplier", MOD_FALL, -1.0);
            }
            case TIEFLING -> {
                addMod(player, Attributes.MAX_HEALTH, MOD_HEALTH, -4.0);
                addMod(player, Attributes.ARMOR, MOD_ARMOR, 2.0);
                addModSoft(player, "irons_spellbooks:spell_power", MOD_SPELL_POWER, 2.0);
                addModSoft(player, "irons_spellbooks:mana_regen", MOD_MANA_REGEN, 0.5);
                addModMul(player, "minecraft:generic.burning_time", MOD_BURN, -0.5);
            }
            case UNDYNE -> {
                addMod(player, Attributes.MAX_HEALTH, MOD_HEALTH, -4.0);
                addMod(player, Attributes.ARMOR, MOD_ARMOR, 2.0);
                addMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK, 2.0);
                addMod(player, Attributes.ATTACK_SPEED, MOD_ATTACK_SPEED, 0.5);
                addModSoft(player, "neoforge:swim_speed", MOD_SWIM, 1.0);
                addModSoft(player, "minecraft:player.submerged_mining_speed", MOD_SUBMERGED, 0.8);
            }
            case VALKYRIA -> {
                addMod(player, Attributes.MAX_HEALTH, MOD_HEALTH, -4.0);
                addMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK, 4.0);
                resetFlight(player);
            }
            case ROBO -> {
                addMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK, 9.0);
            }
            case NONE -> {}
        }

        float maxHp = (float) player.getMaxHealth();
        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }
    }

    public static void applyUndyneWaterBonus(ServerPlayer player) {
        addMod(player, Attributes.MAX_HEALTH, MOD_HEALTH, 0.0);
        addMod(player, Attributes.ARMOR, MOD_ARMOR, 4.0);
        addMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK, 0.0);
        addModSoft(player, "irons_spellbooks:spell_power", MOD_SPELL_POWER, 2.0);

        float maxHp = (float) player.getMaxHealth();
        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }
    }

    public static void applyUndyneLandStats(ServerPlayer player) {
        addMod(player, Attributes.MAX_HEALTH, MOD_HEALTH, -4.0);
        addMod(player, Attributes.ARMOR, MOD_ARMOR, 2.0);
        addMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK, 2.0);
        removeSoft(player, "irons_spellbooks:spell_power", MOD_SPELL_POWER);

        float maxHp = (float) player.getMaxHealth();
        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }
    }

    public static void removeAllModifiers(ServerPlayer player) {
        removeMod(player, Attributes.MAX_HEALTH, MOD_HEALTH);
        removeMod(player, Attributes.ARMOR, MOD_ARMOR);
        removeMod(player, Attributes.ARMOR_TOUGHNESS, MOD_ARMOR_TOUGH);
        removeMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK);
        removeMod(player, Attributes.ATTACK_SPEED, MOD_ATTACK_SPEED);
        removeMod(player, Attributes.MOVEMENT_SPEED, MOD_SPEED);
        removeMod(player, Attributes.LUCK, MOD_LUCK);

        removeSoftMul(player, "minecraft:generic.fall_damage_multiplier", MOD_FALL);
        removeSoftMul(player, "minecraft:generic.burning_time", MOD_BURN);
        removeSoft(player, "neoforge:swim_speed", MOD_SWIM);
        removeSoft(player, "minecraft:player.submerged_mining_speed", MOD_SUBMERGED);

        removeSoft(player, "irons_spellbooks:spell_power", MOD_SPELL_POWER);
        removeSoft(player, "irons_spellbooks:mana_regen", MOD_MANA_REGEN);
        removeSoft(player, "apothic_attributes:experience_gained", MOD_XP);
    }

    private static void addMod(ServerPlayer player, Holder<Attribute> attribute,
                               ResourceLocation modId, double amount) {
        AttributeInstance attr = player.getAttribute(attribute);
        if (attr == null) return;
        attr.removeModifier(modId);
        attr.addPermanentModifier(new AttributeModifier(modId, amount, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void addModSoft(ServerPlayer player, String attributeId,
                                   ResourceLocation modId, double amount) {
        Holder<Attribute> holder = getHolder(attributeId);
        if (holder == null || !player.getAttributes().hasAttribute(holder)) return;
        AttributeInstance attr = player.getAttribute(holder);
        if (attr == null) return;
        attr.removeModifier(modId);
        attr.addPermanentModifier(new AttributeModifier(modId, amount, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void addModMul(ServerPlayer player, String attributeId,
                                  ResourceLocation modId, double amount) {
        Holder<Attribute> holder = getHolder(attributeId);
        if (holder == null || !player.getAttributes().hasAttribute(holder)) return;
        AttributeInstance attr = player.getAttribute(holder);
        if (attr == null) return;
        attr.removeModifier(modId);
        attr.addPermanentModifier(new AttributeModifier(modId, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void removeMod(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation modId) {
        AttributeInstance attr = player.getAttribute(attribute);
        if (attr != null) attr.removeModifier(modId);
    }

    private static void removeSoft(ServerPlayer player, String attributeId, ResourceLocation modId) {
        Holder<Attribute> holder = getHolder(attributeId);
        if (holder == null || !player.getAttributes().hasAttribute(holder)) return;
        AttributeInstance attr = player.getAttribute(holder);
        if (attr != null) attr.removeModifier(modId);
    }

    private static void removeSoftMul(ServerPlayer player, String attributeId, ResourceLocation modId) {
        removeSoft(player, attributeId, modId);
    }

    public static void resetFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        ValkyriaFlightManager.clearData(player.getUUID());
    }

    public static Holder<Attribute> getHolder(String id) {
        try {
            return BuiltInRegistries.ATTRIBUTE
                    .getHolder(ResourceLocation.parse(id))
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}