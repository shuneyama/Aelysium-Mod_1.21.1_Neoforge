package net.aelysium.aelysiummod.sistemas.mundo.porta.item;

import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.UUID;

public class CartaoItem extends Item {

    public CartaoItem(Properties properties) {
        super(properties);
    }

    public static boolean estaVinculado(ItemStack stack) {
        return stack.has(ModComponentesDados.CARTAO_PORTA_VINCULADA.get());
    }

    public static UUID getPortaVinculada(ItemStack stack) {
        return stack.get(ModComponentesDados.CARTAO_PORTA_VINCULADA.get());
    }

    public static void vincular(ItemStack stack, UUID portaId, String tipoNome) {
        stack.set(ModComponentesDados.CARTAO_PORTA_VINCULADA.get(), portaId);
        stack.set(ModComponentesDados.CARTAO_TIPO_VINCULO.get(), tipoNome);
    }

    public static String getTipoVinculo(ItemStack stack) {
        return stack.get(ModComponentesDados.CARTAO_TIPO_VINCULO.get());
    }

    public static boolean temCartaoValido(Player player, UUID id) {
        return casaCartao(player.getMainHandItem(), id) || casaCartao(player.getOffhandItem(), id)
                || ehCartaoMestre(player.getMainHandItem()) || ehCartaoMestre(player.getOffhandItem());
    }

    private static boolean casaCartao(ItemStack stack, UUID id) {
        return stack.getItem() instanceof CartaoItem && id.equals(getPortaVinculada(stack));
    }

    private static boolean ehCartaoMestre(ItemStack stack) {
        return stack.getItem() instanceof CartaoMestreItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (estaVinculado(stack)) {
            String tipo = getTipoVinculo(stack);
            if ("cofre".equals(tipo)) {
                tooltip.add(Component.translatable("tooltip.aelysiummod.porta.vinculado_a_um_cofre"));
            } else if ("porta".equals(tipo)) {
                tooltip.add(Component.translatable("tooltip.aelysiummod.porta.vinculado_a_uma_porta"));
            } else {
                tooltip.add(Component.translatable("tooltip.aelysiummod.porta.vinculado"));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.aelysiummod.porta.este_cartao_ainda_nao_foi_vinculado"));
        }
    }
}
