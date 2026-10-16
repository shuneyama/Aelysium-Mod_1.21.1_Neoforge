package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorTeiasDificuldade {

    @SubscribeEvent
    public static void aoTickEntidade(EntityTickEvent.Post evento) {
        if (!(evento.getEntity() instanceof Spider aranha)) return;
        if (!(aranha.level() instanceof ServerLevel mundo)) return;
        if (aranha.tickCount % 20 != 0 || !aranha.onGround()) return;

        int nivel = AplicadorDificuldade.nivelAplicado(aranha);
        if (nivel <= 0) return;

        DadosDificuldade dados = DadosDificuldade.get(mundo.getServer());
        if (dados.getNivel() <= 0 || !dados.isGriefing()) return;

        LivingEntity alvo = aranha.getTarget();
        if (alvo == null || aranha.distanceTo(alvo) > PerfilDificuldade.distanciaTeiaAlvo()) return;
        if (aranha.getRandom().nextDouble() >= PerfilDificuldade.chanceTeiaPorSegundo(nivel)) return;

        BlockPos pos = aranha.blockPosition();
        if (!mundo.getBlockState(pos).isAir()) return;
        if (!mundo.getBlockState(pos.below()).isFaceSturdy(mundo, pos.below(), net.minecraft.core.Direction.UP)) return;

        mundo.setBlock(pos, ModBlocos.TEIA_DIFICULDADE.get().defaultBlockState(), 3);
    }
}
