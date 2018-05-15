/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.mmtk.plan.garbagefirst;


import org.mmtk.plan.Plan;
import org.mmtk.plan.StopTheWorld;
import org.mmtk.plan.Trace;
import org.mmtk.policy.garbagefirst.GarbageFirstSpace;
import org.mmtk.policy.garbagefirst.Card;
import org.mmtk.utility.deque.SharedDeque;
import org.mmtk.utility.heap.VMRequest;
import org.mmtk.utility.heap.layout.HeapLayout;
import org.mmtk.utility.heap.layout.VMLayoutConstants;
import org.mmtk.vm.VM;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;


/**
 * This class implements the global state of a a simple allocator
 * without a collector.
 */
@Uninterruptible
public class G1 extends StopTheWorld {

  /*****************************************************************************
   * Class variables
   */
  /**
   * Global remember sets
   */
  public static final SharedDeque remsetPool = new SharedDeque("remSets",metaDataSpace, 1);
  public static Card cardMap = new Card(VMLayoutConstants.AVAILABLE_BYTES.toLong(), 512);
  /**
   *
   */
  /** Fraction of available virtual memory to give to the eden */
  public static final float EDEN_VM_FRACTION = 0.5f;
  /** Fraction of available virtual memory to give to the old */
  public static final float OLD_VM_FRACTION = 1.0f - EDEN_VM_FRACTION;

  public static final GarbageFirstSpace edenSpace = new GarbageFirstSpace("eden", true, VMRequest.fraction(EDEN_VM_FRACTION));
  public static final int EDEN = edenSpace.getDescriptor();
  public static final Address EDEN_START = edenSpace.getStart();

  private static long OLD_BYTES = (long) (OLD_VM_FRACTION * VMLayoutConstants.AVAILABLE_BYTES.toLong());
  private static Extent OLD_EXTENT = Extent.fromLong(OLD_BYTES- (4*1024*1024));
  //private static final Extent OLD_EXTENT = Extent.fromLong(EDEN_START.toLong() - (VMLayoutConstants.AVAILABLE_START.toLong()) - (4 * 1024 * 1024));
  public static final GarbageFirstSpace oldSpace = new GarbageFirstSpace("old", true, VMRequest.fixedExtent(OLD_EXTENT, false));

  public static final int OLD = oldSpace.getDescriptor();
  public static final Address OLD_START = oldSpace.getStart();
  

  /*****************************************************************************
   * Instance variables
   */

  /**
   *
   */
  public final Trace edenTrace = new Trace(metaDataSpace);

  
  /*****************************************************************************
   * Collection
   */

  /**
   * {@inheritDoc}
   */
  @Inline
  @Override
  public final void collectionPhase(short phaseId) {
    if (VM.VERIFY_ASSERTIONS) VM.assertions._assert(false);
    /*
    if (phaseId == PREPARE) {
    }
    if (phaseId == CLOSURE) {
    }
    if (phaseId == RELEASE) {
    }
    super.collectionPhase(phaseId);
    */
  }

  /*****************************************************************************
   * Accounting
   */

  /**
   * {@inheritDoc}
   * The superclass accounts for its spaces, we just
   * augment this with the default space's contribution.
   */
  @Override
  public int getPagesUsed() {
    return (edenSpace.reservedPages() + super.getPagesUsed());
  }

  /**
   * Return the number of pages available for allocation, <i>assuming
   * all future allocation is to the nursery</i>.
   *
   * @return The number of pages available for allocation, <i>assuming
   * all future allocation is to the nursery</i>.
   */
  @Override
  public int getPagesAvail() {
    return super.getPagesAvail() >> 1;
  }

  /*****************************************************************************
   * Miscellaneous
   */

//  /**
//   * Return {@code true} if the address resides within the nursery
//   *
//   * @param addr The object to be tested
//   * @return {@code true} if the address resides within the nursery
//   */
//  @Inline
//  static boolean inNursery(Address addr) {
//      return addr.GE(EDEN_START) && addr.LT(OLD_START);
//  }
//
//  /**
//   * Return {@code true} if the object resides within the nursery
//   *
//   * @param obj The object to be tested
//   * @return {@code true} if the object resides within the nursery
//   */
//  @Inline
//  static boolean inNursery(ObjectReference obj) {
//    return inNursery(obj.toAddress());
//  }
  
  /**
   * {@inheritDoc}
   */
  @Interruptible
  @Override
  protected void registerSpecializedMethods() {
    super.registerSpecializedMethods();
  }
  
  public static SharedDeque getRemsetPool() {
    return remsetPool;
  }
}
