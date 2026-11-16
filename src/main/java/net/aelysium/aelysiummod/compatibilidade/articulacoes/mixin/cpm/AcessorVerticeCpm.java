package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.cpm;

import com.tom.cpl.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.tom.cpm.shared.model.render.BoxRender$Vertex", remap = false)
public interface AcessorVerticeCpm {

    @Accessor("position")
    Vec3f cpmec$obterPosicao();

    @Accessor("textureU")
    float cpmec$obterTexturaU();

    @Accessor("textureV")
    float cpmec$obterTexturaV();
}
