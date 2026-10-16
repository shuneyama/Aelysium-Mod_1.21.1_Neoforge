package net.aelysium.aelysiummod.compatibilidade.waystones;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.maozinha.GerenciadorMaozinha;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class IntegracaoWaystones {

    private static final Logger LOGGER = LoggerFactory.getLogger(AelysiumMod.MOD_ID);

    private static final String MOD_WAYSTONES = "waystones";
    private static final String CLASSE_BALM = "net.blay09.mods.balm.api.Balm";
    private static final String CLASSE_EVENTOS_BALM = "net.blay09.mods.balm.api.event.BalmEvents";
    private static final String CLASSE_EVENTO_TELEPORTE = "net.blay09.mods.waystones.api.event.WaystoneTeleportEvent$Pre";
    private static final String CLASSE_CONTEXTO_TELEPORTE = "net.blay09.mods.waystones.api.WaystoneTeleportContext";

    private static Method obterContexto;
    private static Method obterEntidade;
    private static Method adicionarEntidade;

    public static void registrar() {
        if (!ModList.get().isLoaded(MOD_WAYSTONES)) return;

        try {
            Class<?> evento = Class.forName(CLASSE_EVENTO_TELEPORTE);
            obterContexto = evento.getMethod("getContext");
            adicionarEntidade = evento.getMethod("addAdditionalEntity", Entity.class);
            obterEntidade = Class.forName(CLASSE_CONTEXTO_TELEPORTE).getMethod("getEntity");

            Object eventos = Class.forName(CLASSE_BALM).getMethod("getEvents").invoke(null);
            Method aoEvento = Class.forName(CLASSE_EVENTOS_BALM).getMethod("onEvent", Class.class, Consumer.class);
            Consumer<Object> ouvinte = IntegracaoWaystones::antesDoTeleporte;
            aoEvento.invoke(eventos, evento, ouvinte);
        } catch (ReflectiveOperationException | LinkageError e) {
            LOGGER.warn("[Aelysium] Nao foi possivel integrar a maozinha com a Waystones", e);
        }
    }

    private static void antesDoTeleporte(Object evento) {
        try {
            Object contexto = obterContexto.invoke(evento);
            if (!(obterEntidade.invoke(contexto) instanceof ServerPlayer jogador)) return;

            ServerPlayer parceiro = GerenciadorMaozinha.parceiroDe(jogador);
            if (parceiro != null) adicionarEntidade.invoke(evento, parceiro);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("[Aelysium] Falha ao levar o parceiro da maozinha no teleporte da Waystones", e);
        }
    }
}
