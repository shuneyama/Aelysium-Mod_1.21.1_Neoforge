package net.aelysium.aelysiummod.nucleo.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class DesfocoCamposTexto {
    private DesfocoCamposTexto() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void aoClicar(ScreenEvent.MouseButtonPressed.Pre evento) {
        Screen tela = evento.getScreen();
        if (!tela.getClass().getName().startsWith("net.aelysium.")) return;
        if (tela instanceof AreaMantemFoco area && area.mantemFoco(evento.getMouseX(), evento.getMouseY())) return;
        double mx = evento.getMouseX();
        double my = evento.getMouseY();
        for (GuiEventListener filho : tela.children()) {
            if (filho instanceof EditBox caixa && caixa.isMouseOver(mx, my)) return;
        }
        for (GuiEventListener filho : tela.children()) {
            if (filho instanceof EditBox caixa && caixa.isFocused()) {
                caixa.setFocused(false);
                if (tela.getFocused() == caixa) tela.setFocused(null);
            }
        }
    }

    @SubscribeEvent
    public static void aposClicar(ScreenEvent.MouseButtonPressed.Post evento) {
        Screen tela = evento.getScreen();
        if (!tela.getClass().getName().startsWith("net.aelysium.")) return;
        if (tela.getFocused() instanceof AbstractButton botao && !(botao instanceof net.minecraft.client.gui.components.Checkbox)) {
            ((AbstractWidget) botao).setFocused(false);
            tela.setFocused(null);
        }
    }
}
