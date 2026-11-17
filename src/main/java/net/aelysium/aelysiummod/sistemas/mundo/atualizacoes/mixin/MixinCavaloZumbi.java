package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieHorse.class)
public abstract class MixinCavaloZumbi extends AbstractHorse {

    protected MixinCavaloZumbi(EntityType<? extends AbstractHorse> tipo, Level mundo) {
        super(tipo, mundo);
    }

    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void aelysium$vida(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(Attributes.MAX_HEALTH, 25.0);
    }

    @Inject(method = "checkZombieHorseSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void aelysium$spawn(EntityType<? extends Animal> tipo, LevelAccessor mundo, MobSpawnType motivo, BlockPos pos, RandomSource aleatorio, CallbackInfoReturnable<Boolean> cir) {
        if (MobSpawnType.isSpawner(motivo)) return;
        boolean escuro = mundo instanceof ServerLevelAccessor servidor && Monster.isDarkEnoughToSpawn(servidor, pos, aleatorio);
        cir.setReturnValue(mundo.getDifficulty() != Difficulty.PEACEFUL && escuro && Mob.checkMobSpawnRules(tipo, mundo, motivo, pos, aleatorio));
    }

    @Inject(method = "randomizeAttributes", at = @At("HEAD"), cancellable = true)
    private void aelysium$atributos(RandomSource aleatorio, CallbackInfo ci) {
        double pulo = 0.5 + aleatorio.nextDouble() * (1.0 / 15.0) + aleatorio.nextDouble() * (1.0 / 15.0) + aleatorio.nextDouble() * (1.0 / 15.0);
        double velocidade = (9.0 + aleatorio.nextDouble() + aleatorio.nextDouble() + aleatorio.nextDouble()) / 42.16;
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(pulo);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(velocidade);
        ci.cancel();
    }

    @Inject(method = "addBehaviourGoals", at = @At("HEAD"))
    private void aelysium$metas(CallbackInfo ci) {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.RED_MUSHROOM), false));
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void aelysium$interagir(Player jogador, InteractionHand mao, CallbackInfoReturnable<InteractionResult> cir) {
        this.setPersistenceRequired();
        boolean abrirInventario = !this.isBaby() && this.isTamed() && jogador.isSecondaryUseActive();
        if (this.isVehicle() || abrirInventario) {
            cir.setReturnValue(super.mobInteract(jogador, mao));
            return;
        }
        ItemStack item = jogador.getItemInHand(mao);
        if (!item.isEmpty()) {
            if (this.isFood(item)) {
                cir.setReturnValue(this.fedFood(jogador, item));
                return;
            }
            if (!this.isTamed()) {
                this.makeMad();
                cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
                return;
            }
        }
        cir.setReturnValue(super.mobInteract(jogador, mao));
    }

    @Override
    public boolean isFood(ItemStack item) {
        return item.is(Items.RED_MUSHROOM);
    }

    @Override
    protected boolean handleEating(Player jogador, ItemStack item) {
        if (!item.is(Items.RED_MUSHROOM)) return super.handleEating(jogador, item);
        boolean usado = false;
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(3.0F);
            usado = true;
        }
        if ((usado || !this.isTamed()) && this.getTemper() < this.getMaxTemper() && !this.level().isClientSide) {
            this.modifyTemper(3);
            usado = true;
        }
        if (usado) {
            this.eating();
            this.gameEvent(GameEvent.EAT);
        }
        return usado;
    }

    @Override
    public boolean removeWhenFarAway(double distancia) {
        return true;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot espaco) {
        return true;
    }

    @Override
    public boolean isBodyArmorItem(ItemStack item) {
        return item.getItem() instanceof AnimalArmorItem armadura && armadura.getBodyType() == AnimalArmorItem.BodyType.EQUESTRIAN;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && this.isSunBurnTick()) {
            ItemStack protecao = this.getItemBySlot(EquipmentSlot.BODY);
            if (protecao.isEmpty()) {
                this.igniteForSeconds(8.0F);
            } else if (protecao.isDamageableItem()) {
                protecao.hurtAndBreak(this.random.nextInt(2), this, EquipmentSlot.BODY);
            }
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor mundo, DifficultyInstance dificuldade, MobSpawnType motivo, @Nullable SpawnGroupData dados) {
        if (motivo == MobSpawnType.NATURAL) {
            Zombie zumbi = EntityType.ZOMBIE.create(this.level());
            if (zumbi != null) {
                zumbi.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                zumbi.finalizeSpawn(mundo, dificuldade, MobSpawnType.JOCKEY, null);
                zumbi.startRiding(this);
            }
        }
        return super.finalizeSpawn(mundo, dificuldade, motivo, dados);
    }
}
