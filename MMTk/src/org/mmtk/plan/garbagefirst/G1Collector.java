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

import org.mmtk.plan.CollectorContext;
import org.mmtk.plan.StopTheWorldCollector;
import org.mmtk.plan.TraceLocal;
import org.mmtk.plan.marksweep.MS;
import org.mmtk.plan.marksweep.MSMutator;
import org.mmtk.vm.VM;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;

/**
 * This class implements <i>per-collector thread</i> behavior
 * and state for the <i>MS</i> plan, which implements a full-heap
 * mark-sweep collector.<p>
 *
 * Specifically, this class defines <i>MS</i> collection behavior
 * (through <code>trace</code> and the <code>collectionPhase</code>
 * method).<p>
 *
 * @see MS for an overview of the mark-sweep algorithm.
 *
 * @see MS
 * @see MSMutator
 * @see StopTheWorldCollector
 * @see CollectorContext
 */
@Uninterruptible
public class G1Collector extends StopTheWorldCollector {

  /****************************************************************************
   * Instance fields
   */

  /**
   *
   */
  protected G1TraceLocal fullTrace = new G1TraceLocal(global().g1Trace, null);
  protected TraceLocal currentTrace = fullTrace;


  /****************************************************************************
   * Collection
   */

  /**
   * {@inheritDoc}
   */
  @Inline
  @Override
  public void collectionPhase(short phaseId, boolean primary) {
    if (phaseId == G1.PREPARE) {
      super.collectionPhase(phaseId, primary);
      fullTrace.prepare();
      return;
    }

    if (phaseId == G1.CLOSURE) {
      fullTrace.completeTrace();
      return;
    }

    if (phaseId == G1.RELEASE) {
      fullTrace.release();
      super.collectionPhase(phaseId, primary);
      return;
    }

    super.collectionPhase(phaseId, primary);
  }


  /****************************************************************************
   * Miscellaneous
   */

  /** @return The active global plan as an <code>MS</code> instance. */
  @Inline
  private static G1 global() {
    return (G1) VM.activePlan.global();
  }

  @Override
  public final TraceLocal getCurrentTrace() {
    return currentTrace;
  }
}
