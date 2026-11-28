package net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SoundUtils {
   public static void playSound(ServerLevel level, Vec3 pos, SoundEvent soundEvent, SoundSource soundSource, float volume, double range) {
      playSound(level, pos, soundEvent, soundSource, volume, range, null);
   }

   public static void playSound(ServerLevel level, Vec3 pos, SoundEvent soundEvent, SoundSource soundSource, float volume, double range, Player except) {
      playSound(level, pos, soundEvent, soundSource, volume, randomPitch(level.random), range, except);
   }

   public static void playSound(
      ServerLevel level, Vec3 pos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, double range, Player except
   ) {
      Holder<SoundEvent> holder = Holder.direct(SoundEvent.createVariableRangeEvent(soundEvent.getLocation()));
      level.getServer()
         .getPlayerList()
         .broadcast(
            except,
            pos.x,
            pos.y,
            pos.z,
            range,
            level.dimension(),
            new ClientboundSoundPacket(holder, soundSource, pos.x, pos.y, pos.z, volume, pitch, level.random.nextLong())
         );
   }

   public static float randomPitch(@NotNull RandomSource random) {
      return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
   }
}
