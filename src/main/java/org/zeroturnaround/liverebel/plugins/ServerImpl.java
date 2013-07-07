package org.zeroturnaround.liverebel.plugins;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.zeroturnaround.liverebel.util.ServerKind;

public class ServerImpl implements Server {
  private String title;
  private String id;
  private String parentNames;

  private int indentDepth;

  private boolean checked;

  private boolean connected;
  private final boolean isGroup;

  private final ServerKind type;

  private final Set<String> virtualHostNames;
  private final String defaultVirtualHostName;
  private final boolean virtualHostsSupported;

  public ServerImpl(String id, String title, String parentNames, int indentDepth, boolean selected, boolean connected, boolean isGroup,
                    ServerKind type, boolean virtualHostsSupported, String defaultVirtualHostName, Set<String> virtualHostNames) {
    this.id = id;
    this.parentNames = parentNames;
    this.indentDepth = indentDepth;
    this.connected = connected;
    this.title = title;
    this.checked = selected;
    this.isGroup = isGroup;
    this.type = type;
    this.virtualHostsSupported = virtualHostsSupported;
    this.defaultVirtualHostName = defaultVirtualHostName;
    this.virtualHostNames = virtualHostNames;
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
    return getTitle();
  }

  public ServerKind getType() {
    return type;
  }

  public Set<String> getVirtualHostNames() {
    return virtualHostNames;
  }

  public String getDefaultVirtualHostName() {
    return defaultVirtualHostName;
  }

  public boolean isVirtualHostsSupported() {
    return virtualHostsSupported;
  }

}
