package net.aelysium.aelysiummod.sistemas.admin.vip.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteVip {

    @SubscribeEvent
    public static void registrarTelas(RegisterMenuScreensEvent evento) {
        evento.register(ModMenus.SACOLA_VIP.get(), TelaSacolaVip::new);
    }
}
