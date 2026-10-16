package net.aelysium.aelysiummod.sistemas.social.maozinha.mixin;

import net.aelysium.aelysiummod.sistemas.social.maozinha.cliente.AnimacaoBracoMaozinha;
import net.aelysium.aelysiummod.sistemas.social.maozinha.cliente.CacheMaozinha;
import net.aelysium.aelysiummod.sistemas.social.maozinha.cliente.GeometriaMaozinha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerModel.class)
public class ModeloJogadorMixin {

    private static final double ALTURA_PEITO_ALVO = 1.0;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void aelysium_poseMaozinha(LivingEntity entidade, float limbSwing, float limbSwingAmount,
                                       float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entidade instanceof AbstractClientPlayer jogador)) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        PlayerModel<?> modelo = (PlayerModel<?>) (Object) this;
        float parcial = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        UUID id = jogador.getUUID();

        Vec3 destino = null;
        boolean esquerdo = true;

        UUID idParceiro = CacheMaozinha.parceiro(id);
        Player parceiro = idParceiro != null ? minecraft.level.getPlayerByUUID(idParceiro) : null;
        if (parceiro != null) {
            boolean souLider = id.equals(CacheMaozinha.liderDe(idParceiro));
            Player lider = souLider ? jogador : parceiro;
            Player seguidor = souLider ? parceiro : jogador;
            destino = GeometriaMaozinha.encontroDasMaos(lider, seguidor, parcial);
            esquerdo = souLider;
        } else {
            UUID idAlvo = CacheMaozinha.alvoDaOferta(id);
            Player alvo = idAlvo != null ? minecraft.level.getPlayerByUUID(idAlvo) : null;
            if (alvo != null) {
                Vec3 peito = GeometriaMaozinha.posicao(alvo, parcial).add(0.0, ALTURA_PEITO_ALVO, 0.0);
                if (GeometriaMaozinha.estaNaFrente(jogador, peito, parcial)) destino = peito;
            }
        }

        AnimacaoBracoMaozinha.aplicar(modelo, jogador, parcial, ageInTicks, destino, esquerdo);
    }
}
