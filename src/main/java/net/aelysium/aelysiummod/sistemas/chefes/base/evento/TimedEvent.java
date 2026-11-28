package net.aelysium.aelysiummod.sistemas.chefes.base.evento;

import java.util.function.Supplier;

public class TimedEvent implements IEvent {
   private final Runnable callback;
   private final Supplier<Boolean> shouldCancel;
   private final int delay;
   private final int duration;
   private final double velocidade;
   private double age;

   public TimedEvent(Runnable callback, int delay, int duration, Supplier<Boolean> shouldCancel) {
      this.callback = callback;
      this.delay = delay;
      this.duration = duration;
      this.shouldCancel = shouldCancel;
      this.velocidade = VelocidadeEventos.atual();
      this.age = 0;
   }

   public TimedEvent(Runnable callback, int delay, int duration) {
      this(callback, delay, duration, () -> false);
   }

   public TimedEvent(Runnable callback, int delay, Supplier<Boolean> shouldCancel) {
      this(callback, delay, 1, shouldCancel);
   }

   public TimedEvent(Runnable callback, int delay) {
      this(callback, delay, 1, () -> false);
   }

   @Override
   public boolean shouldDoEvent() {
      boolean pronto = this.age >= this.delay;
      this.age += this.velocidade;
      return pronto && !this.shouldCancel.get();
   }

   @Override
   public void doEvent() {
      if (this.velocidade == 1.0) {
         this.callback.run();
      } else {
         VelocidadeEventos.executar(this.velocidade, this.callback);
      }
   }

   @Override
   public boolean shouldRemoveEvent() {
      return this.shouldCancel.get() || this.age >= this.delay + this.duration;
   }

   @Override
   public int tickSize() {
      return 1;
   }
}
