package net.aelysium.aelysiummod.sistemas.chefes.base.evento;

import java.util.Iterator;
import java.util.List;

public class EventSeries implements IEvent {
   private final Iterator<IEvent> iterator;
   private IEvent currentEvent;

   public EventSeries(IEvent... events) {
      if (events.length < 1) {
         throw new IllegalArgumentException("Must have at least one event");
      } else {
         this.iterator = List.of(events).iterator();
         this.currentEvent = this.iterator.next();
      }
   }

   @Override
   public boolean shouldDoEvent() {
      return this.currentEvent.shouldDoEvent();
   }

   @Override
   public void doEvent() {
      this.currentEvent.doEvent();
   }

   @Override
   public boolean shouldRemoveEvent() {
      while (this.currentEvent.shouldRemoveEvent()) {
         if (!this.iterator.hasNext()) {
            return true;
         }

         this.currentEvent = this.iterator.next();
      }

      return false;
   }

   @Override
   public int tickSize() {
      return this.currentEvent.tickSize();
   }
}
