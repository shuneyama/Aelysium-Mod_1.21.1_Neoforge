package net.aelysium.aelysiummod.sistemas.chefes.base.evento;

public interface IEvent {
   boolean shouldDoEvent();

   void doEvent();

   boolean shouldRemoveEvent();

   int tickSize();
}
