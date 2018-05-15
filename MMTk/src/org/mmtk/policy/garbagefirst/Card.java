package org.mmtk.policy.garbagefirst;

/**
 * 
 * @author neroubas
 * Simple class to implement card marking
 */
public class Card {
  
  private byte[] cardByte;
  
  public Card(int heapSize, int grain) {
    int numCards = heapSize / grain;
    cardByte = new byte[numCards];
    for (int i = 0; i < numCards; i++) {
      cardByte[i] = (byte) 0;
    }
  }
  
  public Card(long heapSize, int grain) {
    int numCards = (int)(heapSize / grain);
    cardByte = new byte[numCards];
    for (int i = 0; i < numCards; i++) {
      cardByte[i] = (byte) 0;
    }
  }
  
  public boolean isMarked(int index) {
    return cardByte[index] == (byte) 0;
  }
}
