package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.function.Function;
import java.util.function.Predicate;

public class RenderizadorFilhote<T extends Mob> extends MobRenderer<T, EntityModel<T>> {

    private final Function<T, ResourceLocation> textura;
    private Predicate<T> tremer = entidade -> false;

    public RenderizadorFilhote(EntityRendererProvider.Context contexto, EntityModel<T> modelo, float sombra, Function<T, ResourceLocation> textura) {
        super(contexto, modelo, sombra);
        this.textura = textura;
    }

    public RenderizadorFilhote<T> comTremor(Predicate<T> tremer) {
        this.tremer = tremer;
        return this;
    }

    public RenderizadorFilhote<T> comCamada(Function<RenderizadorFilhote<T>, RenderLayer<T, EntityModel<T>>> fabrica) {
        this.addLayer(fabrica.apply(this));
        return this;
    }

    @Override
    public ResourceLocation getTextureLocation(T entidade) {
        return textura.apply(entidade);
    }

    @Override
    protected boolean isShaking(T entidade) {
        return super.isShaking(entidade) || tremer.test(entidade);
    }
}
