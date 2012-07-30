package org.zeroturnaround.liverebel.test.utils;

import org.zeroturnaround.liverebel.plugins.PluginLogger;

public class TestPluginLogger implements PluginLogger {
  public void log(String message) {
    System.out.println(message);
  }
}
