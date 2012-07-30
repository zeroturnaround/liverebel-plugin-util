package org.zeroturnaround.liverebel.test.utils;

import org.zeroturnaround.liverebel.plugins.UpdateStrategies;

public class TestUpdateStrategiesImpl implements UpdateStrategies {
  public final boolean hotpatch;
  public final int requestPause;
  public final boolean updateWithWarnings;

  public final boolean rolling;
  public final int sessionDrain;
  public final boolean fullRestart;

  public TestUpdateStrategiesImpl(boolean hotpatch, boolean updateWithWarnings, int requestPause, boolean rolling, int sessionDrain, boolean fullRestart) {
    this.hotpatch = hotpatch;
    this.updateWithWarnings = updateWithWarnings;
    this.requestPause = requestPause;
    this.rolling = rolling;
    this.sessionDrain = sessionDrain;
    this.fullRestart = fullRestart;
  }

  public boolean isHotpatch() {
    return hotpatch;
  }

  public boolean isFullRestart() {
    return fullRestart;
  }

  public boolean isRolling() {
    return rolling;
  }

  public boolean updateWithWarnings() {
    return updateWithWarnings;
  }

  public boolean isDefault() {
    return !hotpatch && !rolling && !fullRestart;
  }

  public int getSessionDrainTimeout() {
    return sessionDrain;
  }

  public int getRequestPauseTimeout() {
    return requestPause;
  }
}
