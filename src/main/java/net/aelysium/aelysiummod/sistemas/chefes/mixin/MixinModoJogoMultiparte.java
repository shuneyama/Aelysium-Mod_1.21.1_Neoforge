package net.aelysium.aelysiummod.sistemas.chefes.mixin;

import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.MultipartAwareEntity;
import net.aelysium.aelysiummod.sistemas.chefes.rede.PacoteAtaqueParte;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinModoJogoMultiparte {

    @Shadow
    private GameType localPlayerMode;

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    private static String aelysium$parteMirada(MultipartAwareEntity multiparte) {
        Minecraft cliente = Minecraft.getInstance();
        float parcial = cliente.getTimer().getGameTimeDeltaTicks();
        Vec3 origem = cliente.cameraEntity.getEyePosition(parcial);
        Vec3 direcao = cliente.cameraEntity.getViewVector(parcial);
        double alcance = cliente.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        return multiparte.getBounds().raycast(origem, origem.add(direcao.scale(alcance)));
    }

    @Inject(method = "attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void aelysium$atacar(Player jogador, Entity alvo, CallbackInfo ci) {
        if (!(alvo instanceof MultipartAwareEntity multiparte)) return;
        ensureHasSentCarriedItem();
        String parte = aelysium$parteMirada(multiparte);
        if (parte == null) return;
        PacketDistributor.sendToServer(new PacoteAtaqueParte(alvo.getId(), parte, InteractionHand.MAIN_HAND, Minecraft.getInstance().cameraEntity.isShiftKeyDown(), true));
        if (localPlayerMode == GameType.SPECTATOR) return;
        multiparte.setNextDamagedPart(parte);
        jogador.attack(alvo);
        jogador.resetAttackStrengthTicker();
        ci.cancel();
    }

    @Inject(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true)
    private void aelysium$interagir(Player jogador, Entity entidade, InteractionHand mao, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(entidade instanceof MultipartAwareEntity multiparte)) return;
        ensureHasSentCarriedItem();
        String parte = aelysium$parteMirada(multiparte);
        if (parte == null) return;
        PacketDistributor.sendToServer(new PacoteAtaqueParte(entidade.getId(), parte, mao, Minecraft.getInstance().cameraEntity.isShiftKeyDown(), false));
        if (localPlayerMode != GameType.SPECTATOR) multiparte.interact(jogador, mao, parte);
        cir.setReturnValue(InteractionResult.PASS);
    }
}
