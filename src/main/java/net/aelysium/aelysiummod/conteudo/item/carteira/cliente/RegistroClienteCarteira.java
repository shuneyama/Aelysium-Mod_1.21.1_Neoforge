package net.aelysium.aelysiummod.conteudo.item.carteira.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class RegistroClienteCarteira {

    @SubscribeEvent
    public static void registrarTelas(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CARTEIRA_MENU.get(), TelaCarteira::new);
    }
}
