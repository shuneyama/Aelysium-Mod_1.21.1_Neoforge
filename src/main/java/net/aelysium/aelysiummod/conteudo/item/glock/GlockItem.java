package net.aelysium.aelysiummod.conteudo.item.glock;

import net.aelysium.aelysiummod.conteudo.entidade.EntidadeBalaGlock;
import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.aelysium.aelysiummod.registro.ModItens;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.PacoteRecuoGlock;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.PacoteSomGlock;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GlockItem extends Item {

    public static final int CAPACIDADE_MAGAZINE = 15;
    private static final double VELOCIDADE_BALA = 3.0;
    private static final int COOLDOWN_TICKS = 10;

    public GlockItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        int municao = stack.getOrDefault(ModComponentesDados.GLOCK_MUNICAO, 0);
        if (municao <= 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.7f, 1.6f);
            return InteractionResultHolder.fail(stack);
        }

        stack.set(ModComponentesDados.GLOCK_MUNICAO, municao - 1);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        if (level instanceof ServerLevel serverLevel) {
            EntidadeBalaGlock bala = new EntidadeBalaGlock(serverLevel, player);
            Vec3 look = player.getLookAngle();
            bala.setDeltaMovement(look.scale(VELOCIDADE_BALA));
            bala.setYRot((float) (Mth.atan2(look.x, look.z) * (180F / (float) Math.PI)));
            bala.setXRot((float) (Mth.atan2(look.y, look.horizontalDistance()) * (180F / (float) Math.PI)));
            serverLevel.addFreshEntity(bala);

            PacketDistributor.sendToPlayersNear(serverLevel, null, player.getX(), player.getY(), player.getZ(), 64.0,
                    new PacoteSomGlock(true, player.getX(), player.getY(), player.getZ()));

            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new PacoteRecuoGlock());
            }
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int municao = stack.getOrDefault(ModComponentesDados.GLOCK_MUNICAO, 0);
        tooltipComponents.add(Component.translatableEscape("tooltip.aelysiummod.glock.municao", municao, CAPACIDADE_MAGAZINE));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static void recarregar(ServerPlayer player) {
        ItemStack glock = player.getMainHandItem();
        if (!(glock.getItem() instanceof GlockItem)) {
            glock = player.getOffhandItem();
            if (!(glock.getItem() instanceof GlockItem)) return;
        }

        int atual = glock.getOrDefault(ModComponentesDados.GLOCK_MUNICAO, 0);
        int espaco = CAPACIDADE_MAGAZINE - atual;
        if (espaco <= 0) return;

        Inventory inventario = player.getInventory();
        int totalBalas = 0;
        for (int i = 0; i < inventario.getContainerSize(); i++) {
            ItemStack item = inventario.getItem(i);
            if (item.is(ModItens.BALA_GLOCK.get())) {
                totalBalas += item.getCount();
            }
        }
        if (totalBalas <= 0) return;

        int aRecarregar = Math.min(espaco, totalBalas);
        int restante = aRecarregar;
        for (int i = 0; i < inventario.getContainerSize() && restante > 0; i++) {
            ItemStack item = inventario.getItem(i);
            if (!item.is(ModItens.BALA_GLOCK.get())) continue;
            int consumir = Math.min(restante, item.getCount());
            item.shrink(consumir);
            restante -= consumir;
        }

        glock.set(ModComponentesDados.GLOCK_MUNICAO, atual + aRecarregar);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4f, 1.4f);
    }
}
