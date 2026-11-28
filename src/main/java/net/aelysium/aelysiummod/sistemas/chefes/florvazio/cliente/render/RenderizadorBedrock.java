package net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.DesenhoModelo;
import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.ModeloBedrock;
import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.OssoModelo;
import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.PoseAnimada;
import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.RecursosModelo;
import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao.AnimacaoBedrock;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.comum.AnimationHolder;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.animacao.ICodeAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class RenderizadorBedrock<T extends Entity> extends EntityRenderer<T> {
    private final ResourceLocation modelo;
    private final ResourceLocation textura;
    private final ResourceLocation animacoes;
    private final Function<T, AnimationHolder> controlador;
    private final ICodeAnimations<T> animacoesCodigo;
    private final IRenderLight<T> brilho;
    private final IBoneLight luzOssos;
    private final IRenderer<T> renderer;
    private final IOverlayOverride sobreposicao;
    private final boolean rotacaoMorte;
    private final Map<T, PoseAnimada> poses = new WeakHashMap<>();
    private final DesenhoModelo.AjusteOsso ajuste;
    private final Vector4f corOsso = new Vector4f();

    public RenderizadorBedrock(EntityRendererProvider.Context contexto, ResourceLocation modelo, ResourceLocation textura, ResourceLocation animacoes, Function<T, AnimationHolder> controlador, ICodeAnimations<T> animacoesCodigo, IRenderLight<T> brilho, IBoneLight luzOssos, IRenderer<T> renderer, IOverlayOverride sobreposicao, boolean rotacaoMorte) {
        super(contexto);
        this.modelo = modelo;
        this.textura = textura;
        this.animacoes = animacoes;
        this.controlador = controlador;
        this.animacoesCodigo = animacoesCodigo;
        this.brilho = brilho;
        this.luzOssos = luzOssos;
        this.renderer = renderer;
        this.sobreposicao = sobreposicao;
        this.rotacaoMorte = rotacaoMorte;
        this.ajuste = luzOssos == null ? null : new DesenhoModelo.AjusteOsso() {
            @Override
            public int luz(OssoModelo osso, int luz) {
                return luzOssos.getLightForBone(osso, luz);
            }

            @Override
            public int cor(OssoModelo osso, int cor) {
                corOsso.set(FastColor.ARGB32.red(cor), FastColor.ARGB32.green(cor), FastColor.ARGB32.blue(cor), FastColor.ARGB32.alpha(cor)).mul(1 / 255f);
                Vector4f novo = luzOssos.getColorForBone(osso, corOsso).mul(255f);
                return FastColor.ARGB32.color((int) novo.w, (int) novo.x, (int) novo.y, (int) novo.z);
            }
        };
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entidade) {
        return textura;
    }

    @Override
    protected int getBlockLightLevel(@NotNull T entidade, @NotNull BlockPos posicao) {
        return brilho != null ? brilho.getBlockLight(entidade, posicao) : super.getBlockLightLevel(entidade, posicao);
    }

    @Override
    public void render(@NotNull T entidade, float yaw, float partialTick, @NotNull PoseStack pilha, @NotNull MultiBufferSource buffer, int luz) {
        if (renderer != null) renderer.render(entidade, yaw, partialTick, pilha, buffer, luz);
        pilha.pushPose();
        ModeloBedrock modeloBedrock = RecursosModelo.modelo(modelo);
        if (modeloBedrock != null) desenharModelo(entidade, modeloBedrock, partialTick, pilha, buffer, luz);
        super.render(entidade, 0, partialTick, pilha, buffer, luz);
        pilha.popPose();
    }

    private void desenharModelo(T entidade, ModeloBedrock modeloBedrock, float partialTick, PoseStack pilha, MultiBufferSource buffer, int luz) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean invisivel = entidade.isInvisible();
        boolean translucido = invisivel && !entidade.isInvisibleTo(minecraft.player);
        int cor = translucido ? FastColor.ARGB32.color(Mth.ceil(255 * 38 / 255f), 255, 255, 255) : 0xFFFFFFFF;
        LivingEntity vivo = entidade instanceof LivingEntity l ? l : null;
        int camada = sobreposicao != null ? sobreposicao.getOverlay()
                : vivo != null ? OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(vivo.hurtTime > 0 || vivo.deathTime > 0)) : OverlayTexture.NO_OVERLAY;
        RenderType tipo = translucido ? RenderType.itemEntityTranslucentCull(textura)
                : !invisivel ? RenderType.entityCutout(textura)
                : minecraft.shouldEntityAppearGlowing(entidade) ? RenderType.outline(textura) : null;

        pilha.pushPose();
        float corpo = vivo == null ? 0 : Mth.rotLerp(partialTick, vivo.yBodyRotO, vivo.yBodyRot);
        float escala = vivo != null ? vivo.getScale() : 1;
        pilha.scale(escala, escala, escala);
        aplicarRotacoes(entidade, vivo, pilha, corpo, partialTick, escala);
        animar(entidade, modeloBedrock, partialTick);
        pilha.translate(0, 0.01f, 0);
        if (tipo != null) DesenhoModelo.desenhar(pilha, modeloBedrock, buffer.getBuffer(tipo), luz, camada, cor, ajuste);
        pilha.popPose();
    }

    private void aplicarRotacoes(T entidade, LivingEntity vivo, PoseStack pilha, float corpo, float partialTick, float escala) {
        if (entidade.isFullyFrozen()) corpo += (float) (Math.cos(entidade.tickCount * 3.25d) * Math.PI * 0.4d);

        pilha.mulPose(Axis.YP.rotationDegrees(180f - corpo));

        if (vivo == null) return;

        if (vivo.deathTime > 0) {
            float morte = (vivo.deathTime + partialTick - 1f) / 20f * 1.6f;
            pilha.mulPose(Axis.ZP.rotationDegrees(Math.min(Mth.sqrt(morte), 1) * (rotacaoMorte ? 90f : 0f)));
        } else if (vivo.isAutoSpinAttack()) {
            pilha.mulPose(Axis.XP.rotationDegrees(-90f - vivo.getXRot()));
            pilha.mulPose(Axis.YP.rotationDegrees((vivo.tickCount + partialTick) * -75f));
        } else if (LivingEntityRenderer.isEntityUpsideDown(vivo)) {
            pilha.translate(0, (entidade.getBbHeight() + 0.1f) / escala, 0);
            pilha.mulPose(Axis.ZP.rotationDegrees(180f));
        }
    }

    private void animar(T entidade, ModeloBedrock modeloBedrock, float partialTick) {
        double agora = entidade.tickCount + partialTick;
        AnimacaoBedrock animacao = null;
        double tick = 0;

        AnimationHolder estado = controlador != null ? controlador.apply(entidade) : null;
        if (estado != null && animacoes != null) {
            estado.atualizar(agora);
            Map<String, AnimacaoBedrock> todas = RecursosModelo.animacoes(animacoes);
            AnimacaoBedrock unica = estado.animacaoUnica() != null ? todas.get(estado.animacaoUnica()) : null;
            AnimacaoBedrock repetida = estado.animacaoRepetida() != null ? todas.get(estado.animacaoRepetida()) : null;
            double decorrido = Math.max(agora - estado.inicio(), 0);
            double velocidade = estado.velocidade();
            double decorridoUnica = decorrido * velocidade;

            if (unica != null && decorridoUnica < unica.duracao()) {
                animacao = unica;
                tick = decorridoUnica;
            } else if (repetida != null) {
                double ciclo = unica != null ? decorrido - unica.duracao() / velocidade : decorrido;
                animacao = repetida;
                tick = repetida.duracao() > 0 && repetida.duracao() != Double.MAX_VALUE ? ciclo % repetida.duracao() : ciclo;
            }
        }

        poses.computeIfAbsent(entidade, e -> new PoseAnimada()).aplicar(modeloBedrock, animacao, tick, agora);
        if (animacoesCodigo != null) animacoesCodigo.animate(entidade, modeloBedrock, partialTick);
    }
}
