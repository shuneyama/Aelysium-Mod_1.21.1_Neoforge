package net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util;

import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.MutableBox;
import net.aelysium.aelysiummod.sistemas.chefes.mixin.InvocadorFormaVoxelArray;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CompoundOrientedBox extends AABB implements Iterable<OrientedBox> {
   private final Collection<OrientedBox> boxes;
   private VoxelShape cached;
   @Nullable
   private final MutableBox overrideBox;

   public CompoundOrientedBox(AABB bounds, Collection<OrientedBox> boxes, MutableBox overrideBox) {
      this(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ, boxes, overrideBox);
   }

   public CompoundOrientedBox(
      double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Collection<OrientedBox> boxes, @Nullable MutableBox overrideBox
   ) {
      super(minX, minY, minZ, maxX, maxY, maxZ);
      this.boxes = boxes;
      this.overrideBox = overrideBox;
   }

   private CompoundOrientedBox(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      Collection<OrientedBox> boxes,
      VoxelShape cached,
      @Nullable MutableBox overrideBox
   ) {
      super(minX, minY, minZ, maxX, maxY, maxZ);
      this.boxes = boxes;
      this.cached = cached;
      this.overrideBox = overrideBox;
   }

   @NotNull
   public AABB inflate(double x, double y, double z) {
      List<OrientedBox> orientedBoxes = new ObjectArrayList(this.boxes.size());

      for (OrientedBox box : this.boxes) {
         orientedBoxes.add(box.expand(x, y, z));
      }

      MutableBox overrideBox = null;
      if (this.overrideBox != null) {
         overrideBox = new MutableBox(this.overrideBox.getBox().inflate(x, y, z));
      }

      return this.cached != null
         ? new CompoundOrientedBox(
            this.minX - x, this.minY - y, this.minZ - z, this.maxX + x, this.maxY + y, this.maxZ + z, orientedBoxes, this.cached.move(x, y, z), overrideBox
         )
         : new CompoundOrientedBox(this.minX - x, this.minY - y, this.minZ - z, this.maxX + x, this.maxY + y, this.maxZ + z, orientedBoxes, overrideBox);
   }

   @NotNull
   public AABB move(double x, double y, double z) {
      List<OrientedBox> orientedBoxes = new ObjectArrayList(this.boxes.size());

      for (OrientedBox box : this.boxes) {
         orientedBoxes.add(box.offset(x, y, z));
      }

      MutableBox overrideBox = null;
      if (this.overrideBox != null) {
         overrideBox = new MutableBox(this.overrideBox.getBox().move(x, y, z));
      }

      return this.cached != null
         ? new CompoundOrientedBox(
            this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z, orientedBoxes, this.cached.move(x, y, z), overrideBox
         )
         : new CompoundOrientedBox(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z, orientedBoxes, overrideBox);
   }

   @NotNull
   public AABB move(BlockPos blockPos) {
      return this.move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
   }

   @NotNull
   public Optional<Vec3> clip(@NotNull Vec3 min, @NotNull Vec3 max) {
      double t = Double.MAX_VALUE;

      for (OrientedBox box : this.boxes) {
         double tmp = box.raycast(min, max);
         if (tmp != -1.0) {
            t = Math.min(t, tmp);
         }
      }

      if (t != Double.MAX_VALUE) {
         double d = max.x - min.x;
         double e = max.y - min.y;
         double f = max.z - min.z;
         return Optional.of(min.add(t * d, t * e, t * f));
      } else {
         return Optional.empty();
      }
   }

   @NotNull
   @Override
   public Iterator<OrientedBox> iterator() {
      return Iterators.unmodifiableIterator(this.boxes.iterator());
   }

   public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return this.intersects(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
   }

   public boolean intersects(@NotNull AABB box) {
      Vec3[] vertices = OrientedBox.getVertices(box);

      for (OrientedBox orientedBox : this.boxes) {
         if (orientedBox.intersects(vertices)) {
            return true;
         }
      }

      return false;
   }

   public VoxelShape toVoxelShape() {
      if (this.cached != null) {
         return this.cached;
      } else if (this.overrideBox != null) {
         this.cached = Shapes.create(this.overrideBox.getBox());
         return this.cached;
      } else {
         double minX = this.min(Axis.X) + 1.0E-4;
         double minY = this.min(Axis.Y) + 1.0E-4;
         double minZ = this.min(Axis.Z) + 1.0E-4;
         double deltaX = this.max(Axis.X) - minX;
         double deltaY = this.max(Axis.Y) - minY;
         double deltaZ = this.max(Axis.Z) - minZ;
         double resolution = 4.0;
         int xResolution = (int)Math.ceil(deltaX * resolution + 1.0E-4);
         int yResolution = (int)Math.ceil(deltaY * resolution + 1.0E-4);
         int zResolution = (int)Math.ceil(deltaZ * resolution + 1.0E-4);
         BitSetDiscreteVoxelShape bitSet = new BitSetDiscreteVoxelShape(xResolution, yResolution, zResolution);

         for (int i = 0; i < xResolution; i++) {
            double x = minX + (double)i / resolution;

            for (int j = 0; j < zResolution; j++) {
               double z = minZ + (double)j / resolution;

               for (int k = 0; k < yResolution; k++) {
                  double y = minY + (double)k / resolution;
                  AABB box = new AABB(x, y, z, x + 0.9999 / (double)xResolution, y + 0.9999 / (double)yResolution, z + 0.9999 / (double)zResolution);
                  if (this.intersects(box)) {
                     bitSet.fill(i, k, j);
                  }
               }
            }
         }

         DoubleList xPoints = new DoubleArrayList(xResolution + 1);

         for (int i = 0; i < xResolution + 1; i++) {
            xPoints.add(minX + (double)i / resolution);
         }

         DoubleList yPoints = new DoubleArrayList(yResolution + 1);

         for (int i = 0; i < yResolution + 1; i++) {
            yPoints.add(minY + (double)i / resolution);
         }

         DoubleList zPoints = new DoubleArrayList(zResolution + 1);

         for (int i = 0; i < zResolution + 1; i++) {
            zPoints.add(minZ + (double)i / resolution);
         }

         return this.cached = InvocadorFormaVoxelArray.aelysium$criar(bitSet, xPoints, yPoints, zPoints);
      }
   }

   public boolean contains(double x, double y, double z) {
      for (OrientedBox box : this.boxes) {
         if (box.contains(x, y, z)) {
            return true;
         }
      }

      return false;
   }

   public CompoundOrientedBox withBounds(AABB bounds) {
      return new CompoundOrientedBox(bounds, new ObjectArrayList(this.boxes), this.overrideBox);
   }

   public double calculateMaxDistance(Axis axis, VoxelShape voxelShape, double maxDist) {
      for (AABB boundingBox : this.toVoxelShape().toAabbs()) {
         maxDist = voxelShape.collide(axis, boundingBox, maxDist);
         if (Math.abs(maxDist) < 1.0E-4) {
            return 0.0;
         }
      }

      return maxDist;
   }
}
