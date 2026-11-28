package net.aelysium.aelysiummod.sistemas.chefes.base.dados;

public class BooleanFlag {
   private boolean flag = false;

   public void flag() {
      this.flag = true;
   }

   public boolean getAndReset() {
      boolean toReturn = this.flag;
      if (this.flag) {
         this.flag = false;
      }

      return toReturn;
   }
}
