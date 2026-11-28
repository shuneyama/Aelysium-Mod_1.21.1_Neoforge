package net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render;

import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface ITextureProvider<T> {
    ResourceLocation getTexture(T entity);
}
