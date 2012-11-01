package org.zeroturnaround.liverebel.test.utils;

import java.util.Set;
import com.zeroturnaround.liverebel.api.ServerInfo;

public class TestServerInfoImpl implements ServerInfo {
  private String id;

  public TestServerInfoImpl(String id) {
    this.id = id;
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
}
