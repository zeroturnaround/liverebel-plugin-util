package org.zeroturnaround.liverebel.plugins;

import java.io.File;
import java.util.List;

public class PluginConf {
  public enum Action {
    UPLOAD("Upload"),
    DEPLOY_OR_UPDATE("Deploy or Update"),
    UNDEPLOY("Undeploy");

    private String name;
    private Action(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public PluginConf(Action action) {
    this.action = action;
  }

  private Action action;

  public File deployable;
  public File metadata;
  public List<String> serverIds;
  public String contextPath;
  public UpdateStrategies updateStrategies;
  public String undeployId;
  public boolean isOverride;

  public String overrideApp;
  public String overrideVer;

  public boolean hasStaticContent;
  public boolean hasDatabaseMigrations;
  public String virtualHostName;
  public String destinationFileName;
  public String filePath;
  public String schemaId;
  public String targetProxyId;
  public List<String> staticServerIds;

  public Action getAction() {
    return action;
  }

  public boolean isLiveRebelXmlOverride() {
    return isOverride;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  @Override
  public String toString() {
    return "PluginConf{" +
        "action=" + action +
        ", deployable=" + deployable +
        ", metadata=" + metadata +
        ", serverIds=" + serverIds +
        ", contextPath='" + contextPath + '\'' +
        ", updateStrategies=" + updateStrategies +
        ", undeployId='" + undeployId + '\'' +
        ", isOverride=" + isOverride +
        ", overrideApp='" + overrideApp + '\'' +
        ", overrideVer='" + overrideVer + '\'' +
        ", destinationFileName='" + destinationFileName + '\'' +
        ", hasDatabaseMigrations=" + hasDatabaseMigrations +
        ", schemaId='" + schemaId + '\'' +
        ", targetProxyId='" + targetProxyId + '\'' +
        ", virtualHostName='" + virtualHostName + '\'' +
        ", hasDatabaseMigrations=" + hasDatabaseMigrations +
        ", staticServerIds=" + staticServerIds +
        ", filePath='" + filePath + '\'' +
        '}';
  }


}
