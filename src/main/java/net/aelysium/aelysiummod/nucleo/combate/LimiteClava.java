package net.aelysium.aelysiummod.nucleo.combate;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.UtilDeus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class LimiteClava {
    public static final float DANO_MAXIMO = 20.0F;

    private LimiteClava() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void aoReceberDano(LivingDamageEvent.Pre evento) {
        ItemStack arma = evento.getSource().getWeaponItem();
        if (arma == null || !(arma.getItem() instanceof MaceItem)) return;
        if (UtilDeus.poder(evento.getSource().getEntity(), TipoDeus.DANO_MORTAL)) return;
        if (evento.getNewDamage() > DANO_MAXIMO) {
            evento.setNewDamage(DANO_MAXIMO);
        }
    }
}
