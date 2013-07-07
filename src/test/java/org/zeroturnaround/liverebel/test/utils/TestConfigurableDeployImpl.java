package org.zeroturnaround.liverebel.test.utils;

import java.io.File;
import java.util.Collection;

import com.zeroturnaround.liverebel.api.deploy.ConfigurableDeploy;
import com.zeroturnaround.liverebel.api.update.PausedUpdate;
import com.zeroturnaround.liverebel.api.update.PausingUpdate;
import com.zeroturnaround.liverebel.api.update.RunningUpdate;

public class TestConfigurableDeployImpl implements ConfigurableDeploy {

  public String updateMode = "HOTPATCH";
  public String schemaId = "1";
  public String filePath = "c:\\temp\\testStaticContentDeploy";
  private String contextPath = "testStaticContentDeploy";
  private Object virtualHost = "";
  private String destFileName = "";

  public ConfigurableDeploy on(String serverId) {
    return this;
  }

  public ConfigurableDeploy on(Collection<String> serverIds) {
    return this;
  }

  public ConfigurableDeploy onGroup(String groupName) {
    return this;
  }

  public ConfigurableDeploy onGroups(Collection<String> groupNames) {
    return this;
  }

  public ConfigurableDeploy withTimeout(int seconds) {
    return this;
  }

  public ConfigurableDeploy enableOffline() {
    //see UpdateMode
    updateMode = "OFFLINE";
    return this;
  }

  public String getSchemaId() {
    return schemaId;
  }

  public boolean isOffline() {
    return "OFFLINE".equalsIgnoreCase(updateMode);
  }

  public boolean isRolling() {
    return "ROLLING".equalsIgnoreCase(updateMode);
  }

  public ConfigurableDeploy enableRolling() {
    //see UpdateMode
    updateMode = "ROLLING";
    return this;
  }

  public ConfigurableDeploy disableScriptExecution() {
        return this;
    }

  public ConfigurableDeploy enableScriptExecution() {
    return this;
  }

  public ConfigurableDeploy enableAutoStrategy(boolean ignoreWarnings) {
    return this;
  }

  public ConfigurableDeploy setScriptExecutionDescription(File scriptDescription) {
    return this;
  }

  public ConfigurableDeploy setScriptEntriesArchive(File scriptEntriesArchive) {
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

  public ConfigurableDeploy setProxyTargetServerId(String arg0) {
    return this;
  }

  public ConfigurableDeploy setSchemaIds(Collection<Long> arg0) {
    return this;
  }

  public ConfigurableDeploy setSkipSingleRootDirectory(boolean arg0) {
    return this;
  }

  public String getFileDeploymentPath() {
    return filePath;
  }

  public ConfigurableDeploy setContextPath(String ctxPath) {
    this.contextPath = ctxPath;
    return this;
  }

  public ConfigurableDeploy setVirtualHostName(String virtualHostName) {
    this.virtualHost = virtualHostName;
    return this;
  }

  public ConfigurableDeploy setFileServerDeploymentPath(String path) {
    this.filePath = path;
    return this;
  }

  public ConfigurableDeploy setDestinationFileName(String destFileName) {
    this.destFileName = destFileName;
    return this;
  }

  public ConfigurableDeploy disableScriptExecution(boolean disable) {
    return null;
  }
}
