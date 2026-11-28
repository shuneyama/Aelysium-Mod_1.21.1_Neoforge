package net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util.OrientedBox;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util.QuaternionD;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public final class EntityPart {
   private boolean changed = true;
   private double offX;
   private double offY;
   private double offZ;
   private double x;
   private double y;
   private double z;
   private final AABB box;
   private double px;
   private double py;
   private double pz;
   private QuaternionD rotation;
   @Nullable
   private EntityPart parent;

   EntityPart(@Nullable EntityPart parent, AABB box, boolean center, double offX, double offY, double offZ) {
      this.parent = parent;
      this.offX = offX;
      this.offY = offY;
      this.offZ = offZ;
      this.rotation = QuaternionD.IDENTITY;
      if (center) {
         box = box.move(-box.minX - box.getXsize() / 2.0, -box.minY - box.getXsize() / 2.0, -box.minZ - box.getXsize() / 2.0);
      }

      this.box = box;
      this.setX(0.0);
      this.setY(0.0);
      this.setZ(0.0);
   }

   void setParent(@Nullable EntityPart parent) {
      this.parent = parent;
   }

   public void setOffX(double offX) {
      this.offX = offX;
      this.changed = true;
   }

   public void setOffY(double offY) {
      this.offY = offY;
      this.changed = true;
   }

   public void setOffZ(double offZ) {
      this.offZ = offZ;
      this.changed = true;
   }

   public void setX(double x) {
      this.x = x + this.offX;
      this.changed = true;
   }

   public void setY(double y) {
      this.y = y + this.offY;
      this.changed = true;
   }

   public void setZ(double z) {
      this.z = z + this.offZ;
      this.changed = true;
   }

   public void setPivotX(double px) {
      this.px = px;
      this.changed = true;
   }

   public void setPivotY(double py) {
      this.py = py;
      this.changed = true;
   }

   public void setPivotZ(double pz) {
      this.pz = pz;
      this.changed = true;
   }

   public void setRotation(QuaternionD rotation) {
      this.rotation = rotation;
      this.changed = true;
   }

   public void rotate(QuaternionD quaternion) {
      this.rotation = this.rotation.hamiltonProduct(quaternion);
      this.changed = true;
   }

   public void rotate(double pitch, double yaw, double roll, boolean degrees) {
      this.rotation = this.rotation.hamiltonProduct(new QuaternionD(pitch, yaw, roll, degrees));
      this.changed = true;
   }

   public void setRotation(double pitch, double yaw, double roll, boolean degrees) {
      this.rotation = new QuaternionD(pitch, yaw, roll, degrees);
      this.changed = true;
   }

   void setChanged(boolean changed) {
      this.changed = changed;
   }

   boolean isChanged() {
      return this.changed;
   }

   public OrientedBox getBox() {
      OrientedBox orientedBox = new OrientedBox(this.box);
      return this.transformChild(orientedBox);
   }

   private OrientedBox transformChild(OrientedBox orientedBox) {
      if (this.parent != null) {
         orientedBox = this.parent.transformChild(orientedBox);
      }

      return orientedBox.transform(this.x, this.y, this.z, this.px, this.py, this.pz, this.rotation);
   }
}
