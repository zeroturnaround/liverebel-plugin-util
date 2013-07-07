package org.zeroturnaround.liverebel.plugins;

public enum UpdateMode {
  HOTPATCH ("Hotpatch", "HOTPATCH"),
  ROLLING_RESTARTS ("Rolling Restarts", "ROLLING_RESTARTS"),
  OFFLINE ("Full restart", "OFFLINE"),
  FAIL_BUILD ("Fail the build", "FAIL_BUILD"),
  LIVEREBEL_DEFAULT("LiveRebel default", "LIVEREBEL_DEFAULT"),
  ALL_AT_ONCE_UPDATE("All-at-once update", "ALL_AT_ONCE_UPDATE");

  private UpdateMode(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String name;
  public String value;

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
