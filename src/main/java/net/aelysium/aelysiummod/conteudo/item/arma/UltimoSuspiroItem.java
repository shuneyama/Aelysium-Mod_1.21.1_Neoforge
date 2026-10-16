package net.aelysium.aelysiummod.conteudo.item.arma;

import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.aelysium.aelysiummod.registro.ModParticulas;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class UltimoSuspiroItem extends SwordItem {

    public static final int CARGAS_MAX = 3;
    public static final int TICKS_CARGA = 20;
    public static final int TICKS_RECARGA = 1200;
    public static final int TICKS_COOLDOWN = 2400;
    public static final int TICKS_ARMADURA = 60;

    public static final int SANGUE_VIDA = 4;
    public static final int SANGUE_ATRASO_MAX = 3;

    private static final float CUSTO_SATURACAO = 4.0F;
    private static final float DANO_ALVO = 2.0F;
    private static final double ALCANCE =4.0;
    private static final double LARGURA = 2.0;
    private static final int PARTICULAS = 110;
    private static final double INICIO_MAX = 1.5;

    public record Estado(int cargas, long recargaFim, long cooldownFim) {
    }

    public UltimoSuspiroItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public static Estado estado(ItemStack stack, long agora) {
        int cargas = stack.getOrDefault(ModComponentesDados.SABRE_CARGAS.get(), CARGAS_MAX);
        long recarga = stack.getOrDefault(ModComponentesDados.SABRE_RECARGA_FIM.get(), 0L);
        long cooldown = stack.getOrDefault(ModComponentesDados.SABRE_COOLDOWN_FIM.get(), 0L);

        if (cooldown > 0) {
            if (agora >= cooldown) {
                return new Estado(CARGAS_MAX, 0L, 0L);
            }
            return new Estado(0, 0L, cooldown);
        }

        if (cargas < CARGAS_MAX && recarga > 0) {
            while (cargas < CARGAS_MAX && agora >= recarga) {
                cargas++;
                recarga += TICKS_RECARGA;
            }
        }
        if (cargas >= CARGAS_MAX) {
            return new Estado(CARGAS_MAX, 0L, 0L);
        }
        return new Estado(cargas, recarga, 0L);
    }

    private static int custoCoracoes(int cargasRestantes) {
        return switch (cargasRestantes) {
            case 3 -> 2;
            case 2 -> 4;
            default -> 8;
        };
    }

    private static boolean podeUsar(Player player, Estado estado) {
        return estado.cargas() > 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack antigo, ItemStack novo, boolean slotMudou) {
        return slotMudou || !antigo.is(novo.getItem());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!podeUsar(player, estado(stack, level.getGameTime()))) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tempoRestante) {
        if (level.isClientSide() || !(entity instanceof Player player) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int carga = this.getUseDuration(stack, entity) - tempoRestante;
        if (carga < TICKS_CARGA) {
            return;
        }

        long agora = level.getGameTime();
        Estado atual = estado(stack, agora);
        if (!podeUsar(player, atual)) {
            return;
        }

        int restantes = atual.cargas() - 1;
        stack.set(ModComponentesDados.SABRE_CARGAS.get(), restantes);
        if (restantes <= 0) {
            stack.set(ModComponentesDados.SABRE_COOLDOWN_FIM.get(), agora + TICKS_COOLDOWN);
            stack.set(ModComponentesDados.SABRE_RECARGA_FIM.get(), 0L);
        } else {
            long recarga = atual.recargaFim() > 0 ? atual.recargaFim() : agora + TICKS_RECARGA;
            stack.set(ModComponentesDados.SABRE_RECARGA_FIM.get(), recarga);
            stack.set(ModComponentesDados.SABRE_COOLDOWN_FIM.get(), 0L);
        }

        float custo = custoCoracoes(atual.cargas()) * 2.0F;
        float vidaRestante = player.getHealth() - custo;
        if (player.hasInfiniteMaterials()) {
            serverLevel.broadcastEntityEvent(player, (byte) 2);
        } else if (vidaRestante <= 0.0F) {
            player.hurt(serverLevel.damageSources().genericKill(), Float.MAX_VALUE);
        } else {
            player.setHealth(vidaRestante);
            serverLevel.broadcastEntityEvent(player, (byte) 2);
        }

        if (!player.hasInfiniteMaterials()) {
            player.getFoodData().setSaturation(Math.max(0.0F, player.getFoodData().getSaturationLevel() - CUSTO_SATURACAO));
        }

        player.swing(entity.getUsedItemHand(), true);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.8F);
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6F, 1.3F);

        double yaw = Math.toRadians(player.getYRot());
        double frenteX = -Math.sin(yaw);
        double frenteZ = Math.cos(yaw);
        double direitaX = -Math.cos(yaw);
        double direitaZ = -Math.sin(yaw);

        spawnarSangue(serverLevel, player, frenteX, frenteZ, direitaX, direitaZ);
        atingirAlvos(serverLevel, player, frenteX, frenteZ, direitaX, direitaZ);
    }

    private static void spawnarSangue(ServerLevel level, Player player, double frenteX, double frenteZ, double direitaX, double direitaZ) {
        double origemY = player.getY() + player.getBbHeight() * 0.55;
        double anguloMax = Math.atan2(LARGURA, ALCANCE);
        for (int i = 0; i < PARTICULAS; i++) {
            double angulo = (level.random.nextDouble() * 2.0 - 1.0) * anguloMax;
            double dirX = frenteX * Math.cos(angulo) + direitaX * Math.sin(angulo);
            double dirZ = frenteZ * Math.cos(angulo) + direitaZ * Math.sin(angulo);

            double inicio = level.random.nextDouble() * INICIO_MAX;
            double jitterLateral = (level.random.nextDouble() - 0.5) * 0.4;
            double jitterAltura = (level.random.nextDouble() - 0.5) * 0.6;
            double x = player.getX() + dirX * inicio + direitaX * jitterLateral;
            double z = player.getZ() + dirZ * inicio + direitaZ * jitterLateral;

            double velocidade = (ALCANCE - inicio) / SANGUE_VIDA;
            level.sendParticles(ModParticulas.SANGUE_PARTICULA.get(),
                    x, origemY + jitterAltura, z, 0,
                    dirX * velocidade, 0.0, dirZ * velocidade, 1.0);
        }
    }

    private static void atingirAlvos(ServerLevel level, Player player, double frenteX, double frenteZ, double direitaX, double direitaZ) {
        AABB area = player.getBoundingBox().inflate(ALCANCE + 1.0);
        List<LivingEntity> alvos = level.getEntitiesOfClass(LivingEntity.class, area,
                alvo -> alvo != player && alvo.isAlive() && !alvo.isSpectator());

        for (LivingEntity alvo : alvos) {
            double dx = alvo.getX() - player.getX();
            double dz = alvo.getZ() - player.getZ();
            double profundidade = dx * frenteX + dz * frenteZ;
            double lateral = dx * direitaX + dz * direitaZ;
            double margem = alvo.getBbWidth() / 2.0;

            if (profundidade < -margem || profundidade > ALCANCE + margem) {
                continue;
            }
            if (Math.abs(lateral) > LARGURA * Math.max(profundidade, 0.0) / ALCANCE + margem) {
                continue;
            }
            if (alvo.getY() > player.getEyeY() + 1.0 || alvo.getBoundingBox().maxY < player.getY() - 1.0) {
                continue;
            }

            aplicarEfeitos(player, alvo);
        }
    }

    private static boolean bloqueadoPorEscudo(Player player, LivingEntity alvo) {
        if (!alvo.isBlocking()) {
            return false;
        }
        Vec3 olhar = alvo.getViewVector(1.0F);
        Vec3 ate = player.position().subtract(alvo.position());
        Vec3 frente = new Vec3(olhar.x, 0.0, olhar.z);
        Vec3 direcao = new Vec3(ate.x, 0.0, ate.z);
        return direcao.dot(frente) > 0.0;
    }

    private static void aplicarEfeitos(Player player, LivingEntity alvo) {
        if (bloqueadoPorEscudo(player, alvo)) {
            alvo.hurt(alvo.damageSources().playerAttack(player), DANO_ALVO);
            return;
        }

        MobEffectInstance atual = alvo.getEffect(ModEfeitos.ARMADURA_RACHADA);
        int amplificador = atual == null ? 0 : atual.getAmplifier() + 1;
        int duracao = (atual == null ? 0 : atual.getDuration()) + TICKS_ARMADURA;
        alvo.addEffect(new MobEffectInstance(ModEfeitos.ARMADURA_RACHADA, duracao, amplificador, false, true, true), player);

        alvo.hurt(alvo.damageSources().playerAttack(player), DANO_ALVO);

        int nivel = Mth.clamp(amplificador + 1, 1, 3);
        alvo.igniteForSeconds(5.0F + 3.0F * (nivel - 1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.ultimo_suspiro"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.ultimo_suspiro_shift_down"));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
