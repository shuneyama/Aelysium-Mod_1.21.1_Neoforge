package net.aelysium.aelysiummod.conteudo.item.arma;

import net.aelysium.aelysiummod.conteudo.entidade.EntidadeTridenteConchas;
import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TridenteConchasItem extends TridentItem {

    private static final double RAIO = 5.0;
    private static final int INTERVALO_PULSO = 10;
    private static final int INTERVALO_ANEL = 5;
    private static final int INTERVALO_BOLHAS = 15;
    private static final int INTERVALO_CONDUIT = 40;
    private static final int PONTOS_ANEL = 32;

    private static final Variante CURA_MENOR = new Variante(6.0f, 100, 1000);
    private static final Variante CURA_MAIOR = new Variante(8.0f, 140, 1200);

    private static final Map<UUID, Ativo> ATIVOS = new ConcurrentHashMap<>();

    private record Variante(float vidaTotal, int duracao, int cooldown) {
    }

    private static final class Ativo {
        final long inicio;
        final int duracao;
        final float curaPorPulso;
        long ultimoTick = -1;

        Ativo(long inicio, int duracao, float curaPorPulso) {
            this.inicio = inicio;
            this.duracao = duracao;
            this.curaPorPulso = curaPorPulso;
        }
    }

    public TridenteConchasItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }

        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (level.getGameTime() < stack.getOrDefault(ModComponentesDados.TRIDENTE_COOLDOWN_FIM.get(), 0L)) {
            return InteractionResultHolder.fail(stack);
        }

        Variante variante = level.random.nextBoolean() ? CURA_MENOR : CURA_MAIOR;
        int pulsos = variante.duracao() / INTERVALO_PULSO;
        ATIVOS.put(player.getUUID(), new Ativo(level.getGameTime(), variante.duracao(), variante.vidaTotal() / pulsos));

        stack.set(ModComponentesDados.TRIDENTE_COOLDOWN_FIM.get(), level.getGameTime() + variante.cooldown());
        stack.set(ModComponentesDados.TRIDENTE_COOLDOWN_TOTAL.get(), (long) variante.cooldown());
        level.playSound(null, player.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.2f);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack antigo, ItemStack novo, boolean slotMudou) {
        return slotMudou || !antigo.is(novo.getItem());
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tempoRestante) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int carga = this.getUseDuration(stack, entity) - tempoRestante;
        if (carga < 10) {
            return;
        }

        float giro = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
        if (giro > 0.0F && !player.isInWaterOrRain()) {
            return;
        }
        if (danificadoDemais(stack)) {
            return;
        }

        Holder<SoundEvent> som = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND)
                .orElse(SoundEvents.TRIDENT_THROW);

        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
            if (giro == 0.0F) {
                EntidadeTridenteConchas arremessado = new EntidadeTridenteConchas(level, player, stack);
                arremessado.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                if (player.hasInfiniteMaterials()) {
                    arremessado.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }

                level.addFreshEntity(arremessado);
                level.playSound(null, arremessado, som.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!player.hasInfiniteMaterials()) {
                    player.getInventory().removeItem(stack);
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (giro > 0.0F) {
            float yaw = player.getYRot();
            float pitch = player.getXRot();
            float x = -Mth.sin(yaw * (float) (Math.PI / 180.0)) * Mth.cos(pitch * (float) (Math.PI / 180.0));
            float y = -Mth.sin(pitch * (float) (Math.PI / 180.0));
            float z = Mth.cos(yaw * (float) (Math.PI / 180.0)) * Mth.cos(pitch * (float) (Math.PI / 180.0));
            float norma = Mth.sqrt(x * x + y * y + z * z);
            player.push(x * giro / norma, y * giro / norma, z * giro / norma);
            player.startAutoSpinAttack(20, 8.0F, stack);
            if (player.onGround()) {
                player.move(MoverType.SELF, new Vec3(0.0, 1.1999999F, 0.0));
            }

            level.playSound(null, player, som.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public Projectile asProjectile(Level level, Position posicao, ItemStack stack, Direction direcao) {
        EntidadeTridenteConchas arremessado = new EntidadeTridenteConchas(level, posicao.x(), posicao.y(), posicao.z(), stack.copyWithCount(1));
        arremessado.pickup = AbstractArrow.Pickup.ALLOWED;
        return arremessado;
    }

    private static boolean danificadoDemais(ItemStack stack) {
        return stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Ativo ativo = ATIVOS.get(player.getUUID());
        if (ativo == null) {
            return;
        }

        long agora = level.getGameTime();
        long anterior = ativo.ultimoTick;
        if (anterior == agora) {
            return;
        }
        ativo.ultimoTick = agora;

        boolean segurando = player.getMainHandItem().is(this) || player.getOffhandItem().is(this);
        boolean semItem = anterior != -1 && agora - anterior > 1;
        if (!segurando || semItem) {
            ATIVOS.remove(player.getUUID());
            return;
        }

        long decorrido = agora - ativo.inicio;
        if (decorrido > ativo.duracao || !player.isAlive()) {
            ATIVOS.remove(player.getUUID());
            return;
        }

        if (decorrido % INTERVALO_BOLHAS == 0) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 0.8f, 1.0f);
        }

        if (decorrido % INTERVALO_CONDUIT == 0) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CONDUIT_AMBIENT, SoundSource.PLAYERS, 0.8f, 1.0f);
        }

        if (decorrido % INTERVALO_ANEL == 0) {
            double giro = decorrido * 0.12;
            for (int i = 0; i < PONTOS_ANEL; i++) {
                double angulo = giro + (2.0 * Math.PI * i) / PONTOS_ANEL;
                double x = player.getX() + Math.cos(angulo) * RAIO;
                double z = player.getZ() + Math.sin(angulo) * RAIO;
                serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, x, player.getY() + 0.15, z, 1, 0.0, 0.05, 0.0, 0.0);
                serverLevel.sendParticles(ParticleTypes.NAUTILUS, x, player.getY() + 0.6, z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        if (decorrido > 0 && decorrido % INTERVALO_PULSO == 0) {
            List<Player> alvos = level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(RAIO),
                    alvo -> !alvo.isSpectator() && alvo.isAlive() && alvo.distanceToSqr(player) <= RAIO * RAIO);
            for (Player alvo : alvos) {
                alvo.heal(ativo.curaPorPulso);
                serverLevel.sendParticles(ParticleTypes.HEART, alvo.getX(), alvo.getY() + 1.0, alvo.getZ(), 1, 0.4, 0.5, 0.4, 0.0);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.tridente_coral"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.tridente_coral_shift_down"));
        } super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}