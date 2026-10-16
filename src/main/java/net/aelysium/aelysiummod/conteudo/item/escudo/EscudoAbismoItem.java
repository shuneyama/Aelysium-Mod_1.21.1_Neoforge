package net.aelysium.aelysiummod.conteudo.item.escudo;

import net.aelysium.aelysiummod.conteudo.item.escudo.GerenciadorBarreira;
import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class EscudoAbismoItem extends ShieldItem {

    public static final int TICKS_COOLDOWN = 3600;

    public EscudoAbismoItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack alvo, ItemStack reparo) {
        return reparo.is(Items.NETHERITE_INGOT);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack antigo, ItemStack novo, boolean slotMudou) {
        return slotMudou || !antigo.is(novo.getItem());
    }

    private static boolean emCooldown(ItemStack stack, Level level) {
        return level.getGameTime() < stack.getOrDefault(ModComponentesDados.ESCUDO_COOLDOWN_FIM.get(), 0L);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.getPersistentData().putInt(GerenciadorBarreira.CHAVE_CARGA, 0);
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entidade, ItemStack stack, int restante) {
        if (!(entidade instanceof Player player)) {
            return;
        }

        int carga = player.getPersistentData().getInt(GerenciadorBarreira.CHAVE_CARGA);
        if (player.isShiftKeyDown() && !emCooldown(stack, level)) {
            carga++;
        } else {
            carga = 0;
        }
        player.getPersistentData().putInt(GerenciadorBarreira.CHAVE_CARGA, carga);

        if (carga >= GerenciadorBarreira.TICKS_CARGA && player instanceof ServerPlayer servidor && !GerenciadorBarreira.existe(servidor.getUUID())) {
            player.getPersistentData().putInt(GerenciadorBarreira.CHAVE_CARGA, 0);
            stack.set(ModComponentesDados.ESCUDO_COOLDOWN_FIM.get(), level.getGameTime() + TICKS_COOLDOWN);
            GerenciadorBarreira.criar(servidor);
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entidade, int tempoRestante) {
        if (entidade instanceof Player player) {
            player.getPersistentData().putInt(GerenciadorBarreira.CHAVE_CARGA, 0);
        }
        super.releaseUsing(stack, level, entidade, tempoRestante);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.escudo_abismo"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.escudo_abismo_shift_down"));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
