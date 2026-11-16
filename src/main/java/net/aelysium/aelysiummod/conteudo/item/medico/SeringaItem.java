package net.aelysium.aelysiummod.conteudo.item.medico;

import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SeringaItem extends Item {

    private static final int DURACAO_USO = 3 * 20;
    private static final double TOLERANCIA_ALCANCE = 1.0;
    private static final int INTERVALO_VERIFICACAO = 5;

    public SeringaItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DURACAO_USO;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.isShiftKeyDown() || !(interactionTarget instanceof Player)) return InteractionResult.PASS;

        player.startUsingItem(usedHand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        if (remainingUseDuration % INTERVALO_VERIFICACAO == 0 && !(alvoNaMira(player) instanceof Player)) {
            player.stopUsingItem();
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.alvo_perdido"), true);
            }
            return;
        }

        if (!level.isClientSide && remainingUseDuration % 20 == 0) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.coletando", remainingUseDuration / 20), true);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.interrompida"), true);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) return stack;

        if (!(alvoNaMira(player) instanceof Player doador)) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.alvo_perdido"), true);
            return stack;
        }

        Component nomeDoador = doador.getName().copy();
        ItemStack seringaSangue = new ItemStack(ModItens.SERINGA_SANGUE.get());
        seringaSangue.set(DataComponents.ITEM_NAME, Component.translatable("item.aelysiummod.seringa_sangue.com_nome", nomeDoador));

        level.playSound(null, doador.getX(), doador.getY(), doador.getZ(),
                SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.6f, 1.2f);

        player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.coletou", nomeDoador), true);
        if (doador instanceof ServerPlayer jogadorDoador) {
            jogadorDoador.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.coletado_por", player.getName().copy()), true);
        }

        return ItemUtils.createFilledResult(stack, player, seringaSangue, false);
    }

    static LivingEntity alvoNaMira(Player player) {
        double alcance = player.entityInteractionRange() + TOLERANCIA_ALCANCE;
        Vec3 olho = player.getEyePosition();
        Vec3 percurso = player.getViewVector(1.0f).scale(alcance);
        AABB area = player.getBoundingBox().expandTowards(percurso).inflate(1.0);

        EntityHitResult acerto = ProjectileUtil.getEntityHitResult(player, olho, olho.add(percurso), area,
                entidade -> entidade instanceof LivingEntity && entidade.isPickable() && !entidade.isSpectator(),
                alcance * alcance);
        if (acerto == null) return null;

        HitResult bloco = player.pick(alcance, 1.0f, false);
        if (bloco.getType() != HitResult.Type.MISS
                && bloco.getLocation().distanceToSqr(olho) < acerto.getLocation().distanceToSqr(olho)) {
            return null;
        }

        return (LivingEntity) acerto.getEntity();
    }
}
