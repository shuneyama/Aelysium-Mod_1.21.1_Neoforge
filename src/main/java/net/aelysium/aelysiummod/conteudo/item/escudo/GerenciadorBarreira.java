package net.aelysium.aelysiummod.conteudo.item.escudo;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.aelysium.aelysiummod.conteudo.entidade.EntidadeBarreiraPainel;
import net.aelysium.aelysiummod.registro.ModEntidades;
import net.aelysium.aelysiummod.conteudo.item.escudo.EscudoAbismoItem;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class GerenciadorBarreira {

    public static final int TICKS_CARGA = 100;
    public static final String CHAVE_CARGA = "aelysiummod_abismo_carga";

    private static final double METADE = 2.5;
    private static final double ALTURA_CENTRO = 1.0;
    private static final int DURACAO = 200;
    private static final float VIDA_BARREIRA = 100.0F;
    private static final int LENTIDAO_GERAL_TICKS = 100;

    private static final DustParticleOptions CARMESIM = new DustParticleOptions(new Vector3f(0.78F, 0.04F, 0.2F), 1.3F);

    private static final Map<UUID, Barreira> BARREIRAS = new ConcurrentHashMap<>();

    private static final class Barreira {
        final UUID dono;
        final ServerLevel level;
        final Vec3 centro;
        final long fim;
        final List<EntidadeBarreiraPainel> paineis = new ArrayList<>();
        float vida = VIDA_BARREIRA;

        Barreira(UUID dono, ServerLevel level, Vec3 centro, long fim) {
            this.dono = dono;
            this.level = level;
            this.centro = centro;
            this.fim = fim;
        }

        AABB area() {
            return new AABB(centro.x - METADE, centro.y - METADE, centro.z - METADE,
                    centro.x + METADE, centro.y + METADE, centro.z + METADE);
        }

        boolean contem(Entity entidade) {
            return area().contains(entidade.getBoundingBox().getCenter());
        }
    }

    public static boolean existe(UUID dono) {
        return BARREIRAS.containsKey(dono);
    }

    public static void criar(ServerPlayer dono) {
        ServerLevel level = dono.serverLevel();
        Vec3 centro = new Vec3(dono.getX(), dono.getY() + ALTURA_CENTRO, dono.getZ());
        Barreira barreira = new Barreira(dono.getUUID(), level, centro, level.getGameTime() + DURACAO);

        Vec3[] normais = {
                new Vec3(1, 0, 0), new Vec3(-1, 0, 0),
                new Vec3(0, 1, 0), new Vec3(0, -1, 0),
                new Vec3(0, 0, 1), new Vec3(0, 0, -1)
        };
        for (int face = 0; face < 6; face++) {
            EntidadeBarreiraPainel painel = new EntidadeBarreiraPainel(ModEntidades.BARREIRA_PAINEL.get(), level);
            Vec3 posicao = centro.add(normais[face].scale(METADE));
            painel.setPos(posicao.x, posicao.y, posicao.z);
            painel.configurar(dono.getUUID(), face);
            level.addFreshEntity(painel);
            barreira.paineis.add(painel);
        }

        BARREIRAS.put(dono.getUUID(), barreira);
        level.playSound(null, centro.x, centro.y, centro.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 0.8F);
        explosao(level, centro, 120);
    }

    public static boolean danificar(UUID dono, DamageSource fonte, float dano) {
        Barreira barreira = BARREIRAS.get(dono);
        if (barreira == null) {
            return false;
        }

        Entity atacante = fonte.getEntity();
        if (atacante == null) {
            if (!fonte.is(DamageTypeTags.IS_EXPLOSION)) {
                return false;
            }
        } else if (atacante.getUUID().equals(dono) || barreira.contem(atacante)) {
            return false;
        }

        barreira.vida -= dano;
        barreira.level.playSound(null, barreira.centro.x, barreira.centro.y, barreira.centro.z, SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 1.2F, 0.8F);
        if (barreira.vida <= 0.0F) {
            destruir(barreira);
        }
        return true;
    }

    private static void destruir(Barreira barreira) {
        if (BARREIRAS.remove(barreira.dono, barreira)) {
            for (EntidadeBarreiraPainel painel : barreira.paineis) {
                painel.discard();
            }

            ServerLevel level = barreira.level;
            level.playSound(null, barreira.centro.x, barreira.centro.y, barreira.centro.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.6F);
            level.playSound(null, barreira.centro.x, barreira.centro.y, barreira.centro.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.2F, 0.9F);
            explosao(level, barreira.centro, 200);

            aplicarResultado(barreira);
        }
    }

    private static void aplicarResultado(Barreira barreira) {
        List<ServerPlayer> dentro = barreira.level.getEntitiesOfClass(ServerPlayer.class, barreira.area(),
                jogador -> jogador.isAlive() && !jogador.isSpectator() && barreira.contem(jogador));
        int quantidade = dentro.size();

        ServerPlayer dono = barreira.level.getServer().getPlayerList().getPlayer(barreira.dono);
        boolean donoVivo = dono != null && dono.isAlive();

        float cura;
        int lentidaoNivel;
        int lentidaoTicks;
        int vidaMaxNivel;
        int vidaMaxTicks;
        boolean sobrecarga = false;

        if (quantidade <= 3) {
            cura = 6.0F;
            lentidaoNivel = 0;
            lentidaoTicks = 100;
            vidaMaxNivel = 0;
            vidaMaxTicks = 200;
        } else if (quantidade <= 6) {
            cura = 4.0F;
            lentidaoNivel = 1;
            lentidaoTicks = 200;
            vidaMaxNivel = 1;
            vidaMaxTicks = 300;
        } else if (quantidade <= 9) {
            cura = 2.0F;
            lentidaoNivel = 2;
            lentidaoTicks = 300;
            vidaMaxNivel = 2;
            vidaMaxTicks = 400;
        } else {
            cura = 0.0F;
            lentidaoNivel = 2;
            lentidaoTicks = 300;
            vidaMaxNivel = 3;
            vidaMaxTicks = 500;
            sobrecarga = true;
        }

        for (ServerPlayer jogador : dentro) {
            if (cura > 0.0F) {
                jogador.heal(cura);
            }
            if (sobrecarga) {
                jogador.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, LENTIDAO_GERAL_TICKS, 0));
            }
        }

        if (donoVivo) {
            dono.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, lentidaoTicks, lentidaoNivel));
            dono.addEffect(new MobEffectInstance(ModEfeitos.EXAUSTAO_ABISMO, vidaMaxTicks, vidaMaxNivel));
            if (sobrecarga) {
                dono.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
            }
        }
    }

    private static void explosao(ServerLevel level, Vec3 centro, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            int eixo = level.random.nextInt(3);
            double a = (level.random.nextDouble() * 2.0 - 1.0) * METADE;
            double b = (level.random.nextDouble() * 2.0 - 1.0) * METADE;
            double lado = level.random.nextBoolean() ? METADE : -METADE;
            double x = centro.x + (eixo == 0 ? lado : a);
            double y = centro.y + (eixo == 1 ? lado : (eixo == 0 ? a : b));
            double z = centro.z + (eixo == 2 ? lado : b);
            level.sendParticles(CARMESIM, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @SubscribeEvent
    public static void aoTickarServidor(ServerTickEvent.Post event) {
        if (BARREIRAS.isEmpty()) {
            return;
        }

        Iterator<Barreira> iterador = new ArrayList<>(BARREIRAS.values()).iterator();
        while (iterador.hasNext()) {
            Barreira barreira = iterador.next();
            ServerPlayer dono = barreira.level.getServer().getPlayerList().getPlayer(barreira.dono);
            boolean donoValido = dono != null && dono.isAlive() && dono.level() == barreira.level;

            if (!donoValido || barreira.level.getGameTime() >= barreira.fim) {
                destruir(barreira);
                continue;
            }

            if (barreira.level.getGameTime() % 10 == 0) {
                explosao(barreira.level, barreira.centro, 24);
            }
        }
    }

    @SubscribeEvent
    public static void aoMorrer(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player jogador) {
            Barreira barreira = BARREIRAS.get(jogador.getUUID());
            if (barreira != null) {
                destruir(barreira);
            }
        }
    }

    @SubscribeEvent
    public static void aoSair(PlayerEvent.PlayerLoggedOutEvent event) {
        Barreira barreira = BARREIRAS.get(event.getEntity().getUUID());
        if (barreira != null) {
            destruir(barreira);
        }
    }

    @SubscribeEvent
    public static void aoSofrerDano(LivingDamageEvent.Post event) {
        LivingEntity entidade = event.getEntity();
        if (!(entidade instanceof ServerPlayer jogador) || event.getNewDamage() <= 0.0F) {
            return;
        }
        if (jogador.isUsingItem() && jogador.getUseItem().getItem() instanceof EscudoAbismoItem) {
            jogador.getPersistentData().putInt(CHAVE_CARGA, 0);
            jogador.stopUsingItem();
        }
    }
}
