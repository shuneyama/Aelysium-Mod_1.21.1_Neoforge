package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.flora.SonsExtras;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class EventosAmarras {

    private static final double ALCANCE = 12.0D;

    private static List<Entity> amarradosA(Level mundo, Entity portador) {
        List<Entity> lista = new ArrayList<>();
        AABB caixa = portador.getBoundingBox().inflate(ALCANCE * 2.0D);
        for (Entity entidade : mundo.getEntities((Entity) null, caixa, e -> e instanceof Leashable)) {
            if (((Leashable) entidade).getLeashHolder() == portador) lista.add(entidade);
        }
        return lista;
    }

    private static double distanciaCentros(Entity a, Entity b) {
        return a.getBoundingBox().getCenter().distanceTo(b.getBoundingBox().getCenter());
    }

    @SubscribeEvent
    public static void interagir(PlayerInteractEvent.EntityInteract evento) {
        Player jogador = evento.getEntity();
        Level mundo = jogador.level();
        Entity alvo = evento.getTarget();
        ItemStack pilha = evento.getItemStack();
        if (evento.getHand() != InteractionHand.MAIN_HAND) return;

        if (pilha.is(Items.SHEARS)) {
            if (alvo instanceof LeashFenceKnotEntity no) {
                if (!mundo.isClientSide) {
                    for (Entity preso : amarradosA(mundo, no)) ((Leashable) preso).dropLeash(true, true);
                    no.discard();
                    cortar(mundo, jogador, alvo, pilha, SonsExtras.SHEARS_SNIP);
                }
                evento.setCancellationResult(InteractionResult.sidedSuccess(mundo.isClientSide));
                evento.setCanceled(true);
                return;
            }
            if (alvo instanceof Leashable amarrado && amarrado.isLeashed()) {
                if (!mundo.isClientSide) {
                    amarrado.dropLeash(true, true);
                    for (Entity preso : amarradosA(mundo, alvo)) ((Leashable) preso).dropLeash(true, true);
                    cortar(mundo, jogador, alvo, pilha, SonsExtras.SHEARS_SNIP);
                }
                evento.setCancellationResult(InteractionResult.sidedSuccess(mundo.isClientSide));
                evento.setCanceled(true);
                return;
            }
            if (!jogador.isShiftKeyDown() && !alvo.isVehicle() && retirarEquipamento(mundo, jogador, alvo, pilha)) {
                evento.setCancellationResult(InteractionResult.sidedSuccess(mundo.isClientSide));
                evento.setCanceled(true);
            }
            return;
        }

        if (jogador.isShiftKeyDown() && !(alvo instanceof Player) && alvo != null) {
            List<Entity> presos = amarradosA(mundo, jogador);
            presos.remove(alvo);
            if (presos.isEmpty()) return;
            if (!mundo.isClientSide) {
                boolean algum = false;
                for (Entity preso : presos) {
                    if (distanciaCentros(preso, alvo) > ALCANCE) continue;
                    ((Leashable) preso).setLeashedTo(alvo, true);
                    algum = true;
                }
                if (algum) mundo.playSound(null, alvo.blockPosition(), SonsExtras.LEAD_TIED, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            evento.setCancellationResult(InteractionResult.sidedSuccess(mundo.isClientSide));
            evento.setCanceled(true);
        }
    }

    private static void cortar(Level mundo, Player jogador, Entity alvo, ItemStack tesoura, SoundEvent som) {
        tesoura.hurtAndBreak(1, jogador, EquipmentSlot.MAINHAND);
        mundo.playSound(null, alvo.blockPosition(), som, SoundSource.PLAYERS, 1.0F, 1.0F);
        mundo.gameEvent(jogador, GameEvent.SHEAR, alvo.position());
    }

    private static boolean retirarEquipamento(Level mundo, Player jogador, Entity alvo, ItemStack tesoura) {
        if (mundo.isClientSide) {
            return alvo instanceof AbstractHorse cavalo && (cavalo.isSaddled() || !cavalo.getBodyArmorItem().isEmpty())
                    || alvo instanceof Pig porco && porco.isSaddled()
                    || alvo instanceof Strider andador && andador.isSaddled();
        }
        if (alvo instanceof AbstractHorse cavalo) {
            ItemStack armadura = cavalo.getBodyArmorItem();
            if (!armadura.isEmpty()) {
                SoundEvent som = armadura.is(net.minecraft.tags.ItemTags.WOOL_CARPETS) ? SonsExtras.TAPETE_RETIRADO : SonsExtras.ARMADURA_RETIRADA;
                cavalo.setBodyArmorItem(ItemStack.EMPTY);
                cavalo.spawnAtLocation(armadura);
                finalizar(mundo, jogador, alvo, tesoura, som);
                return true;
            }
            if (cavalo.isSaddled()) {
                ItemStack sela = cavalo.getSlot(400).get().copy();
                cavalo.getSlot(400).set(ItemStack.EMPTY);
                cavalo.spawnAtLocation(sela.isEmpty() ? new ItemStack(Items.SADDLE) : sela);
                finalizar(mundo, jogador, alvo, tesoura, SonsExtras.SELA_RETIRADA);
                return true;
            }
            return false;
        }
        if (alvo instanceof Pig porco && porco.isSaddled()) {
            porco.steering.setSaddle(false);
            porco.spawnAtLocation(new ItemStack(Items.SADDLE));
            finalizar(mundo, jogador, alvo, tesoura, SonsExtras.SELA_RETIRADA);
            return true;
        }
        if (alvo instanceof Strider andador && andador.isSaddled()) {
            andador.steering.setSaddle(false);
            andador.spawnAtLocation(new ItemStack(Items.SADDLE));
            finalizar(mundo, jogador, alvo, tesoura, SonsExtras.SELA_RETIRADA);
            return true;
        }
        return false;
    }

    private static void finalizar(Level mundo, Player jogador, Entity alvo, ItemStack tesoura, SoundEvent som) {
        cortar(mundo, jogador, alvo, tesoura, som);
        mundo.gameEvent(jogador, GameEvent.UNEQUIP, alvo.position());
    }

    @SubscribeEvent
    public static void foguete(PlayerInteractEvent.RightClickItem evento) {
        Player jogador = evento.getEntity();
        if (jogador.level().isClientSide || !jogador.isFallFlying() || !evento.getItemStack().is(Items.FIREWORK_ROCKET)) return;
        for (Entity preso : amarradosA(jogador.level(), jogador)) ((Leashable) preso).dropLeash(true, true);
    }

    @EventBusSubscriber(modid = AelysiumMod.MOD_ID)
    public static class Configuracao {
        @SubscribeEvent
        public static void dispensador(FMLCommonSetupEvent evento) {
            evento.enqueueWork(() -> {
                DispenseItemBehavior original = DispenserBlock.DISPENSER_REGISTRY.get(Items.SHEARS);
                DispenserBlock.registerBehavior(Items.SHEARS, (fonte, pilha) -> {
                    Level mundo = fonte.level();
                    BlockPos frente = fonte.pos().relative(fonte.state().getValue(DispenserBlock.FACING));
                    List<Entity> alvos = mundo.getEntities((Entity) null, new AABB(frente), e -> e instanceof Leashable l && l.isLeashed());
                    if (!alvos.isEmpty() && !mundo.isClientSide) {
                        for (Entity alvo : alvos) ((Leashable) alvo).dropLeash(true, true);
                        pilha.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel) mundo, null, item -> {});
                        mundo.playSound(null, frente, SonsExtras.SHEARS_SNIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return pilha;
                    }
                    return original != null ? original.dispense(fonte, pilha) : pilha;
                });
            });
        }
    }
}
