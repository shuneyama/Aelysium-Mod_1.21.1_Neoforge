package net.aelysium.aelysiummod.conteudo.item.cigarro;

import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CigarroCurio implements ICurio {

    private static final int INTERVALO_TICKS = 30;

    private final ItemStack stack;

    public CigarroCurio(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        boolean aceso = stack.getOrDefault(ModComponentesDados.CIGARRO_ACESO, false);
        int fumadas = stack.getOrDefault(ModComponentesDados.CIGARRO_FUMADAS, 0);
        if (!aceso || fumadas >= CigarroItem.MAX_FUMADAS) return;

        LivingEntity entity = slotContext.entity();
        if (entity.tickCount % INTERVALO_TICKS != 0) return;

        CigarroItem.exalarFumacaPequena(entity, entity.level());
    }
}
