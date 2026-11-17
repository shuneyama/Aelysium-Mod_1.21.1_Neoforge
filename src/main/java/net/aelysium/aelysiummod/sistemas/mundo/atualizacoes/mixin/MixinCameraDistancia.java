package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.AtributosNovos;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Camera.class)
public abstract class MixinCameraDistancia {

    @ModifyArg(method = "setup", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;getDetachedCameraDistance(Lnet/minecraft/client/Camera;ZFF)F"), index = 3)
    private float aelysium$distancia(float padrao) {
        Entity entidade = ((Camera) (Object) this).getEntity();
        if (!(entidade instanceof LivingEntity vivo)) return padrao;
        double distancia = vivo.getAttributeValue(AtributosNovos.DISTANCIA_CAMERA);
        Entity veiculo = vivo.getVehicle();
        if (veiculo instanceof LivingEntity montaria) {
            distancia = Math.max(distancia, montaria.getAttributeValue(AtributosNovos.DISTANCIA_CAMERA));
        }
        return (float) distancia;
    }
}
