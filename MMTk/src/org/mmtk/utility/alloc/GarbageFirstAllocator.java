package org.mmtk.utility.alloc;

import org.mmtk.plan.garbagefirst.G1;
import org.mmtk.policy.Space;
import org.mmtk.policy.garbagefirst.*;
import org.mmtk.utility.options.Options;
import org.mmtk.vm.VM;
import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;
/**
 * 
 */
@Uninterruptible
public class GarbageFirstAllocator extends Allocator{
	  /****************************************************************************
	   *
	   * Instance variables
	   */

	  /** space this allocator is associated with */
	protected GarbageFirstSpace space;
	
	/** bump pointer */
	private Address cursor;
	/** limit for bump pointer */
	private Address limit;
	/** bump pointer for large objects */
	private Address largeCursor;
	/** limit for bump pointer for large objects */
	private Address largeLimit;
	/** is the current request for large or small? */
	private boolean requestForLarge;
	
	/*
	 * Constructor
	 * 
	 * @param space The space to bump point into.
	 */
	public GarbageFirstAllocator(GarbageFirstSpace space) {
		this.space = space;
		reset();
	}
	/**
	 * Reset the allocator. Note that this does not reset the space.
	 */
	public void reset() {
	  cursor = Address.zero();
	  limit = Address.zero();
	  largeCursor = Address.zero();
	  largeLimit = Address.zero();
	  requestForLarge = false;
	}
	
  /**
   * Re-associate this bump pointer with a different space. Also
   * reset the bump pointer so that it will use the new space
   * on the next call to <code>alloc</code>.
   *
   * @param space The space to associate the bump pointer with.
   */
  public final void rebind(GarbageFirstSpace space) {
    reset();
    this.space = space;
  }
	/*****************************************************************************
  *
  * Public interface
  */

 /**
  * Allocate space for a new object.  This is frequently executed code and
  * the coding is deliberately sensitive to the optimizing compiler.
  * After changing this, always check the IR/MC that is generated.
  *
  * @param bytes The number of bytes allocated
  * @param align The requested alignment
  * @param offset The offset from the alignment
  * @return The address of the first byte of the allocated region
  */
 @Inline
 public final Address alloc(int bytes, int align, int offset) {
   /* establish how much we need */
   Address start = alignAllocationNoFill(cursor, align, offset);
   Address end = start.plus(bytes);
//System.out.println("cur: "+start.toString());
   /* check whether we've exceeded the limit */
   if (end.GT(limit)) {
     //System.out.println("alloc limit");

//     if (bytes > BYTES_IN_REGION)
//       return overflowAlloc(bytes, align, offset);
//     else
       return allocSlow(bytes, align, offset);
   }

   /* sufficient memory is available, so we can finish performing the allocation */
   fillAlignmentGap(cursor, start);
   cursor = end;

   return start;
 }

 /**
  * Allocate space for a new object.  This is frequently executed code and
  * the coding is deliberately sensitive to the optimizing compiler.
  * After changing this, always check the IR/MC that is generated.
  *
  * @param bytes The number of bytes allocated
  * @param align The requested alignment
  * @param offset The offset from the alignment
  * @return The address of the first byte of the allocated region
  */
 public final Address overflowAlloc(int bytes, int align, int offset) {
   /* establish how much we need */
   Address start = alignAllocationNoFill(largeCursor, align, offset);
   Address end = start.plus(bytes);

   /* check whether we've exceeded the limit */
   if (end.GT(largeLimit)) {
     requestForLarge = true;
     Address rtn =  allocSlowInline(bytes, align, offset);
     requestForLarge = false;
     return rtn;
   }

   /* sufficient memory is available, so we can finish performing the allocation */
   fillAlignmentGap(largeCursor, start);
   largeCursor = end;

   return start;
 }
 
  /** @return the space associated with this g1 allocator */
  @Override
  public final GarbageFirstSpace getSpace() {
    return space;
  }
 
  /** @return the current cursor value */
  public final Address getCursor() {
    return cursor;
  }
 
  @Override
  protected Address allocSlowOnce(int bytes, int alignment, int offset) {
    Address ptr = space.getSpace();
    
    if (ptr.isZero()) return ptr; //Failed need GC
    
    cursor = ptr;
    limit = ptr.plus(Region.REGION_SIZE);
  
    return alloc(bytes, alignment, offset);
  }
}
