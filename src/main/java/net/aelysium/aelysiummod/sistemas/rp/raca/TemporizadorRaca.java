package net.aelysium.aelysiummod.sistemas.rp.raca;

import net.aelysium.aelysiummod.registro.ModItens;
import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteVooValkyria;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public class TemporizadorRaca {

    private static final int EFFECT_DURATION = 1200;
    private static final int EFFECT_REFRESH  = 200;
    private static final int WATER_CHECK_INTERVAL = 10;

    private static final int VALKYRIA_WEAKNESS_COOLDOWN = 15 * 20;

    private static final ResourceKey<Enchantment> LOOTING_KEY =
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace("looting"));

    private static final Map<UUID, TipoRaca> raceCache = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> valkyriaCooldowns = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> undyneWaterState = new ConcurrentHashMap<>();
    private static final Set<UUID> undynePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, ItemStack> undyneMainHand = new ConcurrentHashMap<>();

    public static TipoRaca getCachedRace(UUID uuid) {
        return raceCache.getOrDefault(uuid, TipoRaca.NONE);
    }

    public static void updateCache(UUID uuid, TipoRaca race) {
        var servidor = ServerLifecycleHooks.getCurrentServer();
        if (servidor != null) {
            ServerPlayer jogador = servidor.getPlayerList().getPlayer(uuid);
            if (jogador != null) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(jogador,
                        new net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteImunidadeFogo(race == TipoRaca.TIEFLING));
            }
        }
        if (race == TipoRaca.NONE) {
            raceCache.remove(uuid);
            undynePlayers.remove(uuid);
            undyneMainHand.remove(uuid);
        } else {
            raceCache.put(uuid, race);
            if (race == TipoRaca.UNDYNE) {
                undynePlayers.add(uuid);
            } else {
                undynePlayers.remove(uuid);
                undyneMainHand.remove(uuid);
            }
        }
    }

    public static void removeFromCache(UUID uuid) {
        raceCache.remove(uuid);
        undynePlayers.remove(uuid);
        undyneMainHand.remove(uuid);
        undyneWaterState.remove(uuid);
        valkyriaCooldowns.remove(uuid);
        AlternadorVisaoNoturna.remover(uuid);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        int serverTick = server.getTickCount();

        AnimacaoRevelacaoRaca.tick();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) continue;

            UUID uuid = player.getUUID();
            TipoRaca race = raceCache.get(uuid);
            if (race == null || race == TipoRaca.NONE) continue;

            boolean shouldRefreshEffects = (serverTick + uuid.hashCode()) % EFFECT_REFRESH == 0;

            switch (race) {
                case HUMANO -> {}
                case ELVARIN -> {
                    if (shouldRefreshEffects) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_DURATION, 0, false, false, false));
                    }
                }
                case DRACONO -> {}
                case TIEFLING -> {}
                case UNDYNE -> {
                    if (shouldRefreshEffects) {
                        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, EFFECT_DURATION, 0, false, false, false));
                    }
                    if (serverTick % WATER_CHECK_INTERVAL == 0) {
                        tickUndyneWater(player);
                    }
                    undyneMainHand.put(uuid, player.getMainHandItem());
                }
                case VALKYRIA -> {
                    if (shouldRefreshEffects) {
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 0, false, false, false));
                    }
                    tickValkyria(player);
                }
                case ROBO -> {
                    tickRoboHunger(player);
                }
                default -> {}
            }
        }
    }

    @SubscribeEvent
    public void onDanoRecebidoTiefling(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer vitima)) return;
        if (raceCache.getOrDefault(vitima.getUUID(), TipoRaca.NONE) != TipoRaca.TIEFLING) return;
        if (!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return;
        if (vitima.level().dimension() != net.minecraft.world.level.Level.OVERWORLD) return;
        event.setCanceled(true);
        vitima.clearFire();
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            TipoRaca attackerRace = raceCache.getOrDefault(attacker.getUUID(), TipoRaca.NONE);

            if (attackerRace == TipoRaca.VALKYRIA) {
                long now = attacker.level().getGameTime();
                Long lastUse = valkyriaCooldowns.get(attacker.getUUID());

                if (lastUse == null || (now - lastUse) >= VALKYRIA_WEAKNESS_COOLDOWN) {
                    if (event.getEntity() instanceof net.minecraft.world.entity.LivingEntity target) {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 5 * 20, 0, false, true, true));
                        valkyriaCooldowns.put(attacker.getUUID(), now);
                    }
                }
            }
        }

        if (event.getEntity() instanceof ServerPlayer victim) {
            TipoRaca victimRace = raceCache.getOrDefault(victim.getUUID(), TipoRaca.NONE);

            if (victimRace == TipoRaca.TIEFLING) {
                DamageSource source = event.getSource();
                if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                    if (victim.level().dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                        event.setNewDamage(0f);
                    } else {
                        event.setNewDamage(event.getOriginalDamage() * 0.5f);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onGetEnchantmentLevel(GetEnchantmentLevelEvent event) {
        if (undynePlayers.isEmpty()) return;

        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        for (Map.Entry<UUID, ItemStack> entry : undyneMainHand.entrySet()) {
            if (entry.getValue() == stack) {
                var server = ServerLifecycleHooks.getCurrentServer();
                if (server == null) return;

                var lookup = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                var lootingHolder = lookup.get(LOOTING_KEY).orElse(null);
                if (lootingHolder == null) return;

                int current = event.getEnchantments().getLevel(lootingHolder);
                event.getEnchantments().set(lootingHolder, current + 1);
                return;
            }
        }
    }

    private static void tickRoboHunger(ServerPlayer player) {
        if (player.tickCount % 80 != 0) return;
        var foodData = player.getFoodData();
        float exh = foodData.getExhaustionLevel();
        if (exh > 0) {
            foodData.setExhaustion(exh - 0.05f);
        }
    }

    @SubscribeEvent
    public void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        TipoRaca race = raceCache.getOrDefault(player.getUUID(), TipoRaca.NONE);
        if (race != TipoRaca.ROBO) return;

        ItemStack stack = event.getItem();
        if (stack.has(DataComponents.FOOD) && !stack.is(ModItens.BATERIA.get())) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.translatable("mensagem.aelysiummod.raca.voce_nao_pode_consumir_alimentos"));
        }
    }

    private static void tickUndyneWater(ServerPlayer player) {
        boolean inWater = player.isInWater() || player.isUnderWater();
        Boolean wasInWater = undyneWaterState.get(player.getUUID());

        if (wasInWater == null) {
            undyneWaterState.put(player.getUUID(), inWater);
            if (inWater) {
                GerenciadorRaca.applyUndyneWaterBonus(player);
            }
            return;
        }

        if (inWater != wasInWater) {
            undyneWaterState.put(player.getUUID(), inWater);
            if (inWater) {
                GerenciadorRaca.applyUndyneWaterBonus(player);
            } else {
                GerenciadorRaca.applyUndyneLandStats(player);
            }
        }
    }

    public static void clearUndyneState(UUID uuid) {
        undyneWaterState.remove(uuid);
    }

    public static void clearValkyriaCooldown(UUID uuid) {
        valkyriaCooldowns.remove(uuid);
    }

    private static void tickValkyria(ServerPlayer player) {
        UUID id = player.getUUID();
        GerenciadorVooValkyria.DadosVoo fd = GerenciadorVooValkyria.getOrCreate(id);

        if (player.isCreative() || player.isSpectator()) {
            PacketDistributor.sendToPlayer(player, new PacoteVooValkyria(false, 0, 0));
            return;
        }

        if (!PerfilJogador.voo().podeVoarAqui(player)) {
            forceDisableFlight(player);
            PacketDistributor.sendToPlayer(player, new PacoteVooValkyria(false, fd.ticksFlown, fd.cooldownTicks));
            return;
        }

        if (fd.cooldownTicks > 0) {
            forceDisableFlight(player);
            fd.cooldownTicks--;

            if (fd.cooldownTicks == 0) {
                player.sendSystemMessage(Component.translatable("mensagem.aelysiummod.raca.valkyria_voo_restaurado"));
            }

            PacketDistributor.sendToPlayer(player, new PacoteVooValkyria(true, fd.ticksFlown, fd.cooldownTicks));
            return;
        }

        if (fd.ticksFlown >= GerenciadorVooValkyria.MAX_FLY_TICKS) {
            forceDisableFlight(player);
            fd.ticksFlown = 0;
            fd.cooldownTicks = GerenciadorVooValkyria.COOLDOWN_TICKS;
            PacketDistributor.sendToPlayer(player, new PacoteVooValkyria(true, fd.ticksFlown, fd.cooldownTicks));
            return;
        }

        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.getAbilities().setFlyingSpeed(GerenciadorVooValkyria.FLIGHT_SPEED);
            player.onUpdateAbilities();
        }

        if (player.getAbilities().flying) {
            fd.ticksFlown++;
            fd.wasFlying = true;
        } else {
            if (fd.wasFlying) {
                player.getAbilities().setFlyingSpeed(GerenciadorVooValkyria.FLIGHT_SPEED);
                player.onUpdateAbilities();
                fd.wasFlying = false;
            }
        }

        PacketDistributor.sendToPlayer(player, new PacoteVooValkyria(true, fd.ticksFlown, 0));
    }

    private static void forceDisableFlight(ServerPlayer player) {
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> creativeFlight =
                GerenciadorRaca.getHolder("neoforge:creative_flight");
        if (creativeFlight != null && player.getAttributes().hasAttribute(creativeFlight)) {
            AttributeInstance attr = player.getAttribute(creativeFlight);
            if (attr != null && attr.getBaseValue() != 0.0) {
                attr.setBaseValue(0.0);
            }
        }

        if (player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }
}