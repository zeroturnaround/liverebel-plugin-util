package org.zeroturnaround.liverebel.test.utils;

import java.util.Set;

import com.zeroturnaround.liverebel.api.ServerInfo;
import com.zeroturnaround.liverebel.util.ServerKind;

public class TestServerInfoImpl implements ServerInfo {
  private String id;
  private ServerKind type;

  public TestServerInfoImpl(String id, ServerKind type) {
    this.id = id;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return id;
  }

  public String getAgentVersion() {
    return null;
  }

  public boolean isConnected() {
    return true;
  }

  public boolean isDaemonConnected() {
    return false;
  }

  public boolean isRefreshed() {
    return false;
  }

  public Set<String> getVirtualHostNames() {
    return null;
  }

  public String getDefaultVirtualHostName() {
    return null;
  }

  public boolean isVirtualHostsSupported() {
    return false;
  }

  public Long getParentGroupId() {
    return null;
  }

  public ServerKind getType() {
    return this.type;
  }
}
