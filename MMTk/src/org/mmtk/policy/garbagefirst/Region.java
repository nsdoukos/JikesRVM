package org.mmtk.policy.garbagefirst;

import org.mmtk.utility.deque.SharedDeque;
import org.mmtk.utility.deque.WriteBuffer;
import org.vmmagic.pragma.Inline;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/*
 * This class defines operations in region-granularity
 */
public class Region {

  /* Min Region size 1MB */
  public static final int MIN_REGION_BYTES = 1 << 20;
  public static final int MIN_REGION_LOG = 20;
  /* Max Region size 32MB */
  public static final int MAX_REGION_BYTES = 32 << 20;
  public static final int MAX_REGION_LOG = 22;
  /* User defined region size */
  public static final int REGION_SIZE = 1 << 19;
  public static final int REGION_LOG = 19;
  
  protected final Address start;
  protected final Extent extent;
  protected final WriteBuffer remset;
  
  private boolean isEden;
  private boolean isSurvivor;
  private boolean isOld;
  private short survived;
  private short survive_thresh;
  
  private short rs_elements;
  

  public Region(Address addr, Extent extent, SharedDeque glbRemset) {
    start = addr;
    this.extent = extent;
    remset = new WriteBuffer(glbRemset);
    isEden = true;
    isSurvivor = false;
    isOld = false;
    survived = 0;
    survive_thresh = 3;
    rs_elements = 0;
  }
  
  public Address getAddress() {
    return start;
  }
  
  public WriteBuffer getWriteBuffer() {
    return remset;
  }
  
  public void setEden() {
    isEden = true;
    isSurvivor = false;
    isOld = false;
  }
  
  private void setSurvivor() {
    isEden = false;
    isSurvivor = true;
    isOld = false;
  }
  
  private void setOld() {
    isEden = false;
    isSurvivor = false;
    isOld = true;
  }
  
  @Inline
  public boolean isEden() {
    return isEden;
  }
  
  @Inline
  public boolean isSurvivor() {
    return isSurvivor;
  }
  
  @Inline
  public boolean isOld() {
    return isOld;
  }
  
  public void survived() {
    if (isOld) return;
    if (isEden) {
      setSurvivor();
      survived++;
    }
    else if (isSurvivor && survive_thresh < 3) {
      survived++;
    }
    else if (isSurvivor && survive_thresh == 3) {
      setOld();
    }
  }
  
  public void rsElementsInc() {
    rs_elements++;
  }
  
  public short getRsElements() {
    return rs_elements;
  }
}
