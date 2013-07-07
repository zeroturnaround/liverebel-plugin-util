package org.zeroturnaround.liverebel.test.utils;

import java.io.File;
import java.util.Collection;

import com.zeroturnaround.liverebel.api.update.ConfigurableUpdate;
import com.zeroturnaround.liverebel.api.update.PausedUpdate;
import com.zeroturnaround.liverebel.api.update.PausingUpdate;
import com.zeroturnaround.liverebel.api.update.RunningUpdate;

public class TestConfigurableUpdateImpl implements ConfigurableUpdate {

  public String updateMode = "HOTPATCH";
  public Collection<Long> schemaIds;
  public String filePath;

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

  public Collection<Long> getSchemaIds() {
    return schemaIds;
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

  public ConfigurableUpdate setScriptEntriesArchive(File scriptEntriesArchive) {
    return this;
  }

  public PausedUpdate executeWithPause() {
    return null;
  }

  public PausingUpdate startWithPause() {
    return null;
  }

  public Long execute() {

    return 0l;
  }

  public RunningUpdate start() {
    return null;
  }

  public ConfigurableUpdate setProxyTargetServerId(String id) {
    return this;
  }

  public ConfigurableUpdate setSchemaIds(Collection<Long> schemaIds) {
    this.schemaIds = schemaIds;
    return this;
  }

  public ConfigurableUpdate setSkipSingleRootDirectory(boolean arg0) {
    return this;
  }

  public String getFileDeploymentPath() {
    return filePath;
  }
}
