package net.aelysium.aelysiummod.sistemas.chefes.base.evento;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventScheduler {
   private int ticks = 0;
   private final List<IEvent> eventQueue = new ArrayList<>();
   private final Set<IEvent> eventsToAdd = new HashSet<>();

   public void updateEvents() {
      this.eventQueue.addAll(this.eventsToAdd);
      this.eventsToAdd.clear();

      for (IEvent iEvent : this.eventQueue) {
         if (this.ticks % iEvent.tickSize() == 0 && iEvent.shouldDoEvent()) {
            iEvent.doEvent();
         }
      }

      this.eventQueue.removeIf(IEvent::shouldRemoveEvent);
      this.ticks++;
   }

   public void addEvent(IEvent event) {
      this.eventsToAdd.add(event);
   }
}
