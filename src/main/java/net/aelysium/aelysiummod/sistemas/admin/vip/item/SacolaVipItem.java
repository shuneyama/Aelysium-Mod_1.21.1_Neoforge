package net.aelysium.aelysiummod.sistemas.admin.vip.item;

import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.aelysium.aelysiummod.sistemas.admin.vip.MenuSacolaVip;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SacolaVipItem extends Item {

    public SacolaVipItem(Properties propriedades) {
        super(propriedades);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level mundo, Player jogador, InteractionHand mao) {
        ItemStack stack = jogador.getItemInHand(mao);
        if (mundo.isClientSide() || !(jogador instanceof ServerPlayer servidorJogador)) {
            return InteractionResultHolder.success(stack);
        }
        servidorJogador.openMenu(new SimpleMenuProvider(
                (idContainer, inventario, p) -> new MenuSacolaVip(idContainer, inventario, stack),
                nomeDoVip(stack)
        ));
        return InteractionResultHolder.success(stack);
    }

    private static Component nomeDoVip(ItemStack stack) {
        String nome = stack.get(ModComponentesDados.SACOLA_VIP_NOME.get());
        if (nome == null || nome.isEmpty()) return Component.translatable("item.aelysiummod.sacola_vip");
        return Component.literal(nome);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext contexto, List<Component> dicas, TooltipFlag flag) {
        dicas.add(Component.translatable("tooltip.aelysiummod.sacola_vip.botao_direito_para_retirar"));
        super.appendHoverText(stack, contexto, dicas, flag);
    }
}
