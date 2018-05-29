package io.bitjynx;

import java.util.Arrays;
import java.util.stream.IntStream;

abstract class AbstractBitPosBlock implements IBlock {

  // Used as 'unsigned short'
  protected final short[] _positions;

  protected AbstractBitPosBlock(short[] positions) {
    this._positions = positions;
  }

  @Override
  public abstract Type getType();

  @Override
  public abstract int cardinality();

  @Override
  public abstract boolean exists(int pos);

  @Override
  public abstract int lastBitPos();

  @Override
  public abstract IntStream stream();

  @Override
  public abstract IBlock not();

  @Override
  public abstract IBlock and(IBlock v2);

  @Override
  public abstract IBlock or(IBlock v2);

  @Override
  public abstract IBlock xor(IBlock v2);

  @Override
  public abstract IBlock nand(IBlock v2);

  @Override
  public int hashCode() { return _positions.length + getType().ordinal(); } // Common for BitPosBlock and ZeroPosBlock

  static boolean isEquivalent(AbstractBitPosBlock b1, AbstractBitPosBlock b2) {
    if( b1 instanceof BitPosBlock ) {
      if (b2 instanceof BitPosBlock)
        return Arrays.equals(b1._positions, b2._positions);
      else if (b2 instanceof ZeroPosBlock) {
        return ((ZeroPosBlock) b2).xorOnes(b1._positions).length == BitJynx.BITS_PER_BLOCK;
      }
      else
        return false;
    }
    else if (b1 instanceof ZeroPosBlock) {
      if (b2 instanceof ZeroPosBlock)
        return Arrays.equals(b1._positions, b2._positions);
      else if (b2 instanceof BitPosBlock) {
        return ((ZeroPosBlock) b1).xorOnes(b2._positions).length == BitJynx.BITS_PER_BLOCK;
      }
      else
        return false;
    }
    return false;
  }

  protected static IBlock newBitPosBlock(short[] result) {
    switch(result.length) {
      case 0:
        return EmptyBlock.instance;
      case BitJynx.BITS_PER_BLOCK:
        return UnityBlock.instance;
      default:
        return new BitPosBlock(result);
    }
  }

  protected static IBlock newZeroPosBlock(short[] result) {
    switch(result.length) {
      case 0:
        return UnityBlock.instance;
      case BitJynx.BITS_PER_BLOCK:
        return EmptyBlock.instance;
      default:
        return new ZeroPosBlock(result);
    }
  }

  protected static short[] andLike2(short[] positions1, short[] positions2) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(positions1.length, positions2.length)];

    while (i < positions1.length && j < positions2.length) {
      if (positions1[i] < positions2[j])
        ++i;
      else if (positions2[j] < positions1[i])
        ++j;
      else {
        temp[counter++] = positions1[i];
        ++i;
        ++j;
      }
    }
    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  protected static short[] andLike(short[] positions1, short[] positions2) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(positions1.length, positions2.length)];

    int dif = 0;
    while (i < positions1.length && j < positions2.length) {
      dif = positions1[i] - positions2[j];
      temp[counter] = positions1[i];
      i = i + (dif <= 0 ? 1 : 0); // if (positions1[i] <= positions2[j])
      j = j + (dif >= 0 ? 1 : 0); // if (positions1[i] >= positions2[j])
      counter = counter + (dif == 0 ? 1 : 0);

//      if (i >= positions1.length || j >= positions2.length) break;
//      dif = positions1[i] - positions2[j];
//      temp[counter] = positions1[i];
//      i = i + (dif <= 0 ? 1 : 0); // if (positions1[i] <= positions2[j])
//      j = j + (dif >= 0 ? 1 : 0); // if (positions1[i] >= positions2[j])
//      counter = counter + (dif == 0 ? 1 : 0);
//
//      if (i >= positions1.length || j >= positions2.length) break;
//      dif = positions1[i] - positions2[j];
//      temp[counter] = positions1[i];
//      i = i + (dif <= 0 ? 1 : 0); // if (positions1[i] <= positions2[j])
//      j = j + (dif >= 0 ? 1 : 0); // if (positions1[i] >= positions2[j])
//      counter = counter + (dif == 0 ? 1 : 0);
//
//      if (i >= positions1.length || j >= positions2.length) break;
//      dif = positions1[i] - positions2[j];
//      temp[counter] = positions1[i];
//      i = i + (dif <= 0 ? 1 : 0); // if (positions1[i] <= positions2[j])
//      j = j + (dif >= 0 ? 1 : 0); // if (positions1[i] >= positions2[j])
//      counter = counter + (dif == 0 ? 1 : 0);
//
//      if (i >= positions1.length || j >= positions2.length) break;
//      dif = positions1[i] - positions2[j];
//      temp[counter] = positions1[i];
//      i = i + (dif <= 0 ? 1 : 0); // if (positions1[i] <= positions2[j])
//      j = j + (dif >= 0 ? 1 : 0); // if (positions1[i] >= positions2[j])
//      counter = counter + (dif == 0 ? 1 : 0);
//
//      if (i >= positions1.length || j >= positions2.length) break;
//      dif = positions1[i] - positions2[j];
//      temp[counter] = positions1[i];
//      i = i + (dif <= 0 ? 1 : 0); // if (positions1[i] <= positions2[j])
//      j = j + (dif >= 0 ? 1 : 0); // if (positions1[i] >= positions2[j])
//      counter = counter + (dif == 0 ? 1 : 0);
//
//      if (i >= positions1.length || j >= positions2.length) break;
//      dif = positions1[i] - positions2[j];
//      temp[counter] = positions1[i];
//      i = i + (dif <= 0 ? 1 : 0); // if (positions1[i] <= positions2[j])
//      j = j + (dif >= 0 ? 1 : 0); // if (positions1[i] >= positions2[j])
//      counter = counter + (dif == 0 ? 1 : 0);
//
//      if (i >= positions1.length || j >= positions2.length) break;
//      dif = positions1[i] - positions2[j];
//      temp[counter] = positions1[i];
//      i = i + (dif <= 0 ? 1 : 0); // if (positions1[i] <= positions2[j])
//      j = j + (dif >= 0 ? 1 : 0); // if (positions1[i] >= positions2[j])
//      counter = counter + (dif == 0 ? 1 : 0);
    }
    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  protected static short[] orLike(short[] positions1, short[] positions2) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(positions1.length + positions2.length, BitJynx.BITS_PER_BLOCK)];

    while (i < positions1.length && j < positions2.length) {
      if (positions1[i] < positions2[j]) {
        temp[counter++] = positions1[i++];
      } else if (positions2[j] < positions1[i]) {
        temp[counter++] = positions2[j++];
      } else {
        temp[counter++] = positions1[i++];
        ++j;
      }
    }
    // Add remaining elements
    while (i < positions1.length)
      temp[counter++] = positions1[i++];
    while (j < positions2.length)
      temp[counter++] = positions2[j++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  /**
   * Performs exclusive disjunction operation (XOR), good for both unity and zero blocks
   *
   * @param positions1 pos array
   * @param positions2 pos array
   * @return unity pos array
   */
  protected static short[] xorLike(short[] positions1, short[] positions2) {
    int i = 0, j = 0, counter = 0;
    short[] temp = new short[Integer.min(positions1.length + positions2.length, BitJynx.BITS_PER_BLOCK)];

    while (i < positions1.length && j < positions2.length) {
      if (positions1[i] < positions2[j]) {
        temp[counter++] = positions1[i++];
      } else if (positions2[j] < positions1[i]) {
        temp[counter++] = positions2[j++];
      } else {
        ++i;
        ++j;
      }
    }
    // Add remaining elements
    while (i < positions1.length)
      temp[counter++] = positions1[i++];
    while (j < positions2.length)
      temp[counter++] = positions2[j++];

    short[] result = new short[counter];
    System.arraycopy(temp, 0, result, 0, counter);
    return result;
  }

  // Taken from java.util.Arrays, adapted for 'unsigned short'
  protected static int unsignedBinarySearch(short[] a, int key) {
    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      short midVal = a[mid];

      int intMidVal = Short.toUnsignedInt(midVal);
      if (intMidVal < key)
        low = mid + 1;
      else if (intMidVal > key)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1);  // key not found.
  }

}
