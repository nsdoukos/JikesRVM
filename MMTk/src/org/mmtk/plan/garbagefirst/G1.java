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

import org.mmtk.plan.StopTheWorld;
import org.mmtk.plan.Trace;
import org.mmtk.plan.TransitiveClosure;
import org.mmtk.policy.MarkSweepSpace;
import org.mmtk.policy.Space;
import org.mmtk.utility.heap.VMRequest;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.ObjectReference;

/**
 * This class implements the global state of a simple mark-sweep collector.<p>
 *
 * All plans make a clear distinction between <i>global</i> and
 * <i>thread-local</i> activities, and divides global and local state
 * into separate class hierarchies.  Global activities must be
 * synchronized, whereas no synchronization is required for
 * thread-local activities.  There is a single instance of Plan (or the
 * appropriate sub-class), and a 1:1 mapping of PlanLocal to "kernel
 * threads" (aka CPUs).  Thus instance
 * methods of PlanLocal allow fast, unsychronized access to functions such as
 * allocation and collection.<p>
 *
 * The global instance defines and manages static resources
 * (such as memory and virtual memory resources).  This mapping of threads to
 * instances is crucial to understanding the correctness and
 * performance properties of MMTk plans.
 */
@Uninterruptible
public class G1 extends StopTheWorld {

  /****************************************************************************
   * Class variables
   */

  /**
   *
   */
  public static final MarkSweepSpace g1Space = new MarkSweepSpace("ms", VMRequest.discontiguous());
  public static final int MARK_SWEEP = g1Space.getDescriptor();

  public static final int SCAN_MARK = 0;


  /****************************************************************************
   * Instance variables
   */

  /**
   *
   */
  public final Trace g1Trace = new Trace(metaDataSpace);


  /*****************************************************************************
   * Collection
   */

  /**
   * {@inheritDoc}
   */
  @Inline
  @Override
  public void collectionPhase(short phaseId) {

    if (phaseId == PREPARE) {
      super.collectionPhase(phaseId);
      g1Trace.prepare();
      g1Space.prepare(true);
      return;
    }

    if (phaseId == CLOSURE) {
      g1Trace.prepare();
      return;
    }
    if (phaseId == RELEASE) {
      g1Trace.release();
      g1Space.release();
      super.collectionPhase(phaseId);
      return;
    }

    super.collectionPhase(phaseId);
  }

  /*****************************************************************************
   * Accounting
   */

  /**
   * {@inheritDoc}
   * The superclass accounts for its spaces, we just
   * augment this with the mark-sweep space's contribution.
   */
  @Override
  public int getPagesUsed() {
    return (g1Space.reservedPages() + super.getPagesUsed());
  }

  /*****************************************************************************
   * Miscellaneous
   */

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean willNeverMove(ObjectReference object) {
    if (Space.isInSpace(MARK_SWEEP, object))
      return true;
    return super.willNeverMove(object);
  }

  @Interruptible
  @Override
  protected void registerSpecializedMethods() {
    TransitiveClosure.registerSpecializedScan(SCAN_MARK, G1TraceLocal.class);
    super.registerSpecializedMethods();
  }
}
