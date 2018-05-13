package org.mmtk.policy.garbagefirst;

/*
 * This class defines operations in region-granularity
 */
public abstract class Region {

  /* Min Region size 1MB */
  public static final int MIN_REGION_BYTES = 1 << 20;
  public static final int MIN_REGION_LOG = 20;
  /* Max Region size 32MB */
  public static final int MAX_REGION_BYTES = 32 << 20;
  public static final int MAX_REGION_LOG = 22;
  /* User defined region size */
  public static final int REGION_SIZE = 1 << 19;
  public static final int REGION_LOG = 19;
}
