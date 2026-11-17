package net.aelysium.aelysiummod.conteudo.filhotes.aquatico;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.filhotes.aquatico.TamanhoSalmao;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class EventosAquaticos {

    @SubscribeEvent
    public static void aoFinalizarSpawn(FinalizeSpawnEvent evento) {
        if (evento.getEntity() instanceof Salmon salmao) {
            if (evento.getSpawnType() != MobSpawnType.BUCKET) {
                TamanhoSalmao.definir(salmao, TamanhoSalmao.sortear(salmao.getRandom()));
            }
            return;
        }
        if (!IdadeAquatica.aplicavel(evento.getEntity())) return;
        if (evento.getLevel().getRandom().nextFloat() < IdadeAquatica.chanceNascer(evento.getEntity())) {
            IdadeAquatica.tornarFilhote(evento.getEntity(), IdadeAquatica.DURACAO);
        }
    }

    @SubscribeEvent
    public static void aoTicar(EntityTickEvent.Post evento) {
        if (!(evento.getEntity() instanceof LivingEntity vivo) || vivo.level().isClientSide) return;
        if (!IdadeAquatica.aplicavel(vivo)) return;
        var dados = vivo.getPersistentData();
        if (!dados.contains(IdadeAquatica.CHAVE)) return;
        int restante = dados.getInt(IdadeAquatica.CHAVE) - 1;
        if (restante <= 0) {
            IdadeAquatica.crescer(vivo);
        } else {
            dados.putInt(IdadeAquatica.CHAVE, restante);
        }
    }

    @SubscribeEvent
    public static void aoInteragir(PlayerInteractEvent.EntityInteract evento) {
        if (evento.getLevel().isClientSide() || !(evento.getTarget() instanceof Dolphin golfinho)) return;
        if (!IdadeAquatica.ehFilhote(golfinho)) return;
        ItemStack item = evento.getItemStack();
        if (item.isEmpty() || !item.is(ItemTags.FISHES)) return;
        var dados = golfinho.getPersistentData();
        int restante = dados.getInt(IdadeAquatica.CHAVE);
        dados.putInt(IdadeAquatica.CHAVE, Math.max(1, restante - restante / 10));
    }

    @SubscribeEvent
    public static void aoUsarOvo(PlayerInteractEvent.EntityInteract evento) {
        if (!IdadeAquatica.aplicavel(evento.getTarget())) return;
        ItemStack item = evento.getItemStack();
        if (!(item.getItem() instanceof SpawnEggItem ovo)) return;
        Entity alvo = evento.getTarget();
        if (!ovo.spawnsEntity(item, alvo.getType())) return;
        evento.setCancellationResult(InteractionResult.SUCCESS);
        evento.setCanceled(true);
        if (!(evento.getLevel() instanceof ServerLevel nivel)) return;
        Entity nova = alvo.getType().create(nivel);
        if (!(nova instanceof Mob mob)) return;
        mob.moveTo(alvo.getX(), alvo.getY(), alvo.getZ(), 0.0F, 0.0F);
        mob.finalizeSpawn(nivel, nivel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWN_EGG, null);
        IdadeAquatica.tornarFilhote(mob, IdadeAquatica.DURACAO);
        if (item.has(DataComponents.CUSTOM_NAME)) mob.setCustomName(item.getHoverName());
        nivel.addFreshEntityWithPassengers(mob);
        if (!evento.getEntity().getAbilities().instabuild) item.shrink(1);
    }

    @SubscribeEvent
    public static void aoMudarAlvo(LivingChangeTargetEvent evento) {
        if (evento.getEntity() instanceof Dolphin golfinho && evento.getNewAboutToBeSetTarget() != null && IdadeAquatica.ehFilhote(golfinho)) {
            evento.setNewAboutToBeSetTarget(null);
        }
    }
}
