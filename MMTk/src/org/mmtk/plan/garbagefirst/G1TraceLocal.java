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

import org.mmtk.plan.Trace;
import org.mmtk.plan.TraceLocal;
import org.mmtk.policy.Space;
import org.mmtk.utility.HeaderByte;
import org.mmtk.utility.deque.ObjectReferenceDeque;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.ObjectReference;

/**
 * This class implements the thread-local functionality for a transitive
 * closure over a mark-sweep space.
 */
@Uninterruptible
public final class G1TraceLocal extends TraceLocal {
  /****************************************************************************
   * Instance fields
   */

  /**
   *
   */
  private final ObjectReferenceDeque modBuffer;

  public G1TraceLocal(Trace trace, ObjectReferenceDeque modBuffer) {
    super(G1.SCAN_MARK, trace);
    this.modBuffer = modBuffer;
  }


  /****************************************************************************
   * Externally visible Object processing and tracing
   */

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isLive(ObjectReference object) {
    if (object.isNull()) return false;
    if (Space.isInSpace(G1.MARK_SWEEP, object)) {
      return G1.g1Space.isLive(object);
    }
    return super.isLive(object);
  }

  /**
   * {@inheritDoc}<p>
   *
   * In this instance, we refer objects in the mark-sweep space to the
   * msSpace for tracing, and defer to the superclass for all others.
   *
   * @param object The object to be traced.
   * @return The new reference to the same object instance.
   */
  @Inline
  @Override
  public ObjectReference traceObject(ObjectReference object) {
    if (object.isNull()) return object;
    if (Space.isInSpace(G1.MARK_SWEEP, object))
      return G1.g1Space.traceObject(this, object);
    return super.traceObject(object);
  }

  /**
   * Process any remembered set entries.  This means enumerating the
   * mod buffer and for each entry, marking the object as unlogged
   * (we don't enqueue for scanning since we're doing a full heap GC).
   */
  @Override
  protected void processRememberedSets() {
    if (modBuffer != null) {
      logMessage(5, "clearing modBuffer");
      while (!modBuffer.isEmpty()) {
        ObjectReference src = modBuffer.pop();
        HeaderByte.markAsUnlogged(src);
      }
    }
  }
}
