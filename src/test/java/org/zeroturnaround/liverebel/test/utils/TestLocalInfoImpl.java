package org.zeroturnaround.liverebel.test.utils;

import java.util.Set;
import com.zeroturnaround.liverebel.api.LocalInfo;

public class TestLocalInfoImpl implements LocalInfo {
  private String id;
  private String ver;

  public TestLocalInfoImpl(String id, String ver) {
    this.id = id;
    this.ver = ver;
  }

  public String getServerId() {
    return id;
  }

  public String getVersionId() {
    return ver;
  }

  public Set<String> getVirtualHostNames() {
    return null;
  }

  public boolean isEditMode() {
    return false;
  }

  public String getLocalPath() {
    return null;
  }

  public Set<String> getUrls() {
    return null;
  }
}
