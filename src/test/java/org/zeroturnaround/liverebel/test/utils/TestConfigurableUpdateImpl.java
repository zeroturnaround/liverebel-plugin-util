package org.zeroturnaround.liverebel.test.utils;

import com.zeroturnaround.liverebel.api.update.ConfigurableUpdate;
import com.zeroturnaround.liverebel.api.update.PausedUpdate;
import com.zeroturnaround.liverebel.api.update.PausingUpdate;
import com.zeroturnaround.liverebel.api.update.RunningUpdate;

import java.io.File;
import java.util.Collection;

public class TestConfigurableUpdateImpl implements ConfigurableUpdate {

  public String updateMode = "HOTPATCH";

  public ConfigurableUpdate on(String serverId) {
    return this;
  }

  public ConfigurableUpdate on(Collection<String> serverIds) {
    return this;
  }

  public ConfigurableUpdate onGroup(String groupName) {
    return this;
  }

  public ConfigurableUpdate onGroups(Collection<String> groupNames) {
    return this;
  }

  public ConfigurableUpdate withTimeout(int seconds) {
    return this;
  }

  public ConfigurableUpdate enableOffline() {
    //see UpdateMode
    updateMode = "OFFLINE";
    return this;
  }

  public boolean isOffline() {
    return "OFFLINE".equalsIgnoreCase(updateMode);
  }

  public boolean isRolling() {
    return "ROLLING".equalsIgnoreCase(updateMode);
  }

  public ConfigurableUpdate enableRolling() {
    //see UpdateMode
    updateMode = "ROLLING";
    return this;
  }

    public ConfigurableUpdate disableScriptExecution() {
        return this;
    }

    public ConfigurableUpdate enableScriptExecution() {
    return this;
  }

  public ConfigurableUpdate enableAutoStrategy(boolean ignoreWarnings) {
    return this;
  }

  public ConfigurableUpdate setScriptExecutionDescription(File scriptDescription) {
    return this;
  }

  public PausedUpdate executeWithPause() {
    return null;
  }

  public PausingUpdate startWithPause() {
    return null;
  }

  public void execute() {

  }

  public RunningUpdate start() {
    return null;
  }
}
