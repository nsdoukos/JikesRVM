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

import org.mmtk.plan.MutatorContext;
import org.mmtk.plan.StopTheWorldMutator;
import org.mmtk.plan.marksweep.MS;
import org.mmtk.plan.marksweep.MSCollector;
import org.mmtk.policy.MarkSweepLocal;
import org.mmtk.policy.Space;
import org.mmtk.utility.alloc.Allocator;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * This class implements <i>per-mutator thread</i> behavior
 * and state for the <i>MS</i> plan, which implements a full-heap
 * mark-sweep collector.<p>
 *
 * Specifically, this class defines <i>MS</i> mutator-time allocation
 * and per-mutator thread collection semantics (flushing and restoring
 * per-mutator allocator state).
 *
 * @see MS
 * @see MSCollector
 * @see StopTheWorldMutator
 * @see MutatorContext
 */
@Uninterruptible
public class G1Mutator extends StopTheWorldMutator {

  /****************************************************************************
   * Instance fields
   */

  /**
   *
   */
  protected MarkSweepLocal g1 = new MarkSweepLocal(G1.g1Space);


  /****************************************************************************
   * Mutator-time allocation
   */

  /**
   * {@inheritDoc}<p>
   *
   * This class handles the default allocator from the mark sweep space,
   * and delegates everything else to the superclass.
   */
  @Inline
  @Override
  public Address alloc(int bytes, int align, int offset, int allocator, int site) {
    if (allocator == G1.ALLOC_DEFAULT) {
      return g1.alloc(bytes, align, offset);
    }
    return super.alloc(bytes, align, offset, allocator, site);
  }

  /**
   * {@inheritDoc}<p>
   *
   * Initialize the object header for objects in the mark-sweep space,
   * and delegate to the superclass for other objects.
   */
  @Inline
  @Override
  public void postAlloc(ObjectReference ref, ObjectReference typeRef,
      int bytes, int allocator) {
    if (allocator == G1.ALLOC_DEFAULT)
      G1.g1Space.postAlloc(ref);
    else
      super.postAlloc(ref, typeRef, bytes, allocator);
  }

  @Override
  public Allocator getAllocatorFromSpace(Space space) {
    if (space == G1.g1Space)
      return g1;
    return super.getAllocatorFromSpace(space);
  }


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
      g1.prepare();
      return;
    }

    if (phaseId == G1.RELEASE) {
      g1.release();
      super.collectionPhase(phaseId, primary);
      return;
    }

    super.collectionPhase(phaseId, primary);
  }

  @Override
  public void flush() {
    super.flush();
    g1.flush();
  }
}
