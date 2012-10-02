package org.zeroturnaround.liverebel.plugins;

import org.apache.commons.lang.StringUtils;

public class ServerImpl implements Server {
  private String title;
  private String id;
  private String parentNames;

  private int indentDepth;

  private boolean checked;

  private boolean connected;
  private final boolean isGroup;

  public ServerImpl(String id, String title, String parentNames, int indentDepth, boolean selected, boolean connected, boolean isGroup) {
    this.id = id;
    this.parentNames = parentNames;
    this.indentDepth = indentDepth;
    this.connected = connected;
    this.title = title;
    this.checked = selected;
    this.isGroup = isGroup;
  }

  public String getIndentDepthAsCSSClass() {
    switch (indentDepth) {
      case 0:
        return "topLevel";
      case 1:
        return "firstLevel";
      case 2:
        return "secondLevel";
      case 3:
        return "thirdLevel";
      default:
        return "";
    }
  }

  public int getIndentDepth() {
    return indentDepth;
  }

  public String getParentNames() {
    return StringUtils.trimToEmpty(parentNames);
  }

  public boolean isGroup() {
    return isGroup;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
  }

  @Override
  public String toString() {
    return "{ ID="+id+" GROUP_NAME="+getTitle()+" checked="+isChecked()+" isGroup="+isGroup() + " connected="+isConnected() + " parentNames="+parentNames+" }";
  }


}
