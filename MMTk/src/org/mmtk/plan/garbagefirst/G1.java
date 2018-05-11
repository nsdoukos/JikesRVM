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
import org.mmtk.plan.Trace;
import org.mmtk.policy.GarbageFirstSpace;
import org.mmtk.utility.heap.VMRequest;
import org.mmtk.utility.heap.layout.VMLayoutConstants;
import org.mmtk.vm.VM;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;


/**
 * This class implements the global state of a a simple allocator
 * without a collector.
 */
@Uninterruptible
public class G1 extends Plan {

  /*****************************************************************************
   * Class variables
   */

  /**
   *
   */
  /** Fraction of available virtual memory to give to the eden */
  protected static final float EDEN_VM_FRACTION = 0.5f;
  /** Fraction of available virtual memory to give to the old */
  protected static final float OLD_VM_FRACTION = 1.0f - EDEN_VM_FRACTION;

  public static final GarbageFirstSpace edenSpace = new GarbageFirstSpace("eden", true, VMRequest.highFraction(EDEN_VM_FRACTION));
  public static final int EDEN = edenSpace.getDescriptor();
  private static final Address EDEN_START = edenSpace.getStart();

  private static final Extent OLD_EXTENT = Extent
      .fromLong(EDEN_START.toLong() - (VMLayoutConstants.AVAILABLE_START.toLong()) - (4 * 1024 * 1024));
  public static final GarbageFirstSpace oldSpace = new GarbageFirstSpace("old", true, VMRequest.fixedExtent(OLD_EXTENT, true));

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


  /*****************************************************************************
   * Miscellaneous
   */

  /**
   * {@inheritDoc}
   */
  @Interruptible
  @Override
  protected void registerSpecializedMethods() {
    super.registerSpecializedMethods();
  }
}
