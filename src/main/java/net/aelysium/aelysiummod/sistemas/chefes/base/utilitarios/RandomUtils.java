package net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios;

import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.world.phys.Vec3;

public class RandomUtils {
   private static final Random rand = new Random();

   public static double randDouble() {
      return rand.nextDouble();
   }

   public static double randDouble(double range) {
      return (rand.nextDouble() - 0.5) * 2.0 * range;
   }

   public static int range(int min, int max) {
      if (min > max) {
         throw new IllegalArgumentException("Minimum is greater than maximum");
      } else {
         int range = max - min;
         return min + rand.nextInt(range);
      }
   }

   public static double range(double min, double max) {
      if (min > max) {
         throw new IllegalArgumentException("Minimum is greater than maximum");
      } else {
         double range = max - min;
         return min + rand.nextDouble() * range;
      }
   }

   public static Vec3 randVec(Supplier<Double> rand) {
      return new Vec3(rand.get() - 0.5, rand.get() - 0.5, rand.get() - 0.5);
   }

   public static Vec3 randVec() {
      return randVec(rand::nextDouble);
   }

   public static int randSign() {
      return rand.nextInt(2) == 0 ? 1 : -1;
   }
}
