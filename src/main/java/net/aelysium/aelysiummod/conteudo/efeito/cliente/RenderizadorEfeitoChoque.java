package net.aelysium.aelysiummod.conteudo.efeito.cliente;

import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@OnlyIn(Dist.CLIENT)
public class RenderizadorEfeitoChoque {

    private static final long PASSO_MS = 35L;
    private static final float TREMOR_AMPLITUDE = 0.16f;
    private static final float CAMERA_AMPLITUDE = 1.2f;

    private static float hashSalto(long seed, int canal) {
        long x = (seed * 2654435761L) ^ (canal * 0x9E3779B97F4A7C15L);
        x = (x ^ (x >>> 33)) * 0xFF51AFD7ED558CCDL;
        x = (x ^ (x >>> 33)) * 0xC4CEB9FE1A85EC53L;
        x = x ^ (x >>> 33);
        return ((x & 0xFFFF) / 65535f) * 2f - 1f;
    }

    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player) || !entity.hasEffect(ModEfeitos.CHOQUE)) return;

        long passo = (System.currentTimeMillis() + entity.getId() * 733L) / PASSO_MS;
        float x = TREMOR_AMPLITUDE * hashSalto(passo, 0);
        float y = TREMOR_AMPLITUDE * hashSalto(passo, 1);
        float z = TREMOR_AMPLITUDE * hashSalto(passo, 2);

        event.getPoseStack().pushPose();
        event.getPoseStack().translate(x, y, z);
    }

    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player) || !entity.hasEffect(ModEfeitos.CHOQUE)) return;
        event.getPoseStack().popPose();
    }

    @SubscribeEvent
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasEffect(ModEfeitos.CHOQUE)) return;

        long passo = System.currentTimeMillis() / PASSO_MS;
        float yaw = CAMERA_AMPLITUDE * hashSalto(passo, 3);
        float pitch = CAMERA_AMPLITUDE * hashSalto(passo, 4);
        float roll = CAMERA_AMPLITUDE * hashSalto(passo, 5);

        event.setYaw(event.getYaw() + yaw);
        event.setPitch(event.getPitch() + pitch);
        event.setRoll(event.getRoll() + roll);
    }

    @SubscribeEvent
    public void onScreenOpening(ScreenEvent.Opening event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasEffect(ModEfeitos.CHOQUE)) return;
        if (event.getNewScreen() instanceof InventoryScreen) {
            event.setCanceled(true);
        }
    }
}
