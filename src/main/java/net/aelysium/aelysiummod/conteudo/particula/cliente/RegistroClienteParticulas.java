package net.aelysium.aelysiummod.conteudo.particula.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.particula.DamaDaNoiteParticula;
import net.aelysium.aelysiummod.conteudo.particula.DamaVermelhaDaNoiteParticula;
import net.aelysium.aelysiummod.conteudo.particula.FendaKairosParticula;
import net.aelysium.aelysiummod.conteudo.particula.SangueParticula;
import net.aelysium.aelysiummod.registro.ModParticulas;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class RegistroClienteParticulas {

    @SubscribeEvent
    public static void registrarFabricas(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticulas.DAMA_DA_NOITE_PARTICULA.get(), DamaDaNoiteParticula.Provedor::new);
        event.registerSpriteSet(ModParticulas.DAMA_VERMELHA_DA_NOITE_PARTICULA.get(), DamaVermelhaDaNoiteParticula.Provedor::new);
        event.registerSpriteSet(ModParticulas.FENDA_KAIROS_PARTICULA.get(), FendaKairosParticula.Provedor::new);
        event.registerSpriteSet(ModParticulas.SANGUE_PARTICULA.get(), SangueParticula.Provedor::new);
    }
}
