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

import org.mmtk.plan.StopTheWorldMutator;
import org.mmtk.plan.generational.GenCollector;
import org.mmtk.policy.Space;
import org.mmtk.policy.garbagefirst.Card;
import org.mmtk.policy.garbagefirst.MutatorLocal;
import org.mmtk.policy.garbagefirst.Region;
import org.mmtk.policy.garbagefirst.RegionList;
import org.mmtk.utility.alloc.Allocator;
import org.mmtk.utility.deque.WriteBuffer;
import org.mmtk.vm.VM;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Word;

/**
 * This class implements <i>per-mutator thread</i> behavior and state
 * for the <i>NoGC</i> plan, which simply allocates (without ever collecting
 * until the available space is exhausted.<p>
 *
 * Specifically, this class defines <i>NoGC</i> mutator-time allocation
 * through a bump pointer (<code>def</code>) and includes stubs for
 * per-mutator thread collection semantics (since there is no collection
 * in this plan, these remain just stubs).
 *
 * @see G1
 * @see G1Collector
 * @see org.mmtk.plan.StopTheWorldMutator
 * @see org.mmtk.plan.MutatorContext
 */
@Uninterruptible
public class G1Mutator extends StopTheWorldMutator {

  /************************************************************************
   * Instance fields
   */

  /**
   *
   */
  protected final MutatorLocal g1 = new MutatorLocal(G1.edenSpace);
  protected WriteBuffer remset;
  protected Card cards;

  /****************************************************************************
  *
  * Initialization
  */

 /**
  * Constructor<p>
  *
  * Note that each mutator is a producer of remsets, while each
  * collector is a consumer.  The <code>G1Collector</code> class
  * is responsible for construction of the consumer.
  * @see GenCollector
  */
 public G1Mutator() {
   remset = G1.edenSpace.getRegionList().getWriteBuffer(1);
    cards = G1.getCardMap();
   //remset = new WriteBuffer(G1.remsetPool);
 }

  /****************************************************************************
   * Mutator-time allocation
   */

  /**
   * {@inheritDoc}
   */
  @Inline
  @Override
  public Address alloc(int bytes, int align, int offset, int allocator, int site) {;
    if (allocator == G1.ALLOC_DEFAULT) {
      return g1.alloc(bytes, align, offset);
    }
    return super.alloc(bytes, align, offset, allocator, site);
  }

  @Inline
  @Override
  public void postAlloc(ObjectReference ref, ObjectReference typeRef,
      int bytes, int allocator) {
    if (allocator == G1.ALLOC_DEFAULT) {
      //G1.edenSpace.postAlloc(ref, bytes);
    }
    else {
      super.postAlloc(ref, typeRef, bytes, allocator);
    }
  }

  @Override
  public Allocator getAllocatorFromSpace(Space space) {
    if (space == G1.edenSpace) return g1;
    return super.getAllocatorFromSpace(space);
  }

  /****************************************************************************
  *
  * Barriers
  */

  /**
   * Perform the write barrier fast path, which may involve remembering
   * a reference if necessary.
   *
   * @param src The object into which the new reference will be stored
   * @param slot The address into which the new reference will be
   * stored.
   * @param tgt The target of the new reference
   * @param mode The mode of the store (eg putfield, putstatic etc)
   */
  @Inline
  private void fastPath(ObjectReference src, Address slot, ObjectReference tgt, int mode) {
    RegionList regions = G1.edenSpace.regionsMap;
    Region srcRegion = regions.getRegion(slot);
    Region dstRegion = regions.getRegion(tgt.toAddress());
    remset = dstRegion.getWriteBuffer();

    //System.out.println("src: "+srcRegion.getAddress().toString()+ " dst: "+dstRegion.getAddress().toString());
    /* Remember from old to young references */
    if (srcRegion.isOld() && !dstRegion.isOld()) {
      if (!cards.isMarked(tgt.toAddress()))
        cards.markCard(tgt.toAddress());

      remset.insert(slot);
      dstRegion.rsElementsInc();
    }
    /* Remember from young to other young references */
    if(srcRegion.getAddress().NE(dstRegion.getAddress())) {
      if (!cards.isMarked(tgt.toAddress()))
        cards.markCard(tgt.toAddress());

      remset.insert(slot);
      dstRegion.rsElementsInc();
      //regions.getRegion(slot).getWriteBuffer().insert(slot);
    }
    if (dstRegion.getRsElements() >= 256)
      remset.flushLocal();
  }

  /**
   * {@inheritDoc}<p>
   *
   * In this case, we remember the address of the source of the
   * pointer if the new reference points into the nursery from
   * non-nursery space.
   */
  @Override
  @Inline
  public final void objectReferenceWrite(ObjectReference src, Address slot,
      ObjectReference tgt, Word metaDataA,
      Word metaDataB, int mode) {
    fastPath(src, slot, tgt, mode);
    VM.barriers.objectReferenceWrite(src, tgt, metaDataA, metaDataB, mode);
  }

  /**
   * {@inheritDoc}<p>
   *
   * In this case, we remember the address of the source of the
   * pointer if the new reference points into the nursery from
   * non-nursery space.
   */
  @Override
  @Inline
  public boolean objectReferenceTryCompareAndSwap(ObjectReference src, Address slot, ObjectReference old, ObjectReference tgt,
      Word metaDataA, Word metaDataB, int mode) {
    boolean result = VM.barriers.objectReferenceTryCompareAndSwap(src, old, tgt, metaDataA, metaDataB, mode);
    if (tgt.isNull()) return result;
    if (result)
      fastPath(src, slot, tgt, mode);
    return result;
  }
  /****************************************************************************
   * Collection
   */

  /**
   * {@inheritDoc}
   */
  @Inline
  @Override
  public final void collectionPhase(short phaseId, boolean primary) {
    VM.assertions.fail("GC Triggered in NoGC Plan.");
    /*
     if (phaseId == NoGC.PREPARE) {
     }

     if (phaseId == NoGC.RELEASE) {
     }
     super.collectionPhase(phaseId, primary);
     */
  }
  /** @return The active global plan as a <code>Gen</code> instance. */
  @Inline
  private static G1 global() {
    return (G1) VM.activePlan.global();
  }
}
