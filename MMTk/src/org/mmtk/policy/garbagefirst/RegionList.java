package org.mmtk.policy.garbagefirst;

import org.mmtk.plan.garbagefirst.*;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.AddressArray;

/*
 * Regions list to make allocation a constant time operation
 */
@Uninterruptible
public class RegionList {
  
  protected static final int NUM_REGIONS = GarbageFirstSpace.HEAP_USED_BYTES / Region.REGION_SIZE;
  protected static final AddressArray regionsFreeList = AddressArray.create(NUM_REGIONS);
  //protected static final IntArrayFreeList edenFreeList = new IntArrayFreeList(0);
  //protected static final IntArrayFreeList oldFreeList = new IntArrayFreeList(0);
  
  public RegionList() {
    //System.out.println("MPHKA \nnum: "+NUM_REGIONS+ " used: "+GarbageFirstSpace.HEAP_USED_BYTES+" Size: "+Region.REGION_SIZE);
//
//    int offset = Region.REGION_SIZE;
//    int edenStart = G1.EDEN_START.toInt();
//    for (int i = 0; i < NUM_REGIONS; i++) {
//      System.out.println("i: "+ i);
//      regionsFreeList.set(i, Address.fromIntSignExtend(edenStart + offset));
//      edenStart += offset;
//      System.out.println(regionsFreeList.get(i));
//    }
  }
  
  public void setRegionList(int index, Address value) {
    //System.out.println("Addr: "+ value.toString()+"reg: "+index);
    regionsFreeList.set(index, value);
  }
  
  
}
