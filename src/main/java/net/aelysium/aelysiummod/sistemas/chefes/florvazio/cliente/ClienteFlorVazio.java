package net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente;

import com.mojang.blaze3d.Blaze3D;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.base.dados.WeakHashPredicate;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.ClientParticleBuilder;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.ParticleContext;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.SimpleParticle;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.SimpleParticleProvider;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.TipoParticulaSimples;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.Geometries;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.MathUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.RandomUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.Vec3Colors;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.animacao.PauseAnimationTimer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.CompositeRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.ConditionalRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.FrameLimiter;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.FullRenderLight;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.LerpedPosRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.NodeBossBarRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.PetalBladeParticleRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.PetalBladeRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.RenderizadorBedrock;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render.SimpleEntityRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.NoRedOnDeathOverlay;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.SpikeAction;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.SpikeWaveAction;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.SporeBallOverlay;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.SporeBallSizeRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.SporeCodeAnimations;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.VoidBlossomBoneLight;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.VoidBlossomCodeAnimations;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.VoidBlossomEntity;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.VoidBlossomSpikeRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.PetalBladeProjectile;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.projetil.SporeBallProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.function.Function;
import java.util.function.Supplier;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class ClienteFlorVazio {

    private static final PauseAnimationTimer TEMPORIZADOR_PAUSA = new PauseAnimationTimer(Blaze3D::getTime, () -> Minecraft.getInstance().isPaused());
    private static NodeBossBarRenderer barraChefe;

    private ClienteFlorVazio() {
    }

    public static NodeBossBarRenderer barraChefe() {
        if (barraChefe == null) {
            barraChefe = new NodeBossBarRenderer(
                    RegistroFlorVazio.VOID_BLOSSOM.get().getDescriptionId(),
                    VoidBlossomEntity.hpMilestones,
                    recurso("textures/gui/chefes/flor_do_vazio_divisores.png"),
                    256
            );
        }
        return barraChefe;
    }

    private static ResourceLocation recurso(String caminho) {
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, caminho);
    }

    @SubscribeEvent
    public static void registrarRenderizadores(EntityRenderersEvent.RegisterRenderers evento) {
        evento.registerEntityRenderer(RegistroFlorVazio.VOID_BLOSSOM.get(), contexto -> {
            VoidBlossomBoneLight luzOssos = new VoidBlossomBoneLight();
            NoRedOnDeathOverlay sobreposicao = new NoRedOnDeathOverlay();
            return new RenderizadorBedrock<VoidBlossomEntity>(
                    contexto,
                    recurso("geo/chefes/flor_do_vazio.geo.json"),
                    recurso("textures/entity/chefes/flor_do_vazio.png"),
                    recurso("animations/chefes/flor_do_vazio.animation.json"),
                    VoidBlossomEntity::getAnimationHolder,
                    new VoidBlossomCodeAnimations(),
                    null,
                    luzOssos,
                    new CompositeRenderer<>(new VoidBlossomSpikeRenderer(), luzOssos, sobreposicao),
                    sobreposicao,
                    false
            );
        });

        evento.registerEntityRenderer(RegistroFlorVazio.SPORE_BALL.get(), contexto -> {
            SporeBallOverlay piscarExplosao = new SporeBallOverlay();
            return new RenderizadorBedrock<SporeBallProjectile>(
                    contexto,
                    recurso("geo/chefes/bola_de_esporos.geo.json"),
                    recurso("textures/entity/chefes/esporo.png"),
                    null,
                    null,
                    new SporeCodeAnimations(),
                    new FullRenderLight<>(),
                    null,
                    new CompositeRenderer<>(
                            new ConditionalRenderer<>(
                                    new WeakHashPredicate<>(() -> new FrameLimiter(60f, TEMPORIZADOR_PAUSA)::canDoFrame),
                                    new LerpedPosRenderer<>(posicao -> {
                                        ClientParticleBuilder particulas = new ClientParticleBuilder(RegistroFlorVazio.OBSIDILITH_BURST.get())
                                                .color(Vec3Colors.GREEN)
                                                .colorVariation(0.4)
                                                .scale(0.5f)
                                                .brightness(RegistroFlorVazio.BRILHO_TOTAL);
                                        particulas.build(posicao.add(RandomUtils.randVec().scale(0.25)), VecUtils.yAxis.scale(0.1));
                                    })),
                            piscarExplosao,
                            new SporeBallSizeRenderer()),
                    piscarExplosao,
                    true);
        });

        ResourceLocation texturaPetala = recurso("textures/entity/chefes/lamina_de_petala.png");
        RenderType tipoPetala = RenderType.entityCutoutNoCull(texturaPetala);
        evento.registerEntityRenderer(RegistroFlorVazio.PETAL_BLADE.get(), contexto ->
                new SimpleEntityRenderer<PetalBladeProjectile>(contexto,
                        new CompositeRenderer<>(
                                new PetalBladeRenderer(contexto.getEntityRenderDispatcher(), tipoPetala),
                                new ConditionalRenderer<>(
                                        new WeakHashPredicate<>(() -> new FrameLimiter(30f, TEMPORIZADOR_PAUSA)::canDoFrame),
                                        new PetalBladeParticleRenderer<>()
                                )),
                        entidade -> texturaPetala,
                        new FullRenderLight<>()));
    }

    @SubscribeEvent
    public static void registrarParticulas(RegisterParticleProvidersEvent evento) {
        simples(evento, RegistroFlorVazio.DISAPPEARING_SWIRL, 15, 20, true);
        simples(evento, RegistroFlorVazio.SPARKLES, 15, 20, true);
        simples(evento, RegistroFlorVazio.LINE, 20, 30, true);
        simples(evento, RegistroFlorVazio.FLUFF, 15, 20, true);
        simples(evento, RegistroFlorVazio.PETAL, 15, 20, false);
        evento.registerSpriteSet(RegistroFlorVazio.OBSIDILITH_BURST.get(), sprites -> new SimpleParticleProvider(sprites, contexto -> {
            SimpleParticle particula = new SimpleParticle(contexto, RandomUtils.range(7, 15), Geometries::buildBillBoardGeometry, true, true);
            particula.setBrightnessOverride(f -> RegistroFlorVazio.BRILHO_TOTAL);
            particula.scale(4f);
            particula.setColorOverride(idade -> MathUtils.lerpVec(idade, Vec3Colors.ORANGE, Vec3Colors.RUNIC_BROWN));
            return particula;
        }));
        evento.registerSpriteSet(RegistroFlorVazio.SPORE.get(), sprites -> new SimpleParticleProvider(sprites, contexto -> {
            SimpleParticle particula = new SimpleParticle(contexto, RandomUtils.range(7, 15), Geometries::buildBillBoardGeometry, true, true);
            particula.setBrightnessOverride(f -> RegistroFlorVazio.BRILHO_TOTAL);
            particula.scale(4f);
            particula.setColorOverride(idade -> MathUtils.lerpVec(idade, Vec3Colors.GREEN, Vec3Colors.DARK_GREEN));
            particula.setColorVariation(0.25);
            return particula;
        }));
        indicador(evento, RegistroFlorVazio.VOID_BLOSSOM_SPIKE_INDICATOR, () -> SpikeAction.indicatorDelay, Vec3Colors.ENDER_PURPLE, 0.2);
        indicador(evento, RegistroFlorVazio.VOID_BLOSSOM_SPIKE_WAVE_INDICATOR, () -> SpikeWaveAction.indicatorDelay, Vec3Colors.ENDER_PURPLE, 0.2);
        indicador(evento, RegistroFlorVazio.SPORE_INDICATOR, () -> SporeBallProjectile.explosionDelay, Vec3Colors.GREEN, 0.35);
    }

    private static void simples(RegisterParticleProvidersEvent evento, Supplier<TipoParticulaSimples> tipo, int minimo, int maximo, boolean girar) {
        evento.registerSpriteSet(tipo.get(), sprites -> new SimpleParticleProvider(sprites,
                contexto -> new SimpleParticle(contexto, RandomUtils.range(minimo, maximo), Geometries::buildBillBoardGeometry, girar, true)));
    }

    private static void indicador(RegisterParticleProvidersEvent evento, Supplier<TipoParticulaSimples> tipo, Supplier<Integer> atraso, Vec3 cor, double variacao) {
        Function<ParticleContext, Particle> fabrica = contexto -> {
            double velocidade = contexto.vel().x > 0 ? contexto.vel().x : 1.0;
            int vida = Math.max((int) Math.round(atraso.get() / velocidade) + RandomUtils.range(-1, 2), 1);
            ParticleContext parado = new ParticleContext(contexto.spriteSet(), contexto.level(), contexto.pos(), Vec3.ZERO, contexto.cycleSprites());
            SimpleParticle particula = new SimpleParticle(parado, vida, Geometries::buildFlatGeometry, true, true);
            particula.setColorOverride(f -> cor);
            particula.setColorVariation(variacao);
            particula.setBrightnessOverride(f -> RegistroFlorVazio.BRILHO_TOTAL);
            particula.setScaleOverride(f -> (1 + f) * 0.25f);
            return particula;
        };
        evento.registerSpriteSet(tipo.get(), sprites -> new SimpleParticleProvider(sprites, fabrica));
    }
}
