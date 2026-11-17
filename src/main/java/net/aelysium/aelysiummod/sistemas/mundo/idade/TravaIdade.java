package net.aelysium.aelysiummod.sistemas.mundo.idade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class TravaIdade {

    public static final String CHAVE_TRAVADA = "AgeLocked";
    private static final int RECARGA_TICKS = 40;

    public static final TagKey<EntityType<?>> NAO_PODE_TRAVAR = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nao_pode_travar_idade"));

    private static final Map<Entity, Long> RECARGAS = new WeakHashMap<>();

    public static boolean estaTravado(Entity entidade) {
        return entidade.getPersistentData().getBoolean(CHAVE_TRAVADA);
    }

    public static boolean deveBloquearCrescimento(Entity entidade, int idadeAtual, int novaIdade) {
        return novaIdade > idadeAtual && entidade.tickCount > 0 && estaTravado(entidade);
    }

    public static boolean podeTravar(Entity alvo) {
        if (alvo.getType().is(NAO_PODE_TRAVAR)) return false;
        if (alvo instanceof Tadpole) return true;
        return alvo instanceof AgeableMob mob && mob.isBaby();
    }

    @SubscribeEvent
    public void aoInteragirEntidade(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModBlocos.DENTE_DE_LEAO_DOURADO.get().asItem())) return;

        Entity alvo = event.getTarget();
        if (!podeTravar(alvo)) return;

        event.setCanceled(true);

        if (!(alvo.level() instanceof ServerLevel nivel)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        long agora = nivel.getGameTime();
        Long ultimoUso = RECARGAS.get(alvo);
        if (ultimoUso != null && agora - ultimoUso < RECARGA_TICKS) {
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        boolean travar = !estaTravado(alvo);
        if (travar) {
            alvo.getPersistentData().putBoolean(CHAVE_TRAVADA, true);
        } else {
            alvo.getPersistentData().remove(CHAVE_TRAVADA);
        }
        RECARGAS.put(alvo, agora);

        Player jogador = event.getEntity();
        stack.consume(1, jogador);

        nivel.playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.0F, travar ? 0.8F : 1.4F);

        double largura = alvo.getBbWidth() * 0.5;
        double altura = alvo.getBbHeight();
        if (travar) {
            nivel.sendParticles(ParticleTypes.WAX_ON,
                    alvo.getX(), alvo.getY() + altura, alvo.getZ(),
                    14, largura, altura * 0.3, largura, 0.0);
            nivel.sendParticles(ParticleTypes.FALLING_NECTAR,
                    alvo.getX(), alvo.getY() + altura + 0.2, alvo.getZ(),
                    8, largura, 0.1, largura, 0.0);
        } else {
            nivel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    alvo.getX(), alvo.getY() + altura * 0.5, alvo.getZ(),
                    14, largura, altura * 0.5, largura, 0.0);
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public void aoVerificarDespawn(MobDespawnEvent event) {
        if (estaTravado(event.getEntity())) {
            event.setResult(MobDespawnEvent.Result.DENY);
        }
    }

    @SubscribeEvent
    public void aoGerarTrocasAndarilho(WandererTradesEvent event) {
        event.getGenericTrades().add(new BasicItemListing(2,
                new ItemStack(ModBlocos.DENTE_DE_LEAO_DOURADO.get()), 12, 1));
    }
}
