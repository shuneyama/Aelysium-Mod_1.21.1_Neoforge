package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasMonstrosFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada.CamadaArmaduraFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada.CamadaCabecaFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada.CamadaExternaAfogadoFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada.CamadaItemFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.camada.CamadaTipoAldeaoFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloAfogadoFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloAldeaoFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloAldeaoZumbiFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloHoglinFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloPiglinFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloZumbiFilhote;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.client.renderer.entity.HuskRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.ZoglinRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.ZombieVillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class RenderizadoresMonstros {

    private static final ResourceLocation ZUMBI = UtilFilhotes.textura("filhotes/zombie/zombie_baby");
    private static final ResourceLocation HUSK = UtilFilhotes.textura("filhotes/zombie/husk_baby");
    private static final ResourceLocation AFOGADO = UtilFilhotes.textura("filhotes/zombie/drowned_baby");
    private static final ResourceLocation ALDEAO_ZUMBI = UtilFilhotes.textura("filhotes/zombie_villager/zombie_villager_baby");
    private static final ResourceLocation ALDEAO = UtilFilhotes.textura("filhotes/villager/villager_baby");
    private static final ResourceLocation PIGLIN = UtilFilhotes.textura("filhotes/piglin/piglin_baby");
    private static final ResourceLocation PIGLIN_ZUMBI = UtilFilhotes.textura("filhotes/piglin/zombified_piglin_baby");
    private static final ResourceLocation HOGLIN = UtilFilhotes.textura("filhotes/hoglin/hoglin_baby");
    private static final ResourceLocation ZOGLIN = UtilFilhotes.textura("filhotes/hoglin/zoglin_baby");

    private RenderizadoresMonstros() {
    }

    public static void registrar(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.ZOMBIE, Zumbi::new);
        event.registerEntityRenderer(EntityType.HUSK, Husk::new);
        event.registerEntityRenderer(EntityType.DROWNED, Afogado::new);
        event.registerEntityRenderer(EntityType.ZOMBIE_VILLAGER, AldeaoZumbi::new);
        event.registerEntityRenderer(EntityType.VILLAGER, Aldeao::new);
        event.registerEntityRenderer(EntityType.PIGLIN, contexto -> new Piglin(contexto, ModelLayers.PIGLIN, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, false));
        event.registerEntityRenderer(EntityType.ZOMBIFIED_PIGLIN, contexto -> new Piglin(contexto, ModelLayers.ZOMBIFIED_PIGLIN, ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR, ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR, true));
        event.registerEntityRenderer(EntityType.HOGLIN, Porcoespinho::new);
        event.registerEntityRenderer(EntityType.ZOGLIN, Zoglino::new);
    }

    public static class Zumbi extends ZombieRenderer {

        private final Zumbi filhote;

        public Zumbi(EntityRendererProvider.Context contexto) {
            this(contexto, false);
        }

        private Zumbi(EntityRendererProvider.Context contexto, boolean ehFilhote) {
            super(contexto);
            if (ehFilhote) {
                this.model = new ModeloZumbiFilhote<>(contexto.bakeLayer(CamadasMonstrosFilhotes.ZUMBI));
                this.layers.clear();
                this.addLayer(new CamadaCabecaFilhote<>(this, contexto.getModelSet(), contexto.getItemInHandRenderer(), 1.0F, 1.0F, 1.0F));
                this.addLayer(new CamadaItemFilhote<>(this, contexto.getItemInHandRenderer()));
                this.addLayer(new CamadaArmaduraFilhote<>(this, contexto.getModelSet(), CamadasMonstrosFilhotes.ARMADURA, 0.5F));
                this.filhote = null;
            } else {
                this.filhote = new Zumbi(contexto, true);
            }
        }

        @Override
        public void render(Zombie entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (filhote != null && entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }

        @Override
        public ResourceLocation getTextureLocation(Zombie entidade) {
            return filhote == null ? ZUMBI : super.getTextureLocation(entidade);
        }
    }

    public static class Husk extends HuskRenderer {

        private final Husk filhote;

        public Husk(EntityRendererProvider.Context contexto) {
            this(contexto, false);
        }

        private Husk(EntityRendererProvider.Context contexto, boolean ehFilhote) {
            super(contexto);
            if (ehFilhote) {
                this.model = new ModeloZumbiFilhote<>(contexto.bakeLayer(CamadasMonstrosFilhotes.ZUMBI));
                this.layers.clear();
                this.addLayer(new CamadaCabecaFilhote<>(this, contexto.getModelSet(), contexto.getItemInHandRenderer(), 1.0F, 1.0F, 1.0F));
                this.addLayer(new CamadaItemFilhote<>(this, contexto.getItemInHandRenderer()));
                this.addLayer(new CamadaArmaduraFilhote<>(this, contexto.getModelSet(), CamadasMonstrosFilhotes.ARMADURA, 0.5F));
                this.filhote = null;
            } else {
                this.filhote = new Husk(contexto, true);
            }
        }

        @Override
        public void render(Zombie entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (filhote != null && entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }

        @Override
        protected void scale(Zombie entidade, PoseStack poseStack, float parcial) {
            if (filhote == null) return;
            super.scale(entidade, poseStack, parcial);
        }

        @Override
        public ResourceLocation getTextureLocation(Zombie entidade) {
            return filhote == null ? HUSK : super.getTextureLocation(entidade);
        }
    }

    public static class Afogado extends DrownedRenderer {

        private final Afogado filhote;

        public Afogado(EntityRendererProvider.Context contexto) {
            this(contexto, false);
        }

        private Afogado(EntityRendererProvider.Context contexto, boolean ehFilhote) {
            super(contexto);
            if (ehFilhote) {
                this.model = new ModeloAfogadoFilhote<>(contexto.bakeLayer(CamadasMonstrosFilhotes.ZUMBI));
                this.layers.clear();
                this.addLayer(new CamadaCabecaFilhote<>(this, contexto.getModelSet(), contexto.getItemInHandRenderer(), 1.0F, 1.0F, 1.0F));
                this.addLayer(new CamadaItemFilhote<>(this, contexto.getItemInHandRenderer()));
                this.addLayer(new CamadaArmaduraFilhote<>(this, contexto.getModelSet(), CamadasMonstrosFilhotes.ARMADURA, 0.5F));
                this.addLayer(new CamadaExternaAfogadoFilhote<>(this, contexto.getModelSet()));
                this.filhote = null;
            } else {
                this.filhote = new Afogado(contexto, true);
            }
        }

        @Override
        public void render(Drowned entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (filhote != null && entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }

        @Override
        public ResourceLocation getTextureLocation(Zombie entidade) {
            return filhote == null ? AFOGADO : super.getTextureLocation(entidade);
        }
    }

    public static class AldeaoZumbi extends ZombieVillagerRenderer {

        private final AldeaoZumbi filhote;

        public AldeaoZumbi(EntityRendererProvider.Context contexto) {
            this(contexto, false);
        }

        private AldeaoZumbi(EntityRendererProvider.Context contexto, boolean ehFilhote) {
            super(contexto);
            if (ehFilhote) {
                this.model = new ModeloAldeaoZumbiFilhote<>(contexto.bakeLayer(CamadasMonstrosFilhotes.ALDEAO_ZUMBI));
                this.layers.clear();
                this.addLayer(new CamadaCabecaFilhote<>(this, contexto.getModelSet(), contexto.getItemInHandRenderer(), 1.0F, 1.0F, 1.0F));
                this.addLayer(new CamadaItemFilhote<>(this, contexto.getItemInHandRenderer()));
                this.addLayer(new CamadaArmaduraFilhote<>(this, contexto.getModelSet(), CamadasMonstrosFilhotes.ARMADURA, 0.5F));
                this.addLayer(new CamadaTipoAldeaoFilhote<>(this, "zombie_villager"));
                this.filhote = null;
            } else {
                this.filhote = new AldeaoZumbi(contexto, true);
            }
        }

        @Override
        public void render(ZombieVillager entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (filhote != null && entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }

        @Override
        public ResourceLocation getTextureLocation(ZombieVillager entidade) {
            return filhote == null ? ALDEAO_ZUMBI : super.getTextureLocation(entidade);
        }
    }

    public static class Aldeao extends VillagerRenderer {

        private final Aldeao filhote;

        public Aldeao(EntityRendererProvider.Context contexto) {
            this(contexto, false);
        }

        private Aldeao(EntityRendererProvider.Context contexto, boolean ehFilhote) {
            super(contexto);
            if (ehFilhote) {
                this.model = new ModeloAldeaoFilhote<>(contexto.bakeLayer(CamadasMonstrosFilhotes.ALDEAO));
                this.layers.clear();
                this.addLayer(new CamadaCabecaFilhote<>(this, contexto.getModelSet(), contexto.getItemInHandRenderer(), 1.0F, 1.0F, 1.0F));
                this.addLayer(new CamadaTipoAldeaoFilhote<>(this, "villager"));
                this.filhote = null;
            } else {
                this.filhote = new Aldeao(contexto, true);
            }
        }

        @Override
        public void render(Villager entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (filhote != null && entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }

        @Override
        protected void scale(Villager entidade, PoseStack poseStack, float parcial) {
            if (filhote == null) {
                poseStack.scale(0.9375F, 0.9375F, 0.9375F);
                return;
            }
            super.scale(entidade, poseStack, parcial);
        }

        @Override
        public ResourceLocation getTextureLocation(Villager entidade) {
            return filhote == null ? ALDEAO : super.getTextureLocation(entidade);
        }
    }

    public static class Piglin extends PiglinRenderer {

        private final Piglin filhote;
        private final boolean zumbificado;

        public Piglin(EntityRendererProvider.Context contexto, ModelLayerLocation camada, ModelLayerLocation interna, ModelLayerLocation externa, boolean semOrelhaDireita) {
            this(contexto, camada, interna, externa, semOrelhaDireita, false);
        }

        private Piglin(EntityRendererProvider.Context contexto, ModelLayerLocation camada, ModelLayerLocation interna, ModelLayerLocation externa, boolean semOrelhaDireita, boolean ehFilhote) {
            super(contexto, camada, interna, externa, semOrelhaDireita);
            this.zumbificado = semOrelhaDireita;
            if (ehFilhote) {
                this.model = new ModeloPiglinFilhote<>(contexto.bakeLayer(CamadasMonstrosFilhotes.PIGLIN));
                this.layers.clear();
                this.addLayer(new CamadaCabecaFilhote<>(this, contexto.getModelSet(), contexto.getItemInHandRenderer(), 1.0019531F, 1.0F, 1.0019531F));
                this.addLayer(new CamadaItemFilhote<>(this, contexto.getItemInHandRenderer()));
                this.addLayer(new CamadaArmaduraFilhote<>(this, contexto.getModelSet(), CamadasMonstrosFilhotes.ARMADURA_PIGLIN, 0.0F));
                this.filhote = null;
            } else {
                this.filhote = new Piglin(contexto, camada, interna, externa, semOrelhaDireita, true);
            }
        }

        @Override
        public void render(Mob entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (filhote != null && entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }

        @Override
        public ResourceLocation getTextureLocation(Mob entidade) {
            if (filhote == null) return zumbificado ? PIGLIN_ZUMBI : PIGLIN;
            return super.getTextureLocation(entidade);
        }
    }

    public static class Porcoespinho extends HoglinRenderer {

        private final RenderizadorFilhote<Hoglin> filhote;

        public Porcoespinho(EntityRendererProvider.Context contexto) {
            super(contexto);
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloHoglinFilhote<Hoglin>(contexto.bakeLayer(CamadasMonstrosFilhotes.HOGLIN)), 0.7F, h -> HOGLIN)
                    .comTremor(Hoglin::isConverting);
        }

        @Override
        public void render(Hoglin entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }

    public static class Zoglino extends ZoglinRenderer {

        private final RenderizadorFilhote<Zoglin> filhote;

        public Zoglino(EntityRendererProvider.Context contexto) {
            super(contexto);
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloHoglinFilhote<Zoglin>(contexto.bakeLayer(CamadasMonstrosFilhotes.HOGLIN)), 0.7F, z -> ZOGLIN);
        }

        @Override
        public void render(Zoglin entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }
}
