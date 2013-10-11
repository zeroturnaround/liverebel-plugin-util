package org.slf4j.impl;

import org.slf4j.spi.MDCAdapter;

import ch.qos.logback.classic.util.LogbackMDCAdapter;

/**
 * Copy of {@link StaticMDCBinder} from Logback project.
 */
public class StaticMDCBinder {

  /**
   * The unique instance of this class.
   */
  public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

  private StaticMDCBinder() {
  }
  
  /**
   * Currently this method always returns an instance of 
   * {@link StaticMDCBinder}.
   */
  public MDCAdapter getMDCA() {
     return new LogbackMDCAdapter();
  }

  public String  getMDCAdapterClassStr() {
    return LogbackMDCAdapter.class.getName();
  }

}
