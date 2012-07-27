package org.zeroturnaround.liverebel.plugins;

/**
 * Interface for getting selected update strategies from plugin UI.
 */
public interface UpdateStrategies {

  /**
   * @return <code>true</code> if Hotpatch is selected.
   */
  boolean isHotpatch();

  /**
   * @return <code>true</code> if full restart (or offline update) is selected.
   */
  boolean isFullRestart();

  /**
   * @return <code>true</code> if rolling restarts is selected.
   */
  boolean isRolling();

  /**
   * @return <code>true</code> if the user has allowed to use Hotpatching even if the archives are compatible with warning
   */
  boolean updateWithWarnings();

  /**
   * @return <code>true</code> if the user wants LiveRebel to determine the best strategy
   */
  boolean isDefault();

  /**
   * @return the session drain timeout in seconds when using rolling restarts
   */
  int getSessionDrainTimeout();

  /**
   * @return the request pause timeout in seconds when using Hotpatching
   */
  int getRequestPauseTimeout();

}
