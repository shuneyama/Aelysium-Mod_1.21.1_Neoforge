package net.aelysium.aelysiummod.compatibilidade.jade;

import net.minecraft.server.level.ServerPlayer;
import snownee.jade.util.CommonProxy;

import java.util.Collection;

public class ControleOverlayJade {

    public static void hide(Collection<ServerPlayer> players) {
        CommonProxy.showOrHideFromServer(players, false);
    }

    public static void show(Collection<ServerPlayer> players) {
        CommonProxy.showOrHideFromServer(players, true);
    }
}
