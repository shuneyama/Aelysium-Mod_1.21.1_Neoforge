package net.aelysium.aelysiummod.conteudo.item.cigarro;

import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;

public class CigarroItem extends Item {

    private static final int TICKS_PARA_ACENDER = 20;
    private static final int TICKS_PARA_FUMAR = 60;
    private static final int USE_DURATION = 100;
    private static final int COOLDOWN_TICKS = 20;
    static final int MAX_FUMADAS = 10;
    private static final int EFEITO_FUMADA_DURATION_TICKS = 200;
    private static final int NIVEL_MAX_VIGOR = 5;
    private static final int JANELA_ABSTINENCIA_TICKS = 1200;
    private static final int FUMADAS_PARA_ABSTINENCIA = 5;
    private static final int ABSTINENCIA_DURATION_TICKS = 6000;
    private static final int ALIVIO_DURATION_TICKS = 200;
    public static final String ABSTINENCIA_MAX_HEALTH_ID = "cigarro_abstinencia_max_health";

    private static final java.util.Map<java.util.UUID, java.util.Deque<Long>> HISTORICO_FUMADAS = new java.util.HashMap<>();

    public CigarroItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        boolean aceso = stack.getOrDefault(ModComponentesDados.CIGARRO_ACESO, false);
        return aceso ? UseAnim.TOOT_HORN : UseAnim.NONE;
    }

    @Override
    public net.minecraft.network.chat.Component getName(ItemStack stack) {
        boolean aceso = stack.getOrDefault(ModComponentesDados.CIGARRO_ACESO, false);
        if (aceso) {
            return net.minecraft.network.chat.Component.translatable("item.aelysiummod.cigarro_aceso");
        }
        return super.getName(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        boolean aceso = stack.getOrDefault(ModComponentesDados.CIGARRO_ACESO, false);
        int fumadas = stack.getOrDefault(ModComponentesDados.CIGARRO_FUMADAS, 0);
        return aceso && fumadas < MAX_FUMADAS;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int fumadas = stack.getOrDefault(ModComponentesDados.CIGARRO_FUMADAS, 0);
        return Math.round(13.0F * (1.0F - (float) fumadas / MAX_FUMADAS));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int fumadas = stack.getOrDefault(ModComponentesDados.CIGARRO_FUMADAS, 0);
        float f = Math.max(0.0F, (MAX_FUMADAS - fumadas) / (float) MAX_FUMADAS);
        return net.minecraft.util.Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        boolean aceso = stack.getOrDefault(ModComponentesDados.CIGARRO_ACESO, false);

        if (aceso) {
            int fumadas = stack.getOrDefault(ModComponentesDados.CIGARRO_FUMADAS, 0);
            if (fumadas >= MAX_FUMADAS) {
                return InteractionResultHolder.pass(stack);
            }
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        InteractionHand outraMao = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack outraStack = player.getItemInHand(outraMao);
        if (outraStack.getItem() instanceof IsqueiroItem
                && outraStack.getOrDefault(ModComponentesDados.ISQUEIRO_ACESO, false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        int decorridos = getUseDuration(stack, livingEntity) - remainingUseDuration;
        boolean aceso = stack.getOrDefault(ModComponentesDados.CIGARRO_ACESO, false);

        if (!aceso) {
            if (decorridos < TICKS_PARA_ACENDER) return;

            InteractionHand mao = livingEntity.getUsedItemHand();
            InteractionHand outraMao = mao == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack outraStack = livingEntity.getItemInHand(outraMao);
            if (!(outraStack.getItem() instanceof IsqueiroItem)
                    || !outraStack.getOrDefault(ModComponentesDados.ISQUEIRO_ACESO, false)) {
                livingEntity.stopUsingItem();
                return;
            }

            stack.set(ModComponentesDados.CIGARRO_ACESO, true);
            level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                    SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.7f, 1.0f);

            livingEntity.stopUsingItem();
            aplicarCooldown(livingEntity);
            if (livingEntity instanceof Player player) {
                player.getCooldowns().addCooldown(outraStack.getItem(), COOLDOWN_TICKS);
            }
            return;
        }

        if (decorridos < TICKS_PARA_FUMAR) return;

        int fumadas = stack.getOrDefault(ModComponentesDados.CIGARRO_FUMADAS, 0) + 1;
        stack.set(ModComponentesDados.CIGARRO_FUMADAS, fumadas);

        if (livingEntity instanceof Player player) {
            fumar(player, fumadas, level, livingEntity);
            if (fumadas >= MAX_FUMADAS) {
                quebrarCigarro(stack, livingEntity, level);
            }
        }

        livingEntity.stopUsingItem();
        aplicarCooldown(livingEntity);
    }

    private void fumar(Player player, int fumadas, Level level, LivingEntity livingEntity) {
        if (!player.hasEffect(ModEfeitos.CIGARRO_ABSTINENCIA)) {
            int coracoesAmplifier = proximoAmplifier(player, ModEfeitos.CIGARRO_CORACOES, NIVEL_MAX_VIGOR - 1);
            player.addEffect(new MobEffectInstance(ModEfeitos.CIGARRO_CORACOES, EFEITO_FUMADA_DURATION_TICKS,
                    coracoesAmplifier, false, true, true));
            player.heal(2.0F);

            int danoAmplifier = proximoAmplifier(player, ModEfeitos.CIGARRO_DANO, Integer.MAX_VALUE);
            player.addEffect(new MobEffectInstance(ModEfeitos.CIGARRO_DANO, EFEITO_FUMADA_DURATION_TICKS,
                    danoAmplifier, false, false, true));
        }

        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.BREEZE_INHALE, SoundSource.PLAYERS, 0.6f, 0.7f);

        exalarFumaca(livingEntity, level);

        registrarFumadaEVerificarAbstinencia(player, level);
    }

    private int proximoAmplifier(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> efeito, int amplifierMaximo) {
        MobEffectInstance atual = player.getEffect(efeito);
        int amplifier = atual != null ? atual.getAmplifier() + 1 : 0;
        return Math.min(amplifier, amplifierMaximo);
    }

    private void registrarFumadaEVerificarAbstinencia(Player player, Level level) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel)) return;

        if (player.hasEffect(ModEfeitos.CIGARRO_ABSTINENCIA)) {
            player.addEffect(new MobEffectInstance(ModEfeitos.CIGARRO_ALIVIO, ALIVIO_DURATION_TICKS, 0, false, true, true));
            removerClampAbstinencia(player);
            player.setHealth(player.getMaxHealth());
            return;
        }

        long agora = level.getGameTime();
        java.util.Deque<Long> historico = HISTORICO_FUMADAS.computeIfAbsent(player.getUUID(), id -> new java.util.ArrayDeque<>());
        historico.addLast(agora);
        while (!historico.isEmpty() && agora - historico.peekFirst() > JANELA_ABSTINENCIA_TICKS) {
            historico.pollFirst();
        }

        if (historico.size() >= FUMADAS_PARA_ABSTINENCIA) {
            historico.clear();
            player.addEffect(new MobEffectInstance(ModEfeitos.CIGARRO_ABSTINENCIA, ABSTINENCIA_DURATION_TICKS, 0, false, true, true));
            aplicarClampAbstinencia(player);
        }
    }

    public static void aplicarClampAbstinencia(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;

        double atual = maxHealth.getValue();
        if (atual <= 0) return;

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, ABSTINENCIA_MAX_HEALTH_ID);
        maxHealth.removeModifier(id);
        double fator = (2.0 / atual) - 1.0;
        maxHealth.addPermanentModifier(new AttributeModifier(id, fator, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        player.setHealth(2.0F);
    }

    public static void removerClampAbstinencia(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;

        maxHealth.removeModifier(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, ABSTINENCIA_MAX_HEALTH_ID));
    }

    static void exalarFumaca(LivingEntity livingEntity, Level level) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        double yawRad = Math.toRadians(livingEntity.getYHeadRot());
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        double x = livingEntity.getX() + dirX * 0.3;
        double y = livingEntity.getEyeY() - 0.1;
        double z = livingEntity.getZ() + dirZ * 0.3;

        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 7,
                dirX * 0.15, 0.08, dirZ * 0.15, 0.03);
    }

    static void exalarFumacaPequena(LivingEntity livingEntity, Level level) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        double yawRad = Math.toRadians(livingEntity.getYHeadRot());
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        double x = livingEntity.getX() + dirX * 0.3;
        double y = livingEntity.getEyeY() - 0.1;
        double z = livingEntity.getZ() + dirZ * 0.3;

        serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 2,
                dirX * 0.08, 0.03, dirZ * 0.08, 0.015);
    }

    private void quebrarCigarro(ItemStack stack, LivingEntity livingEntity, Level level) {
        stack.shrink(1);

        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.7f, 1.0f);

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    livingEntity.getX(), livingEntity.getEyeY(), livingEntity.getZ(),
                    12, 0.2, 0.2, 0.2, 0.02);
        }
    }

    private void aplicarCooldown(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide()) return;
        if (!(entity instanceof LivingEntity livingEntity)) return;

        boolean aceso = stack.getOrDefault(ModComponentesDados.CIGARRO_ACESO, false);
        int fumadas = stack.getOrDefault(ModComponentesDados.CIGARRO_FUMADAS, 0);
        if (!aceso || fumadas >= MAX_FUMADAS) return;

        boolean mainHand = livingEntity.getMainHandItem() == stack;
        boolean offHand = livingEntity.getOffhandItem() == stack;
        if (!mainHand && !offHand) return;

        if (level.random.nextInt(20) != 0) return;

        net.minecraft.world.entity.HumanoidArm braco = mainHand ? livingEntity.getMainArm() : livingEntity.getMainArm().getOpposite();
        double lado = braco == net.minecraft.world.entity.HumanoidArm.RIGHT ? -1.0 : 1.0;

        double yawRad = Math.toRadians(livingEntity.getYRot());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = forwardZ;
        double rightZ = -forwardX;

        double x = livingEntity.getX() + forwardX * 0.35 + rightX * lado * 0.4
                + (level.random.nextDouble() - 0.5) * 0.05;
        double y = livingEntity.getY() + livingEntity.getBbHeight() * 0.6
                + (level.random.nextDouble() - 0.5) * 0.05;
        double z = livingEntity.getZ() + forwardZ * 0.35 + rightZ * lado * 0.4
                + (level.random.nextDouble() - 0.5) * 0.05;

        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.01, 0.0);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        return true;
    }
}
