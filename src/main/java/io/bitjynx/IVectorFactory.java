package io.bitjynx;

@FunctionalInterface
interface IVectorFactory {
  IVector getNewVector(int[] a, int size);
}
