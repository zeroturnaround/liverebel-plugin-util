package org.zeroturnaround.liverebel.test.utils;

import com.zeroturnaround.liverebel.api.SchemaInfo;
import com.zeroturnaround.liverebel.util.DatabaseServerExecutionMode;

public class TestSchemaInfoImpl implements SchemaInfo {
  private Long id;
  private String serverId;
  private String targetProxyId;

  public TestSchemaInfoImpl(Long id) {
    this.id = id;
  }

  public TestSchemaInfoImpl(Long id, String serverId) {
    this.id = id;
    this.serverId = serverId;
    this.targetProxyId = serverId;
  }

  public TestSchemaInfoImpl(Long id, String serverId, String proxyId) {
    this.id = id;
    this.serverId = serverId;
    this.targetProxyId = proxyId;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return id.toString();
  }

  public String getServerId() {
    return serverId;
  }

  public String getJdbcUrl() {
    return null;
  }

  public DatabaseServerExecutionMode getExecutionMode() {
    return null;
  }

  public String getTargetProxyServer() {
    return targetProxyId;
  }

}
