package net.aelysium.aelysiummod.sistemas.chefes.base.dados;

import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class WeakHashPredicate<T> implements Predicate<T> {
   private final Supplier<Supplier<Boolean>> predicateFactory;
   private final WeakHashMap<T, Supplier<Boolean>> conditionals = new WeakHashMap<>();

   public WeakHashPredicate(Supplier<Supplier<Boolean>> predicateFactory) {
      this.predicateFactory = predicateFactory;
   }

   @Override
   public boolean test(T t) {
      if (!this.conditionals.containsKey(t)) {
         this.conditionals.put(t, this.predicateFactory.get());
      }

      return this.conditionals.get(t).get();
   }
}
