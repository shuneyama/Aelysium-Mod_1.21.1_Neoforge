package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ProxyFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.som.IceBreathSound;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ProxyClienteFrostmaw extends ProxyFrostmaw {

    @Override
    public void playIceBreathSound(Entity entidade) {
        Minecraft.getInstance().getSoundManager().play(new IceBreathSound(entidade));
    }

    @Override
    public Player getLocalPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
