package org.mmtk.policy.garbagefirst;

import org.mmtk.utility.heap.layout.VMLayoutConstants;
import org.vmmagic.unboxed.Address;

/**
 *
 * @author neroubas
 * Simple class to implement card marking
 */
public class Card {

  private byte[] cardByte;
  private short[] numProcessed;
  private int grain;
  private int hotnessThresh;

  public Card(int heapSize, int grain, int hotnessThresh) {
    int numCards = heapSize / grain;
    cardByte = new byte[numCards];
    numProcessed = new short[numCards];
    for (int i = 0; i < numCards; i++) {
      cardByte[i] = (byte) 0;
      numProcessed[i] = 0;
    }
    this.grain = grain;
    this.hotnessThresh = hotnessThresh;
  }

  public Card(long heapSize, int grain, int hotnessThresh) {
    int numCards = (int)(heapSize / grain);
    cardByte = new byte[numCards];
    numProcessed = new short[numCards];
    for (int i = 0; i < numCards; i++) {
      cardByte[i] = (byte) 0;
      numProcessed[i] = 0;
    }
    this.grain = grain;
    this.hotnessThresh = hotnessThresh;
  }

  public boolean isMarked(int index) {
    return cardByte[index] == (byte) 0;
  }

  public boolean isMarked(Address addr) {
    int index = addr.minus(VMLayoutConstants.AVAILABLE_START.toLong()).toInt() / grain;
    return isMarked(index);
  }

  public void markCard(int index) {
    cardByte[index] = (byte) 1;
  }

  public void markCard(Address addr) {
    int index = addr.minus(VMLayoutConstants.AVAILABLE_START.toLong()).toInt() / grain;
    markCard(index);
  }

  public byte getCard(int index) {
    return cardByte[index];
  }

  public void setCard(int index, byte value) {
    cardByte[index] = value;
  }

  public short getNumProcessed(int index) {
    return numProcessed[index];
  }

  public short getNumProcessed(Address addr) {
    int index = addr.minus(VMLayoutConstants.AVAILABLE_START.toLong()).toInt() / grain;
    return getNumProcessed(index);
  }

  public void incNumProcessed(int index) {
    numProcessed[index]++;
  }

  public void incNumProcessed(Address addr) {
    int index = addr.minus(VMLayoutConstants.AVAILABLE_START.toLong()).toInt() / grain;
    incNumProcessed(index);
  }

  public boolean isHot(int index) {
    return numProcessed[index] >= hotnessThresh;
  }

  public boolean isHot(Address addr) {
    int index = addr.minus(VMLayoutConstants.AVAILABLE_START.toLong()).toInt() / grain;
    return isHot(index);
  }
}
