package net.aelysium.aelysiummod.nucleo.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.aelysium.aelysiummod.compatibilidade.jade.RegistroRedeJade;
import net.aelysium.aelysiummod.conteudo.item.carteira.rede.RegistroRedeCarteira;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.RegistroRedeGlock;
import net.aelysium.aelysiummod.nucleo.guarda.RegistroRedeGuarda;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.RegistroRedeBanimentos;
import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.RegistroRedeProtecao;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.rede.RegistroRedeCinematica;
import net.aelysium.aelysiummod.sistemas.midia.holograma.rede.RegistroRedeHolograma;
import net.aelysium.aelysiummod.sistemas.midia.tocar.rede.RegistroRedeTocar;
import net.aelysium.aelysiummod.sistemas.mundo.lua.rede.RegistroRedeLua;
import net.aelysium.aelysiummod.sistemas.mundo.porta.rede.RegistroRedePorta;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.RegistroRedeDeus;
import net.aelysium.aelysiummod.sistemas.rp.morte.rede.RegistroRedeMorte;
import net.aelysium.aelysiummod.sistemas.rp.raca.rede.RegistroRedeRaca;
import net.aelysium.aelysiummod.sistemas.social.apelido.rede.RegistroRedeApelido;
import net.aelysium.aelysiummod.sistemas.social.chat.rede.RegistroRedeChat;
import net.aelysium.aelysiummod.sistemas.social.insignia.rede.RegistroRedeInsignia;
import net.aelysium.aelysiummod.sistemas.social.maozinha.rede.RegistroRedeMaozinha;
import net.aelysium.aelysiummod.sistemas.social.npc.rede.RegistroRedeNpc;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ServidorMensagens {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AelysiumMod.MOD_ID).versioned("1").optional();

        RegistroRedeJade.registrar(registrar);
        RegistroRedeBanimentos.registrar(registrar);
        RegistroRedeChat.registrar(registrar);
        RegistroRedeLua.registrar(registrar);
        RegistroRedeApelido.registrar(registrar);
        RegistroRedeInsignia.registrar(registrar);
        RegistroRedeRaca.registrar(registrar);
        RegistroRedeMorte.registrar(registrar);
        RegistroRedeGlock.registrar(registrar);
        RegistroRedeNpc.registrar(registrar);
        RegistroRedeDeus.registrar(registrar);
        RegistroRedeGuarda.registrar(registrar);
        RegistroRedeCarteira.registrar(registrar);
        RegistroRedeProtecao.registrar(registrar);
        RegistroRedeCinematica.registrar(registrar);
        RegistroRedeHolograma.registrar(registrar);
        RegistroRedeTocar.registrar(registrar);
        RegistroRedePorta.registrar(registrar);
        RegistroRedeMaozinha.registrar(registrar);
        net.aelysium.aelysiummod.sistemas.mundo.minifarol.rede.RegistroRedeMiniFarol.registrar(registrar);
        net.aelysium.aelysiummod.sistemas.admin.cabeca.rede.RegistroRedeCabeca.registrar(registrar);
    }
}