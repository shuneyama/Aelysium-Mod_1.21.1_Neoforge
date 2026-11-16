package net.aelysium.aelysiummod.sistemas.social.npc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.cliente.JogadorNpcFalso;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class EventoNomeNpcMixin {

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;")
    )
    private Event aelysium$semEventoNomeNoNpc(IEventBus barramento, Event evento, Operation<Event> original) {
        if (evento instanceof RenderNameTagEvent nome) {
            Entity entidade = nome.getEntity();
            if (entidade instanceof EntidadeNpcPersonalizado || entidade instanceof JogadorNpcFalso) return evento;
        }
        return original.call(barramento, evento);
    }
}
