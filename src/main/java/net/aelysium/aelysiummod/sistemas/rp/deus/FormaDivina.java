package net.aelysium.aelysiummod.sistemas.rp.deus;

import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteFormaDivina;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;

public class FormaDivina {

    private static final Map<UUID, TipoDeus> formasAtivas = new ConcurrentHashMap<>();

    public static boolean estaAtiva(ServerPlayer player) {
        return formasAtivas.containsKey(player.getUUID());
    }

    public static boolean estaAtiva(UUID uuid) {
        return formasAtivas.containsKey(uuid);
    }

    public static TipoDeus getFormaAtiva(UUID uuid) {
        return formasAtivas.getOrDefault(uuid, TipoDeus.NONE);
    }

    public static void tentar(ServerPlayer player) {
        DadosDeus data = DadosDeus.get(player.serverLevel());
        TipoDeus deus = data.getDeus(player.getUUID());

        if (deus == TipoDeus.NONE) {
            return;
        }

        if (estaAtiva(player)) {
            desativar(player);
        } else {
            ativar(player, deus);
        }
    }

    public static void ativar(ServerPlayer player, TipoDeus deus) {
        formasAtivas.put(player.getUUID(), deus);

        player.forceAddEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false), null);

        PacketDistributor.sendToAllPlayers(
                new PacoteFormaDivina(player.getUUID(), true, deus.id, deus.color));
    }

    public static void desativar(ServerPlayer player) {
        formasAtivas.remove(player.getUUID());

        player.removeEffect(MobEffects.INVISIBILITY);

        PacketDistributor.sendToAllPlayers(
                new PacoteFormaDivina(player.getUUID(), false, "", 0));
    }

    @SubscribeEvent
    public void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!estaAtiva(player)) return;

        if (!event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
        }
    }

    public static void onPlayerLogout(UUID uuid) {
        formasAtivas.remove(uuid);
    }

    @SubscribeEvent
    public void aoSairJogador(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        onPlayerLogout(player.getUUID());
    }
}