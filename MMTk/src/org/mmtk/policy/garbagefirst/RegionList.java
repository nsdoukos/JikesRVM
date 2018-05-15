package org.mmtk.policy.garbagefirst;

import org.mmtk.plan.Plan;
import org.mmtk.plan.garbagefirst.*;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.AddressArray;
import org.vmmagic.unboxed.Extent;
import org.mmtk.utility.GenericFreeList;
import org.mmtk.utility.IntArrayFreeList;
import org.mmtk.utility.deque.SharedDeque;
import org.mmtk.utility.deque.WriteBuffer;
import org.mmtk.utility.heap.layout.VMLayoutConstants;
import org.mmtk.vm.VM;

/*
 * Regions list to make allocation a constant time operation
 */
@Uninterruptible
public class RegionList extends GenericFreeList{
  
  private static final int NUM_REGIONS = GarbageFirstSpace.HEAP_USED_BYTES / Region.REGION_SIZE;
  private static final int EDEN_REGIONS = GarbageFirstSpace.EDEN_USED_BYTES / Region.REGION_SIZE;
  private static final Extent regExtent = Extent.fromIntSignExtend(GarbageFirstSpace.HEAP_USED_BYTES / NUM_REGIONS);

  private final RegionList parent;
  private Region[] table;
  
  /**
   * Constructor
   * 
   * @param units The number of allocatable units for the region list
   * @param glbRemset 
   */
  public RegionList(SharedDeque glbRemset) {
    int units = NUM_REGIONS;
    this.parent = null;
    if (VM.VERIFY_ASSERTIONS) VM.assertions._assert(units <= MAX_UNITS && heads <= MAX_HEADS);
    this.heads = 1;
    
    table = new Region[(units + 1 + heads) << 1];
    initializeHeap(units, units);
    Address tmpAddress = VMLayoutConstants.AVAILABLE_START;
    for (int i = 1; i <= units; i++) {
      table[i] = new Region(tmpAddress, regExtent, glbRemset);
      tmpAddress = tmpAddress.plus(regExtent);
    }
  }
  
  
  /**
   * Resize the free list for a child free list.
   * This must not be called dynamically (ie not after bootstrap).
   */
  @Override
  public void resizeFreeList() {
    if (VM.VERIFY_ASSERTIONS) VM.assertions._assert(parent != null && !Plan.isInitialized());
    table = parent.getTable();
  }
  
  /**
   * Resize the free list for a parent free list.
   * This must not be called dynamically (ie not after bootstrap).
   *
   * @param units The number of allocatable units for this free list
   * @param grain Units are allocated such that they will never cross this granularity boundary
   */
  @Override
  public void resizeFreeList(int units, int grain) {
    if (VM.VERIFY_ASSERTIONS) VM.assertions._assert(units <= MAX_UNITS && heads <= MAX_HEADS);
    table = new Region[(units + 1 + heads) << 1];
    initializeHeap(units, units);

  }

  @Override
  public void dbgPrintDetail() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void dbgPrintSummary() {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected int getEntry(int index) {
    // TODO Auto-generated method stub
    return 0;
  }
  
  public Region getRegion(int index) {
    return table[index];
  }
  
  public Region getRegion(Address addr) {
    Address start = VMLayoutConstants.AVAILABLE_START;
    int index = addr.minus(start.toLong()).toInt() / regExtent.toInt();

    return getRegion(index + 1);
  }
  
  public Address getAddress(int index) {
    return table[index].getAddress();
  }
  
  public WriteBuffer getWriteBuffer(int index) {
    return table[index].getWriteBuffer();
  }

  @Override
  protected void setEntry(int index, int value) {
    // TODO Auto-generated method stub
    
  }
  
  /* Getter */
  Region[] getTable() {
    return table;
  }
//  public void setEdenList(int index, int value) {
//    edenFreeList.setEntry(index, value);
//  }
  
}
