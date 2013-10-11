package org.zeroturnaround.liverebel.test.utils;

import org.zeroturnaround.liverebel.plugins.UpdateMode;
import org.zeroturnaround.liverebel.plugins.UpdateStrategies;

public class TestUpdateStrategiesImpl implements UpdateStrategies {

  public final UpdateMode primary;
  public final UpdateMode fallback;

  public final int requestPause;
  public final boolean updateWithWarnings;
  public final int sessionDrain;
  public final int connectionPause;

  public TestUpdateStrategiesImpl(UpdateMode primary, UpdateMode fallback, int requestPause, boolean updateWithWarnings, int sessionDrain) {
    this.primary = primary;
    this.fallback = fallback;
    this.requestPause = requestPause;
    this.updateWithWarnings = updateWithWarnings;
    this.sessionDrain = sessionDrain;
    this.connectionPause = requestPause;
  }

  public UpdateMode getPrimaryUpdateStrategy() {
    return primary;
  }

  public UpdateMode getFallbackUpdateStrategy() {
    return fallback;
  }

  public boolean updateWithWarnings() {
    return updateWithWarnings;
  }

  public int getSessionDrainTimeout() {
    return sessionDrain;
  }

  public int getRequestPauseTimeout() {
    return requestPause;
  }

  public int getConnectionPauseTimeout() {
    return connectionPause;
  }

  @Override
  public String toString() {
    return "TestUpdateStrategiesImpl [primary=" + primary + ", fallback=" + fallback + ", updateWithWarnings="
        + updateWithWarnings + ", sessionDrainTimeout=" + sessionDrain + ", requestPauseTimeout=" + requestPause
        + ", connectionPauseTimeout=" + connectionPause + "]";
  }

}
