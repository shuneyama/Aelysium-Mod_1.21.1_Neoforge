package io.github.kosmx.bendylib.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.MutableCuboid;
import io.github.kosmx.bendylib.impl.accessors.CuboidSideAccessor;
import io.github.kosmx.bendylib.impl.accessors.IModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public abstract class IModelPartMixin implements IModelPartAccessor {

    @Shadow @Final private Map<String, ModelPart> children;

    @Mutable
    @Shadow @Final private List<ModelPart.Cube> cubes;

    @Unique
    private boolean hasMutatedCuboid = false;

    private ModelPartAccessor.Workaround workaround = ModelPartAccessor.Workaround.VanillaDraw;

    @Shadow
    private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ModelPart.Cube> getCubes() {
        hasMutatedCuboid = true;
        return cubes;
    }

    @Override
    public void setCubes(List<ModelPart.Cube> cubes) {
        this.cubes = cubes;
    }

    @Override
    public Map<String, ModelPart> getChildren() {
        return children;
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void copyTransformExtended(ModelPart part, CallbackInfo ci){
        if(((IModelPartAccessor)part).getCubes() == null || cubes == null) return;
        Iterator<ModelPart.Cube> iterator0 = ((IModelPartAccessor)part).getCubes().iterator();
        Iterator<ModelPart.Cube> iterator1 = cubes.iterator();

        while (iterator0.hasNext() && iterator1.hasNext()){
            MutableCuboid cuboid1 = (MutableCuboid) iterator1.next();
            MutableCuboid cuboid0 = (MutableCuboid) iterator0.next();
            cuboid1.copyStateFrom(cuboid0);
        }

    }

    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"), require = 0)
    private void redirectCompile(ModelPart modelPart, PoseStack.Pose entry, VertexConsumer vertexConsumer, int light, int overlay, int color){
        redirectedFunction(modelPart, entry, vertexConsumer, light, overlay, color);
    }

    @Unique
    private void redirectedFunction(ModelPart modelPart, PoseStack.Pose entry, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        if(workaround == ModelPartAccessor.Workaround.ExportQuads){
            for(ModelPart.Cube cube:cubes){
                ((CuboidSideAccessor)cube).doSideSwapping();
            }
            compile(entry, vertexConsumer, light, overlay, color);
            for(ModelPart.Cube cube:cubes){
                ((CuboidSideAccessor)cube).resetSides();
            }
        }
        else if(workaround == ModelPartAccessor.Workaround.VanillaDraw){
            if(!hasMutatedCuboid || cubes.size() == 1 && ((MutableCuboid)cubes.get(0)).getActiveMutator() == null){
                compile(entry, vertexConsumer, light, overlay, color);
            }
            else {
                for(ModelPart.Cube cube:cubes){
                    cube.compile(entry, vertexConsumer, light, overlay, color);
                }
            }
        }
        else {
            compile(entry, vertexConsumer, light, overlay, color);
        }
    }

    @Override
    public void setWorkaround(ModelPartAccessor.Workaround workaround) {
        this.workaround = workaround;
    }
}
