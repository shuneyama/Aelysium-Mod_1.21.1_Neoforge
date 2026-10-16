package net.aelysium.aelysiummod.sistemas.admin.banimentos.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteListaMods;
import net.aelysium.aelysiummod.sistemas.admin.historico.ModCliente;
import net.aelysium.aelysiummod.sistemas.admin.historico.UtilitariosMods;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class ManipuladorListaNegraModsCliente {

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        Thread calculo = new Thread(() -> {
            try {
                List<ModCliente> mods = UtilitariosMods.coletar(true);
                String minecraft = UtilitariosMods.versaoMinecraft();
                String loader = "neoforge";
                String loaderVersao = UtilitariosMods.versaoLoader();
                Minecraft.getInstance().execute(() -> enviar(minecraft, loader, loaderVersao, mods));
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(AelysiumMod.MOD_ID)
                        .warn("Falha ao enviar lista de mods ao servidor: ", e);
            }
        }, "aelysium-hash-mods-cliente");
        calculo.setDaemon(true);
        calculo.start();
    }

    private static void enviar(String minecraft, String loader, String loaderVersao, List<ModCliente> mods) {
        if (Minecraft.getInstance().getConnection() == null) return;

        int tamanho = PacoteListaMods.MODS_POR_PARTE;
        int total = Math.max(1, (mods.size() + tamanho - 1) / tamanho);
        for (int parte = 0; parte < total; parte++) {
            List<ModCliente> fatia = new ArrayList<>(mods.subList(parte * tamanho, Math.min(mods.size(), (parte + 1) * tamanho)));
            PacketDistributor.sendToServer(new PacoteListaMods(parte, total, minecraft, loader, loaderVersao, fatia));
        }
    }
}
