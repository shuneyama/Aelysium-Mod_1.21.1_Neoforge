package net.aelysium.aelysiummod.sistemas.chefes.base.dados;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.jetbrains.annotations.NotNull;

public class HistoricalData<T> implements List<T> {
   private final int maxHistory;
   private final List<T> history;

   public HistoricalData(T initialValue, int maxHistory) {
      if (maxHistory < 2) {
         throw new IllegalArgumentException("Max History cannot be less than 2");
      } else {
         this.maxHistory = maxHistory;
         this.history = new ArrayList<>();
         this.history.add(initialValue);
      }
   }

   @Override
   public int size() {
      return this.history.size();
   }

   @Override
   public boolean isEmpty() {
      return this.history.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      return this.history.contains(o);
   }

   @NotNull
   @Override
   public Iterator<T> iterator() {
      return this.history.iterator();
   }

   @NotNull
   @Override
   public Object[] toArray() {
      return this.history.toArray();
   }

   @NotNull
   @Override
   public <T1> T1[] toArray(@NotNull T1[] a) {
      return this.history.toArray(a);
   }

   @Override
   public boolean add(T value) {
      this.history.add(value);
      if (this.history.size() > this.maxHistory) {
         this.history.remove(0);
      }

      return true;
   }

   @Override
   public void add(int index, T value) {
      throw new UnsupportedOperationException("Add with index is not supported.");
   }

   @Override
   public boolean addAll(@NotNull Collection<? extends T> c) {
      for (T value : c) {
         this.add(value);
      }

      return true;
   }

   @Override
   public boolean addAll(int index, @NotNull Collection<? extends T> c) {
      throw new UnsupportedOperationException("AddAll with index is not supported.");
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return new HashSet<>(this.history).containsAll(c);
   }

   @Override
   public boolean remove(Object o) {
      return this.history.remove(o);
   }

   @Override
   public T remove(int index) {
      return this.history.remove(index);
   }

   @Override
   public boolean removeAll(@NotNull Collection<?> c) {
      return this.history.removeAll(c);
   }

   @Override
   public boolean retainAll(@NotNull Collection<?> c) {
      return this.history.retainAll(c);
   }

   @Override
   public void clear() {
      this.history.clear();
   }

   @Override
   public T get(int past) {
      if (past < 0) {
         throw new IllegalArgumentException("Past cannot be negative");
      } else {
         int clampedPast = Math.max(this.history.size() - 1 - past, 0);
         return this.history.get(clampedPast);
      }
   }

   public List<T> getAll() {
      return new ArrayList<>(this.history);
   }

   @Override
   public T set(int index, T element) {
      return this.history.set(index, element);
   }

   @Override
   public int indexOf(Object o) {
      return this.history.indexOf(o);
   }

   @Override
   public int lastIndexOf(Object o) {
      return this.history.lastIndexOf(o);
   }

   @NotNull
   @Override
   public ListIterator<T> listIterator() {
      return this.history.listIterator();
   }

   @NotNull
   @Override
   public ListIterator<T> listIterator(int index) {
      return this.history.listIterator(index);
   }

   @NotNull
   @Override
   public List<T> subList(int fromIndex, int toIndex) {
      return this.history.subList(fromIndex, toIndex);
   }
}
