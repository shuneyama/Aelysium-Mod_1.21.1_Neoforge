package net.aelysium.aelysiummod.sistemas.mundo.flora.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.flora.ModFlora;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class ClienteFlora {

    private static final ColorResolver FOLHAGEM_SECA = (bioma, x, z) ->
            CoresFolhagemSeca.obter(bioma.getBaseTemperature(), bioma.getModifiedClimateSettings().downfall());

    @SubscribeEvent
    public static void recarga(RegisterClientReloadListenersEvent evento) {
        evento.registerReloadListener(new CoresFolhagemSeca());
    }

    @SubscribeEvent
    public static void particulas(RegisterParticleProvidersEvent evento) {
        evento.registerSpriteSet(ModFlora.PARTICULA_VAGALUME.get(), ParticulaVagalume.Provedor::new);
        evento.registerSpriteSet(net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.ModJardimPalido.PARTICULA_FOLHAS.get(), net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.cliente.ParticulaFolhasPalidas.Provedor::new);
    }

    @SubscribeEvent
    public static void coresBlocos(RegisterColorHandlersEvent.Block evento) {
        evento.register((estado, mundo, pos, indice) -> mundo != null && pos != null
                ? mundo.getBlockTint(pos, FOLHAGEM_SECA) : CoresFolhagemSeca.PADRAO, ModFlora.FOLHAS_SECAS.get());
        evento.register((estado, mundo, pos, indice) -> mundo != null && pos != null
                ? BiomeColors.getAverageGrassColor(mundo, pos) : GrassColor.getDefaultColor(), ModFlora.ARBUSTO.get());
        evento.register((estado, mundo, pos, indice) -> indice == 1 && mundo != null && pos != null
                ? BiomeColors.getAverageGrassColor(mundo, pos) : indice == 1 ? GrassColor.getDefaultColor() : -1, ModFlora.FLORES_SELVAGENS.get());
    }

    @SubscribeEvent
    public static void coresItens(RegisterColorHandlersEvent.Item evento) {
        evento.register((pilha, indice) -> GrassColor.get(0.5D, 1.0D), ModFlora.ITEM_ARBUSTO.get());
    }

    public static class ParticulaVagalume extends TextureSheetParticle {

        private ParticulaVagalume(ClientLevel mundo, double x, double y, double z, double xa, double ya, double za, SpriteSet sprites) {
            super(mundo, x, y, z, xa, ya, za);
            this.pickSprite(sprites);
            this.speedUpWhenYMotionIsBlocked = true;
            this.friction = 0.96F;
            this.quadSize *= 0.75F;
            this.yd *= 0.8D;
            this.xd *= 0.8D;
            this.zd *= 0.8D;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        @Override
        public int getLightColor(float parcial) {
            int brilho = (int) (15.0F * fade(progresso(this.age + parcial), 0.1F, 0.3F));
            return net.minecraft.client.renderer.LightTexture.pack(brilho, brilho);
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z)).isAir()) {
                this.remove();
            } else {
                this.setAlpha(fade(progresso(this.age), 0.3F, 0.5F));
                if (this.random.nextFloat() > 0.95F || this.age == 1) {
                    this.setParticleSpeed(-0.05F + 0.1F * this.random.nextFloat(), -0.05F + 0.1F * this.random.nextFloat(), -0.05F + 0.1F * this.random.nextFloat());
                }
            }
        }

        private float progresso(float idade) {
            return Mth.clamp(idade / this.lifetime, 0.0F, 1.0F);
        }

        private static float fade(float progresso, float entrada, float saida) {
            if (progresso >= 1.0F - entrada) return (1.0F - progresso) / entrada;
            return progresso <= saida ? progresso / saida : 1.0F;
        }

        public static class Provedor implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provedor(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Override
            public net.minecraft.client.particle.Particle createParticle(SimpleParticleType tipo, ClientLevel mundo, double x, double y, double z, double xa, double ya, double za) {
                RandomSource aleatorio = mundo.getRandom();
                ParticulaVagalume particula = new ParticulaVagalume(mundo, x, y, z, 0.5D - aleatorio.nextDouble(),
                        aleatorio.nextBoolean() ? ya : -ya, 0.5D - aleatorio.nextDouble(), sprites);
                particula.setLifetime(aleatorio.nextIntBetweenInclusive(200, 300));
                particula.scale(1.5F);
                particula.setAlpha(0.0F);
                return particula;
            }
        }
    }
}
