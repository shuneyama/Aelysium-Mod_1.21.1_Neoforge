package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.servidor;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.EntidadesAtivas;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.RegistroFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.EntityFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.rede.MessageFreezeEffect;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class EventosFrostmaw {
    private static final EntidadesAtivas<EntityFrostmaw> FROSTMAWS = new EntidadesAtivas<>();

    private EventosFrostmaw() {
    }

    private static boolean congelado(LivingEntity entidade) {
        return entidade.hasEffect(RegistroFrostmaw.FROZEN);
    }

    private static void enviarCongelamento(LivingEntity entidade, boolean congelado) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entidade, new MessageFreezeEffect(entidade.getId(), congelado));
    }

    private static void descongelar(LivingEntity entidade) {
        entidade.getData(RegistroFrostmaw.FROZEN_DATA).onUnfreeze(entidade);
        enviarCongelamento(entidade, false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registrarFrostmaw(EntityJoinLevelEvent evento) {
        if (!evento.getLevel().isClientSide() && evento.getEntity() instanceof EntityFrostmaw frostmaw) FROSTMAWS.adicionar(frostmaw);
    }

    @SubscribeEvent
    public static void removerFrostmaw(EntityLeaveLevelEvent evento) {
        if (!evento.getLevel().isClientSide() && evento.getEntity() instanceof EntityFrostmaw frostmaw) FROSTMAWS.remover(frostmaw);
    }

    @SubscribeEvent
    public static void aoEntrarNoNivel(EntityJoinLevelEvent evento) {
        if (evento.getLevel().isClientSide()) return;
        Entity entidade = evento.getEntity();
        if (entidade instanceof Animal animal) {
            animal.goalSelector.addGoal(3, new AvoidEntityGoal<>(animal, EntityFrostmaw.class, 10.0F, 1.0D, 1.2D) {
                @Override
                public boolean canUse() {
                    return FROSTMAWS.existeEm(animal.level()) && !(animal instanceof TamableAnimal domavel && domavel.isTame()) && super.canUse();
                }
            });
        }
        if (entidade instanceof AbstractVillager aldeao) {
            aldeao.goalSelector.addGoal(3, new AvoidEntityGoal<>((PathfinderMob) aldeao, EntityFrostmaw.class, 10.0F, 1.0D, 1.2D) {
                @Override
                public boolean canUse() {
                    return FROSTMAWS.existeEm(aldeao.level()) && super.canUse();
                }
            });
        }
    }

    @SubscribeEvent
    public static void aoTickEntidade(EntityTickEvent.Pre evento) {
        if (evento.getEntity() instanceof LivingEntity vivo && vivo.hasData(RegistroFrostmaw.FROZEN_DATA)) {
            vivo.getData(RegistroFrostmaw.FROZEN_DATA).tick(vivo);
        }
    }

    @SubscribeEvent
    public static void aoAdicionarEfeito(MobEffectEvent.Added evento) {
        if (evento.getEntity().level().isClientSide()) return;
        if (evento.getEffectInstance().getEffect().equals(RegistroFrostmaw.FROZEN)) {
            evento.getEntity().getData(RegistroFrostmaw.FROZEN_DATA).onFreeze(evento.getEntity());
            enviarCongelamento(evento.getEntity(), true);
        }
    }

    @SubscribeEvent
    public static void aoRemoverEfeito(MobEffectEvent.Remove evento) {
        if (evento.getEntity().level().isClientSide() || evento.getEffectInstance() == null) return;
        if (evento.getEffectInstance().getEffect().equals(RegistroFrostmaw.FROZEN)) {
            descongelar(evento.getEntity());
        }
    }

    @SubscribeEvent
    public static void aoExpirarEfeito(MobEffectEvent.Expired evento) {
        if (evento.getEntity().level().isClientSide() || evento.getEffectInstance() == null) return;
        if (evento.getEffectInstance().getEffect().equals(RegistroFrostmaw.FROZEN)) {
            descongelar(evento.getEntity());
        }
    }

    @SubscribeEvent
    public static void aoSofrerDano(LivingDamageEvent.Post evento) {
        LivingEntity entidade = evento.getEntity();
        if (entidade.level().isClientSide()) return;
        if (evento.getSource().is(DamageTypeTags.IS_FIRE) && congelado(entidade)) {
            entidade.removeEffectNoUpdate(RegistroFrostmaw.FROZEN);
            descongelar(entidade);
        } else if (entidade.getHealth() <= 0 && congelado(entidade)) {
            entidade.removeEffectNoUpdate(RegistroFrostmaw.FROZEN);
            descongelar(entidade);
        }
    }

    @SubscribeEvent
    public static void aoPular(LivingEvent.LivingJumpEvent evento) {
        LivingEntity entidade = evento.getEntity();
        if (congelado(entidade) && entidade.onGround()) {
            entidade.setDeltaMovement(entidade.getDeltaMovement().multiply(1, 0, 1));
        }
    }

    @SubscribeEvent
    public static void aoUsarItem(LivingEntityUseItemEvent.Start evento) {
        if (congelado(evento.getEntity())) evento.setCanceled(true);
    }

    @SubscribeEvent
    public static void aoColocarBloco(BlockEvent.EntityPlaceEvent evento) {
        if (evento.getEntity() instanceof LivingEntity vivo && congelado(vivo)) evento.setCanceled(true);
    }

    @SubscribeEvent
    public static void aoQuebrarBloco(BlockEvent.BreakEvent evento) {
        if (congelado(evento.getPlayer())) evento.setCanceled(true);
    }

    @SubscribeEvent
    public static void aoInteragirEntidade(PlayerInteractEvent.EntityInteract evento) {
        if (congelado(evento.getEntity())) evento.setCanceled(true);
    }

    @SubscribeEvent
    public static void aoClicarBloco(PlayerInteractEvent.RightClickBlock evento) {
        if (congelado(evento.getEntity())) evento.setCanceled(true);
    }

    @SubscribeEvent
    public static void aoClicarItem(PlayerInteractEvent.RightClickItem evento) {
        if (congelado(evento.getEntity())) evento.setCanceled(true);
    }

    @SubscribeEvent
    public static void aoBaterBloco(PlayerInteractEvent.LeftClickBlock evento) {
        if (congelado(evento.getEntity())) evento.setCanceled(true);
    }

    @SubscribeEvent
    public static void aoAtacar(AttackEntityEvent evento) {
        if (congelado(evento.getEntity())) evento.setCanceled(true);
    }
}
