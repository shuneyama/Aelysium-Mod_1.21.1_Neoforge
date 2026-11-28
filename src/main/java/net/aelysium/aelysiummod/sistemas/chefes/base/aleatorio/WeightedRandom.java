package net.aelysium.aelysiummod.sistemas.chefes.base.aleatorio;

import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

public class WeightedRandom<E> {
   private final Random random;
   private final NavigableMap<Double, E> map;
   private double total;

   public WeightedRandom() {
      this(new Random());
   }

   public WeightedRandom(Random random) {
      this.random = random;
      this.map = new TreeMap<>();
      this.total = 0.0;
   }

   public WeightedRandom<E> add(double weight, E result) {
      if (weight <= 0.0) {
         return this;
      } else if (!Double.isNaN(weight) && !Double.isInfinite(weight)) {
         this.total += weight;
         this.map.put(Double.valueOf(this.total), result);
         return this;
      } else {
         throw new IllegalArgumentException("The weight for random collection is invalid: " + weight);
      }
   }

   public void addAll(Collection<Pair<Double, E>> collection) {
      for (Pair<Double, E> pair : collection) {
         this.add((Double)pair.getFirst(), (E)pair.getSecond());
      }
   }

   public E next() {
      double value = this.random.nextDouble() * this.total;
      Entry<Double, E> entry = this.map.higherEntry(value);
      return entry != null ? entry.getValue() : null;
   }
}
