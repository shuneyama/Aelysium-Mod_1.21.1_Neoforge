package net.aelysium.aelysiummod.sistemas.mundo.porta.item;

import net.aelysium.aelysiummod.sistemas.mundo.porta.BlocoPortaSenha;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class GazuaItem extends Item {

    private static final int DURACAO_USO = 3 * 20;
    private static final float CHANCE_ABRIR = 0.05f;
    private static final float CHANCE_QUEBRAR = 0.25f;
    private static final int RECARGA_TICKS = 20;
    private static final int INTERVALO_VERIFICACAO = 5;
    private static final int INTERVALO_SOM = 10;

    public GazuaItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DURACAO_USO;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockState estado = level.getBlockState(context.getClickedPos());
        if (player == null || !(estado.getBlock() instanceof BlocoPortaSenha)) return InteractionResult.PASS;

        if (player.getCooldowns().isOnCooldown(this)) return InteractionResult.CONSUME;

        if (estado.getValue(DoorBlock.OPEN)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.gazua.ja_aberta"), true);
            }
            return InteractionResult.CONSUME;
        }

        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        if (remainingUseDuration % INTERVALO_VERIFICACAO == 0 && portaNaMira(level, player) == null) {
            player.stopUsingItem();
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.gazua.perdeu_fechadura"), true);
            }
            return;
        }

        if (level.isClientSide) return;

        if (remainingUseDuration % 20 == 0) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.gazua.arrombando", remainingUseDuration / 20), true);
        }
        if (remainingUseDuration % INTERVALO_SOM == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.PLAYERS, 0.4f, 1.6f + level.random.nextFloat() * 0.3f);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.gazua.interrompida"), true);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) return stack;

        BlockPos base = portaNaMira(level, player);
        if (base == null) {
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.gazua.perdeu_fechadura"), true);
            return stack;
        }

        player.getCooldowns().addCooldown(this, RECARGA_TICKS);

        if (level.random.nextFloat() < CHANCE_ABRIR) {
            BlockState estado = level.getBlockState(base);
            ((BlocoPortaSenha) estado.getBlock()).abrirPorta(level, base, estado);
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.gazua.sucesso"), true);
            return stack;
        }

        if (level.random.nextFloat() < CHANCE_QUEBRAR) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8f, 1.2f);
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.gazua.quebrou"), true);
            stack.consume(1, player);
            return stack;
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 0.5f, 1.4f);
        player.displayClientMessage(Component.translatable("mensagem.aelysiummod.gazua.falhou"), true);
        return stack;
    }

    private static BlockPos portaNaMira(Level level, Player player) {
        BlockHitResult acerto = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (acerto.getType() != HitResult.Type.BLOCK) return null;

        BlockPos pos = acerto.getBlockPos();
        BlockState estado = level.getBlockState(pos);
        if (!(estado.getBlock() instanceof BlocoPortaSenha) || estado.getValue(DoorBlock.OPEN)) return null;

        return estado.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }
}
