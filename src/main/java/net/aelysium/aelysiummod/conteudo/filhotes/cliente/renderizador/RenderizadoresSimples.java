package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.CamadasFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.UtilFilhotes;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloAbelhaFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloAxolotlFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloBurroFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloCameloFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloCavaloFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloCoelhoFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloLhamaFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloSnifferFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloStriderFilhote;
import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloTatuFilhote;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.ArmadilloRenderer;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.CamelRenderer;
import net.minecraft.client.renderer.entity.ChestedHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SnifferRenderer;
import net.minecraft.client.renderer.entity.StriderRenderer;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.monster.Strider;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class RenderizadoresSimples {

    private RenderizadoresSimples() {
    }

    public static class Coelho extends RabbitRenderer {

        private final RenderizadorFilhote<Rabbit> filhote;
        private final RenderizadorFilhote<Rabbit> adulto;

        public Coelho(EntityRendererProvider.Context contexto) {
            super(contexto);
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloCoelhoFilhote(contexto.bakeLayer(CamadasFilhotes.COELHO)), 0.3F, c -> textura(c, "_baby"));
            this.adulto = new RenderizadorFilhote<>(contexto, new net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.ModeloCoelhoAdulto(contexto.bakeLayer(CamadasFilhotes.COELHO_ADULTO)), 0.3F, c -> textura(c, ""));
        }

        private static ResourceLocation textura(Rabbit coelho, String sufixo) {
            if ("Toast".equals(ChatFormatting.stripFormatting(coelho.getName().getString()))) {
                return UtilFilhotes.textura("filhotes/rabbit/rabbit_toast" + sufixo);
            }
            String nome = switch (coelho.getVariant()) {
                case WHITE -> "white";
                case BLACK -> "black";
                case GOLD -> "gold";
                case SALT -> "salt";
                case WHITE_SPLOTCHED -> "white_splotched";
                case EVIL -> "caerbannog";
                default -> "brown";
            };
            return UtilFilhotes.textura("filhotes/rabbit/rabbit_" + nome + sufixo);
        }

        @Override
        public void render(Rabbit entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            adulto.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }

    public static class Abelha extends BeeRenderer {

        private final RenderizadorFilhote<Bee> filhote;

        public Abelha(EntityRendererProvider.Context contexto) {
            super(contexto);
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloAbelhaFilhote(contexto.bakeLayer(CamadasFilhotes.ABELHA)), 0.4F, Abelha::textura);
        }

        private static ResourceLocation textura(Bee abelha) {
            String nome = abelha.isAngry() ? (abelha.hasNectar() ? "bee_angry_nectar_baby" : "bee_angry_baby") : (abelha.hasNectar() ? "bee_nectar_baby" : "bee_baby");
            return UtilFilhotes.textura("filhotes/bee/" + nome);
        }

        @Override
        public void render(Bee entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }

    public static class Tatu extends ArmadilloRenderer {

        private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/armadillo/armadillo_baby");
        private final RenderizadorFilhote<Armadillo> filhote;

        public Tatu(EntityRendererProvider.Context contexto) {
            super(contexto);
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloTatuFilhote(contexto.bakeLayer(CamadasFilhotes.TATU)), 0.4F, tatu -> FILHOTE);
        }

        @Override
        public void render(Armadillo entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }

    public static class Camelo extends CamelRenderer {

        private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/camel/camel_baby");
        private final RenderizadorFilhote<Camel> filhote;

        public Camelo(EntityRendererProvider.Context contexto) {
            super(contexto, ModelLayers.CAMEL);
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloCameloFilhote(contexto.bakeLayer(CamadasFilhotes.CAMELO)), 0.7F, camelo -> FILHOTE);
        }

        @Override
        public void render(Camel entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }

    public static class Axolote extends AxolotlRenderer {

        private final RenderizadorFilhote<Axolotl> filhote;

        public Axolote(EntityRendererProvider.Context contexto) {
            super(contexto);
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloAxolotlFilhote(contexto.bakeLayer(CamadasFilhotes.AXOLOTE)), 0.5F,
                    axolote -> UtilFilhotes.textura("filhotes/axolotl/axolotl_" + axolote.getVariant().getName() + "_baby"));
        }

        @Override
        public void render(Axolotl entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }

    public static class Lhama extends LlamaRenderer {

        private static final ResourceLocation DECORACAO_MERCADOR = UtilFilhotes.textura("filhotes/equipment/llama_body/trader_llama_baby");
        private final RenderizadorFilhote<Llama> filhote;

        public Lhama(EntityRendererProvider.Context contexto, boolean mercador) {
            super(contexto, mercador ? ModelLayers.TRADER_LLAMA : ModelLayers.LLAMA);
            ModeloLhamaFilhote decoracao = new ModeloLhamaFilhote(contexto.bakeLayer(CamadasFilhotes.LHAMA_DECORACAO));
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloLhamaFilhote(contexto.bakeLayer(CamadasFilhotes.LHAMA)), 0.7F,
                    lhama -> UtilFilhotes.textura("filhotes/llama/llama_" + lhama.getVariant().name().toLowerCase(Locale.ROOT) + "_baby"))
                    .comCamada(pai -> new CamadaDecoracaoMercador(pai, decoracao));
        }

        @Override
        public void render(Llama entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }

        private static class CamadaDecoracaoMercador extends RenderLayer<Llama, EntityModel<Llama>> {

            private final ModeloLhamaFilhote modelo;

            CamadaDecoracaoMercador(RenderLayerParent<Llama, EntityModel<Llama>> pai, ModeloLhamaFilhote modelo) {
                super(pai);
                this.modelo = modelo;
            }

            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, Llama lhama, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
                if (!lhama.isTraderLlama()) return;
                this.getParentModel().copyPropertiesTo(modelo);
                modelo.setupAnim(lhama, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                VertexConsumer consumidor = buffer.getBuffer(RenderType.entityCutoutNoCull(DECORACAO_MERCADOR));
                modelo.renderToBuffer(poseStack, consumidor, luz, LivingEntityRenderer.getOverlayCoords(lhama, 0.0F), -1);
            }
        }
    }

    public static class Cavalo extends AbstractHorseRenderer<Horse, HorseModel<Horse>> {

        private static final Map<Variant, ResourceLocation> ADULTOS = new EnumMap<>(Variant.class);
        private static final Map<Variant, ResourceLocation> FILHOTES = new EnumMap<>(Variant.class);
        private static final Map<Markings, ResourceLocation> MARCAS = new EnumMap<>(Markings.class);

        static {
            registrar(Variant.WHITE, "white");
            registrar(Variant.CREAMY, "creamy");
            registrar(Variant.CHESTNUT, "chestnut");
            registrar(Variant.BROWN, "brown");
            registrar(Variant.BLACK, "black");
            registrar(Variant.GRAY, "gray");
            registrar(Variant.DARK_BROWN, "darkbrown");
            MARCAS.put(Markings.WHITE, UtilFilhotes.textura("filhotes/horse/horse_markings_white_baby"));
            MARCAS.put(Markings.WHITE_FIELD, UtilFilhotes.textura("filhotes/horse/horse_markings_whitefield_baby"));
            MARCAS.put(Markings.WHITE_DOTS, UtilFilhotes.textura("filhotes/horse/horse_markings_whitedots_baby"));
            MARCAS.put(Markings.BLACK_DOTS, UtilFilhotes.textura("filhotes/horse/horse_markings_blackdots_baby"));
        }

        private static void registrar(Variant variante, String nome) {
            ADULTOS.put(variante, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_" + nome + ".png"));
            FILHOTES.put(variante, UtilFilhotes.textura("filhotes/horse/horse_" + nome + "_baby"));
        }

        private final RenderizadorFilhote<Horse> filhote;

        public Cavalo(EntityRendererProvider.Context contexto) {
            super(contexto, new HorseModel<>(contexto.bakeLayer(ModelLayers.HORSE)), 1.1F);
            this.addLayer(new HorseMarkingLayer(this));
            this.addLayer(new HorseArmorLayer(this, contexto.getModelSet()));
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloCavaloFilhote<Horse>(contexto.bakeLayer(CamadasFilhotes.CAVALO)), 0.75F,
                    cavalo -> FILHOTES.get(cavalo.getVariant()))
                    .comCamada(CamadaMarcas::new);
        }

        @Override
        public ResourceLocation getTextureLocation(Horse entidade) {
            return ADULTOS.get(entidade.getVariant());
        }

        @Override
        public void render(Horse entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }

        private static class CamadaMarcas extends RenderLayer<Horse, EntityModel<Horse>> {

            CamadaMarcas(RenderLayerParent<Horse, EntityModel<Horse>> pai) {
                super(pai);
            }

            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int luz, Horse cavalo, float limbSwing, float limbSwingAmount, float parcial, float ageInTicks, float netHeadYaw, float headPitch) {
                ResourceLocation textura = MARCAS.get(cavalo.getMarkings());
                if (textura == null || cavalo.isInvisible()) return;
                VertexConsumer consumidor = buffer.getBuffer(RenderType.entityTranslucent(textura));
                this.getParentModel().renderToBuffer(poseStack, consumidor, luz, LivingEntityRenderer.getOverlayCoords(cavalo, 0.0F), -1);
            }
        }
    }

    public static class Burro<T extends AbstractChestedHorse> extends ChestedHorseRenderer<T> {

        private final RenderizadorFilhote<T> filhote;

        public Burro(EntityRendererProvider.Context contexto, boolean mula) {
            super(contexto, mula ? 0.92F : 0.87F, mula ? ModelLayers.MULE : ModelLayers.DONKEY);
            ResourceLocation textura = UtilFilhotes.textura(mula ? "filhotes/horse/mule_baby" : "filhotes/horse/donkey_baby");
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloBurroFilhote<T>(contexto.bakeLayer(CamadasFilhotes.BURRO)), 0.75F, burro -> textura);
        }

        @Override
        public void render(T entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }

    public static class StriderAelysium extends StriderRenderer {

        private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/strider/strider_baby");
        private static final ResourceLocation FILHOTE_FRIO = UtilFilhotes.textura("filhotes/strider/strider_cold_baby");
        private final RenderizadorFilhote<Strider> filhote;

        public StriderAelysium(EntityRendererProvider.Context contexto) {
            super(contexto);
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloStriderFilhote(contexto.bakeLayer(CamadasFilhotes.STRIDER)), 0.25F,
                    strider -> strider.isSuffocating() ? FILHOTE_FRIO : FILHOTE)
                    .comTremor(Strider::isSuffocating);
        }

        @Override
        public void render(Strider entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }

    public static class SnifferAelysium extends SnifferRenderer {

        private static final ResourceLocation FILHOTE = UtilFilhotes.textura("filhotes/sniffer/snifflet");
        private final RenderizadorFilhote<Sniffer> filhote;

        public SnifferAelysium(EntityRendererProvider.Context contexto) {
            super(contexto);
            this.filhote = new RenderizadorFilhote<>(contexto, new ModeloSnifferFilhote(contexto.bakeLayer(CamadasFilhotes.SNIFFER)), 0.55F, sniffer -> FILHOTE);
        }

        @Override
        public void render(Sniffer entidade, float yaw, float parcial, PoseStack poseStack, MultiBufferSource buffer, int luz) {
            if (entidade.isBaby()) {
                filhote.render(entidade, yaw, parcial, poseStack, buffer, luz);
                return;
            }
            super.render(entidade, yaw, parcial, poseStack, buffer, luz);
        }
    }

    public static void registrar(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.RABBIT, Coelho::new);
        event.registerEntityRenderer(EntityType.BEE, Abelha::new);
        event.registerEntityRenderer(EntityType.ARMADILLO, Tatu::new);
        event.registerEntityRenderer(EntityType.CAMEL, Camelo::new);
        event.registerEntityRenderer(EntityType.AXOLOTL, Axolote::new);
        event.registerEntityRenderer(EntityType.LLAMA, contexto -> new Lhama(contexto, false));
        event.registerEntityRenderer(EntityType.TRADER_LLAMA, contexto -> new Lhama(contexto, true));
        event.registerEntityRenderer(EntityType.HORSE, Cavalo::new);
        event.registerEntityRenderer(EntityType.DONKEY, contexto -> new Burro<>(contexto, false));
        event.registerEntityRenderer(EntityType.MULE, contexto -> new Burro<>(contexto, true));
        event.registerEntityRenderer(EntityType.STRIDER, StriderAelysium::new);
        event.registerEntityRenderer(EntityType.SNIFFER, SnifferAelysium::new);
    }
}
