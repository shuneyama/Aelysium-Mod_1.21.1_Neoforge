package net.aelysium.aelysiummod.time;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

@EventBusSubscriber(modid = "aelysiummod", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TimeCorRenderizador {

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        Entity entity = event.getEntity();
        PlayerTeam team = entity.getTeam();

        if (team == null) return;

        TimeCorGerenciador.getTeamColor(team).ifPresent(customColor -> {
            Component newContent = event.getContent().copy().withColor(customColor.getRgb());
            event.setContent(newContent);
        });
    }
}