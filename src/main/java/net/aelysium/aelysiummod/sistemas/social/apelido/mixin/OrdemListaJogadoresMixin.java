package net.aelysium.aelysiummod.sistemas.social.apelido.mixin;

import net.aelysium.aelysiummod.sistemas.social.apelido.cliente.OrdemListaJogadores;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.Comparator;

@Mixin(PlayerTabOverlay.class)
public abstract class OrdemListaJogadoresMixin {

    @Redirect(
            method = "getPlayerInfos",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;getListedOnlinePlayers()Ljava/util/Collection;")
    )
    private Collection<PlayerInfo> aelysium$semEspectadores(ClientPacketListener conexao) {
        net.minecraft.world.entity.player.Player local = net.minecraft.client.Minecraft.getInstance().player;
        if (local != null && net.aelysium.aelysiummod.compatibilidade.jade.GerenciadorJogadoresOp.isOp(local.getUUID())) {
            return conexao.getListedOnlinePlayers();
        }
        return conexao.getListedOnlinePlayers().stream()
                .filter(info -> info.getGameMode() != GameType.SPECTATOR)
                .toList();
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I")
    )
    private int aelysium$nomeSemSombraNaInsignia(GuiGraphics graficos, Font fonte, Component nome, int x, int y, int cor) {
        return OrdemListaJogadores.desenharNome(graficos, fonte, nome, x, y, cor);
    }

    @ModifyArg(
            method = "getPlayerInfos",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;sorted(Ljava/util/Comparator;)Ljava/util/stream/Stream;"),
            index = 0
    )
    private Comparator<PlayerInfo> aelysium$ordem(Comparator<PlayerInfo> original) {
        return OrdemListaJogadores.COMPARADOR;
    }
}
