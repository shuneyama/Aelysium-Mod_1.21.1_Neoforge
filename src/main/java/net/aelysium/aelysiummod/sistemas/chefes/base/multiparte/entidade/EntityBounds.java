package net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util.CompoundOrientedBox;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util.OrientedBox;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class EntityBounds {
   private CompoundOrientedBox cache;
   private final Map<String, EntityPart> partMap;
   @Nullable
   private final MutableBox overrideBox;

   EntityBounds(Map<String, EntityPart> partMap, @Nullable AABB overrideBox) {
      this.partMap = partMap;
      this.overrideBox = new MutableBox(overrideBox);
   }

   @Nullable
   public MutableBox getOverrideBox() {
      return this.overrideBox;
   }

   public boolean hasPart(String name) {
      return this.partMap.get(name) != null;
   }

   public EntityPart getPart(String name) {
      return this.partMap.get(name);
   }

   @Nullable
   public String raycast(Vec3 start, Vec3 end) {
      double t = 1.00001;
      String result = null;

      for (Entry<String, EntityPart> entry : this.partMap.entrySet()) {
         double tmp = entry.getValue().getBox().raycast(start, end);
         if (tmp != -1.0 && tmp < t) {
            t = tmp;
            result = entry.getKey();
         }
      }

      return result;
   }

   public CompoundOrientedBox getBox(AABB bounds) {
      boolean changed = this.cache == null;

      for (EntityPart value : this.partMap.values()) {
         if (value.isChanged()) {
            changed = true;
            value.setChanged(false);
         }
      }

      if (changed) {
         List<OrientedBox> parts = new ObjectArrayList(this.partMap.size());

         for (EntityPart valuex : this.partMap.values()) {
            parts.add(valuex.getBox());
         }

         this.cache = new CompoundOrientedBox(bounds, parts, this.overrideBox);
      }

      return this.cache.withBounds(bounds);
   }

   public static EntityBounds.EntityBoundsBuilder builder() {
      return new EntityBounds.EntityBoundsBuilder();
   }

   public static final class EntityBoundsBuilder {
      private final Map<String, EntityBounds.EntityPartInfo> partInfos = new Object2ObjectLinkedOpenHashMap();
      private AABB overrideBox = null;

      EntityBounds.EntityBoundsBuilder addInfo(EntityBounds.EntityPartInfo info) {
         if (info.parent != null && !this.partInfos.containsKey(info.parent)) {
            throw new RuntimeException("Unknown part: " + info.parent + ", did you register a child before a parent");
         } else {
            this.partInfos.put(info.name, info);
            return this;
         }
      }

      public EntityBounds.EntityBoundsBuilder overrideCollisionBox(AABB box) {
         this.overrideBox = box;
         return this;
      }

      public EntityBounds.EntityPartInfoBuilder add(String name) {
         if (this.partInfos.containsKey(name)) {
            throw new RuntimeException("Duplicate part: " + name);
         } else {
            return new EntityBounds.EntityPartInfoBuilder(this, name);
         }
      }

      public EntityBounds.Factory getFactory() {
         Map<String, EntityBounds.EntityPartInfo> copy = new Object2ObjectLinkedOpenHashMap(this.partInfos);
         return () -> {
            Map<String, EntityPart> partMap = new Object2ObjectOpenHashMap();

            for (Entry<String, EntityBounds.EntityPartInfo> entry : copy.entrySet()) {
               EntityBounds.EntityPartInfo info = entry.getValue();
               EntityPart entityPart = new EntityPart(info.parent != null ? partMap.get(info.parent) : null, info.bounds, false, info.x, info.y, info.z);
               entityPart.setPivotX(info.px);
               entityPart.setPivotY(info.py);
               entityPart.setPivotZ(info.pz);
               partMap.put(entry.getKey(), entityPart);
            }

            return new EntityBounds(partMap, this.overrideBox);
         };
      }
   }

   private static record EntityPartInfo(@Nullable String parent, String name, double x, double y, double z, double px, double py, double pz, AABB bounds) {
   }

   public static final class EntityPartInfoBuilder {
      final EntityBounds.EntityBoundsBuilder builder;
      @Nullable
      String parent;
      final String name;
      double x;
      double y;
      double z;
      double px;
      double py;
      double pz;
      AABB bounds;

      EntityPartInfoBuilder(EntityBounds.EntityBoundsBuilder builder, String name) {
         this.builder = builder;
         this.name = name;
      }

      public EntityBounds.EntityPartInfoBuilder setOffset(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
         return this;
      }

      public EntityBounds.EntityPartInfoBuilder setPivot(double x, double y, double z) {
         this.px = x;
         this.py = y;
         this.pz = z;
         return this;
      }

      public EntityBounds.EntityPartInfoBuilder setParent(@Nullable String parent) {
         this.parent = parent;
         return this;
      }

      public EntityBounds.EntityPartInfoBuilder setBounds(AABB bounds) {
         this.bounds = bounds;
         return this;
      }

      public EntityBounds.EntityPartInfoBuilder setBounds(double xLength, double yLength, double zLength) {
         this.bounds = new AABB(-xLength / 2.0, -yLength / 2.0, -zLength / 2.0, xLength / 2.0, yLength / 2.0, zLength / 2.0);
         return this;
      }

      public EntityBounds.EntityBoundsBuilder build() {
         return this.builder.addInfo(new EntityBounds.EntityPartInfo(this.parent, this.name, this.x, this.y, this.z, this.px, this.py, this.pz, this.bounds));
      }
   }

   public interface Factory {
      EntityBounds create();
   }
}
