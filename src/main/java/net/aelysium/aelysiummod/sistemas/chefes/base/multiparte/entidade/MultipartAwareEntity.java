package net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface MultipartAwareEntity extends MultipartEntity {
   EntityBounds getBounds();

   void onSetPos(double var1, double var3, double var5);

   void setNextDamagedPart(@Nullable String var1);

   default InteractionResult interact(Entity entity, InteractionHand hand, String part) {
      return InteractionResult.PASS;
   }
}
