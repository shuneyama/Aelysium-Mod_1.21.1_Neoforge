package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.cpm;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.tom.cpm.client.PlayerRenderManager$RedirectModelRendererBase", remap = false)
public interface AcessorRedirecionadorBase {

    @Accessor("parent")
    ModelPart cpmec$obterPai();
}
