package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

public class AplicadorDificuldade {

    public static final String CHAVE_NIVEL = "AelysiumDificuldade";

    private static final Item[][] ARMADURAS = {
            {Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS},
            {Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS},
            {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS}
    };

    private static final Item[] ARMADURAS_DIAMANTE = {
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS
    };

    private static final EquipmentSlot[] SLOTS_ARMADURA = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static boolean elegivel(Mob mob) {
        return mob instanceof Enemy
                && !ConfiguracaoDificuldade.entidadeExcluida(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()))
                && !(mob instanceof NeutralMob)
                && !(mob instanceof Warden)
                && !mob.getType().is(Tags.EntityTypes.BOSSES);
    }

    public static boolean marcado(Mob mob) {
        return mob.getPersistentData().contains(CHAVE_NIVEL);
    }

    public static int nivelAplicado(Mob mob) {
        CompoundTag dados = mob.getPersistentData();
        return dados.contains(CHAVE_NIVEL) ? dados.getInt(CHAVE_NIVEL) : 0;
    }

    public static void aplicar(Mob mob, int nivel) {
        mob.getPersistentData().putInt(CHAVE_NIVEL, nivel);

        if (mob instanceof Creeper creeper) tornarEletrico(creeper, nivel);

        adicionar(mob, Attributes.MAX_HEALTH, "dificuldade_vida", PerfilDificuldade.bonusVida(nivel), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        adicionar(mob, Attributes.ATTACK_DAMAGE, "dificuldade_dano", PerfilDificuldade.bonusDano(nivel), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        adicionar(mob, Attributes.MOVEMENT_SPEED, "dificuldade_velocidade", PerfilDificuldade.bonusVelocidade(nivel), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        adicionar(mob, Attributes.FOLLOW_RANGE, "dificuldade_visao", PerfilDificuldade.bonusAlcanceVisao(nivel), AttributeModifier.Operation.ADD_VALUE);
        adicionar(mob, Attributes.KNOCKBACK_RESISTANCE, "dificuldade_empurrao", PerfilDificuldade.bonusResistenciaEmpurrao(nivel), AttributeModifier.Operation.ADD_VALUE);
        adicionar(mob, Attributes.ARMOR, "dificuldade_armadura", PerfilDificuldade.bonusArmadura(nivel), AttributeModifier.Operation.ADD_VALUE);

        if (mob instanceof Zombie) {
            adicionar(mob, Attributes.SPAWN_REINFORCEMENTS_CHANCE, "dificuldade_reforcos", PerfilDificuldade.bonusReforcosZumbi(nivel), AttributeModifier.Operation.ADD_VALUE);
        }

        if (usaPerfilDeZumbi(mob)) {
            equiparZumbi(mob, nivel);
        }

        if (mob instanceof Spider) {
            adicionar(mob, Attributes.SCALE, "dificuldade_escala", PerfilDificuldade.escalaAranha(nivel), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            adicionar(mob, Attributes.MOVEMENT_SPEED, "dificuldade_velocidade_aranha", PerfilDificuldade.bonusVelocidadeAranha(nivel), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }

        if (mob instanceof Slime) {
            adicionar(mob, Attributes.MOVEMENT_SPEED, "dificuldade_velocidade_slime", PerfilDificuldade.bonusVelocidadeSlime(nivel), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }

        mob.setHealth(mob.getMaxHealth());
        aplicarComportamentos(mob, nivel);
    }

    private static boolean usaPerfilDeZumbi(Mob mob) {
        if (mob instanceof Zombie) return true;
        if (ConfiguracaoDificuldade.humanoideConfigurado(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()))) return true;
        if (!PerfilDificuldade.armaduraParaHumanoidesGenericos()) return false;
        if (mob instanceof Creeper || mob instanceof Spider || mob instanceof Slime
                || mob instanceof AbstractSkeleton || mob instanceof Phantom) return false;
        if (!(mob.getNavigation() instanceof GroundPathNavigation) || mob.isNoGravity()) return false;
        return mob.getBbWidth() <= 1.0f && mob.getBbHeight() >= 1.4f && mob.getBbHeight() <= 2.6f;
    }

    public static void bloquearDropsEquipamento(Mob mob) {
        if (!PerfilDificuldade.bloquearDropEquipamento()) return;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mob.setDropChance(slot, 0.0f);
        }
    }

    public static void aplicarComportamentos(Mob mob, int nivel) {
        bloquearDropsEquipamento(mob);

        if (mob instanceof Slime) {
            AttributeInstance pulo = mob.getAttribute(Attributes.JUMP_STRENGTH);
            if (pulo != null) pulo.removeModifier(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "dificuldade_pulo"));
        }

        if (mob instanceof PathfinderMob && mob instanceof RangedAttackMob
                && ConfiguracaoDificuldade.bruxaConfigurada(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()))) {
            substituirObjetivosDaBruxa((PathfinderMob & RangedAttackMob) mob, nivel);
        }

        if (mob instanceof AbstractSkeleton esqueleto) {
            for (WrappedGoal objetivo : esqueleto.goalSelector.getAvailableGoals()) {
                if (objetivo.getGoal() instanceof RangedBowAttackGoal<?> arco) {
                    arco.setMinAttackInterval(PerfilDificuldade.intervaloMinimoArco(nivel));
                }
            }
        }
    }

    private static <T extends PathfinderMob & RangedAttackMob> void substituirObjetivosDaBruxa(T bruxa, int nivel) {
        List<Goal> removiveis = new ArrayList<>();
        for (WrappedGoal objetivo : bruxa.goalSelector.getAvailableGoals()) {
            Goal goal = objetivo.getGoal();
            if (goal instanceof ObjetivoBruxaTatica) return;
            if (goal instanceof RangedAttackGoal) removiveis.add(goal);
        }
        removiveis.forEach(bruxa.goalSelector::removeGoal);
        bruxa.goalSelector.addGoal(2, new ObjetivoBruxaTatica(bruxa, nivel));
    }

    private static void adicionar(Mob mob, Holder<Attribute> atributo, String id, double valor, AttributeModifier.Operation operacao) {
        AttributeInstance instancia = mob.getAttribute(atributo);
        if (instancia == null || valor == 0.0) return;
        ResourceLocation chave = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, id);
        instancia.removeModifier(chave);
        instancia.addPermanentModifier(new AttributeModifier(chave, valor, operacao));
    }

    private static void tornarEletrico(Creeper creeper, int nivel) {
        if (creeper.getRandom().nextDouble() >= PerfilDificuldade.chanceCreeperEletrico(nivel)) return;
        CompoundTag tag = new CompoundTag();
        creeper.saveWithoutId(tag);
        tag.putBoolean("powered", true);
        creeper.load(tag);
    }

    private static void equiparZumbi(Mob zumbi, int nivel) {
        RandomSource aleatorio = zumbi.getRandom();
        int tierMaximo = PerfilDificuldade.tierMaximoArmadura(nivel);
        double chancePeca = PerfilDificuldade.chanceArmaduraPeca(nivel);

        for (int i = 0; i < SLOTS_ARMADURA.length; i++) {
            EquipmentSlot slot = SLOTS_ARMADURA[i];
            if (!zumbi.getItemBySlot(slot).isEmpty()) continue;
            if (aleatorio.nextDouble() >= chancePeca) continue;

            Item item;
            if (aleatorio.nextDouble() < PerfilDificuldade.chanceDiamanteArmadura(nivel)) {
                item = ARMADURAS_DIAMANTE[i];
            } else {
                int tier = tierMaximo;
                if (tier > 0 && aleatorio.nextBoolean()) tier--;
                item = ARMADURAS[tier][i];
            }

            zumbi.setItemSlot(slot, new ItemStack(item));
            if (PerfilDificuldade.bloquearDropEquipamento()) zumbi.setDropChance(slot, 0.0f);
        }
    }
}
